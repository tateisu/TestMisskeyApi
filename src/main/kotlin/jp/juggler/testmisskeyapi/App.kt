package jp.juggler.testmisskeyapi

import com.beust.klaxon.JsonBase
import io.github.classgraph.ClassGraph
import jp.juggler.testmisskeyapi.utils.LogBuffer
import jp.juggler.testmisskeyapi.utils.TestSequence
import jp.juggler.testmisskeyapi.utils.lookupSimple
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.kotlinFunction

object App : CoroutineScope {

    val log = Config.log

    @Volatile
    var exitCode = 1
    val mainJob = Job()

    override val coroutineContext : CoroutineContext
        get() = Dispatchers.Default + mainJob


    private fun clearCache() {
        Config.cacheDir
            .list()
            .asSequence()
            .map { File(it) }
            .forEach {
                if (it.isFile) it.delete()
            }
    }

    private suspend fun getUserIds() {

        data class Params(
            val caption : String,
            val accessToken : String,
            val after : (JsonBase) -> Unit
        )

        val list = arrayOf(

            Params(
                "user1",
                Config.user1AccessToken
            ) {
                Config.user1Id = it.lookupSimple("id")
            },

            Params(
                "user2",
                Config.user2AccessToken
            ) {
                Config.user2Id = it.lookupSimple("id")
            },

            Params(
                "user3",
                Config.user3AccessToken
            ) {
                Config.user3Id = it.lookupSimple("id")
            }

        ).mapNotNull {
            if (it.accessToken.isEmpty()) {
                null
            } else {
                async(Dispatchers.IO + coroutineContext) {
                    val logBuffer = LogBuffer()
                    ApiTest(
                        caption = "(${it.caption})ログインユーザの情報"
                        , path = "/api/i"
                        , checkExists = arrayOf("id")
                        , accessToken = it.accessToken
                        , after = it.after
                    ).run(logBuffer, "0getUserIds-${it.caption}")
                    logBuffer
                }
            }
        }

        list.forEach {
            val rv = it.await()
            rv.copyBuffer(log)
        }

    }

    private fun getTestFunctions() : ArrayList<KFunction<*>> {
        val dst = ArrayList<KFunction<*>>()
        try {
            val pkg = "jp.juggler.testmisskeyapi.tests"
            ClassGraph().whitelistPackages(pkg).enableMethodInfo().scan().use { scanResult ->
                scanResult.allClasses.forEach { classInfo ->
                    classInfo.loadClass().methods.forEach { javaMethod ->
                        javaMethod.kotlinFunction?.let { kFunction ->
                            if (kFunction.annotations.any { it is TestSequence }) {
                                dst.add(kFunction)
                            }
                        }
                    }
                }
            }
        } catch (ex : Throwable) {
            ex.printStackTrace()
        }
        dst.sortBy { it.name.toLowerCase() }
        return dst
    }

    suspend fun run() {
        if (Config.clearCache) clearCache()

        getUserIds()
        if (! isActive) return

        val tssList = getTestFunctions().map { TestStatus(it) }
        tssList.forEachIndexed { i, v -> v.makeName(i) }

        val queue = ConcurrentLinkedQueue<TestStatus>()
        queue.addAll(tssList)

        val runners = (0 until 4).map { rNum ->
            async(Dispatchers.IO + coroutineContext) {
                while (isActive) {
                    val tss = queue.poll() ?: break
                    log.i("runner#$rNum ${tss.name} start")
                    try {
                        tss.kFunction.callSuspend(tss)
                    } catch (ex : Throwable) {
                        ex.printStackTrace()
                    }
                    log.i("runner#$rNum ${tss.name} end")
                }
            }
        }.toList()

        runners.forEach { it.await() }

        for (tss in tssList) {
            if (! isActive) break
            log.i("####################")
            log.i("# ${tss.name} result")
            tss.log.copyBuffer(log)
        }

        if (isActive) {
            log.i("####################")
            log.i("# all test completed. error=${log.countError}, warning=${log.countWarning}")
            exitCode = 0
        }
    }
}

fun main(args : Array<String>) {
    Runtime.getRuntime().addShutdownHook(Thread {
        if (App.exitCode != 0) {
            App.log.i("Shutdown Hook detected.")
            App.mainJob.cancel()
        }
    })
    runBlocking {
        Config.parseOptions(args)
        App.run()
    }
    System.exit(App.exitCode)
}
