package jp.juggler.testmisskeyapi

import jp.juggler.testmisskeyapi.utils.LogTag
import jp.juggler.testmisskeyapi.utils.jsonArray
import jp.juggler.testmisskeyapi.utils.readFile
import java.io.File
import java.nio.file.Paths

object Config{
    val log = LogTag()

    ///////////////////////////////////////
    // 内部ステータス

    private var hasError = false

    var user1Id : Any? = null
    var user3Id : Any? = null
    var user2Id : Any? = null


    val permissionArray = jsonArray(
        "account-read",
        "account-write",

        "note-read",
        "note-write",

        "reaction-read",
        "reaction-write",

        "following-read",
        "following-write",

        "drive-read",
        "drive-write",

        "notification-read",
        "notification-write",

        "favorite-read",
        "favorites-read",
        "favorite-write",

        "account/read",
        "account/write",

        "messaging-read",
        "messaging-write",

        "vote-read",
        "vote-write"
    )


    ///////////////////////////////////////

    var clearCache = false
    var instance = ""
    var user1AccessToken = ""
    var user2AccessToken = ""
    var user3AccessToken = ""
    var user1Password = ""
    var cacheDir : File = File("./cache")
    var dumpAll = false
    var jpegSample : ByteArray? = null

    private fun configError(message : String) {
        hasError = true
        log.e(message)
    }

    private fun parseConfigPair(k : String?, v : String?) {
        if (k?.isEmpty() != false) error("parseConfigPair: key is empty or null.")
        if (v == null) error("parseConfigPair: value is null.")

        var hideValue = false
        when (k) {
            "clearCache" -> clearCache = v.toBoolean()
            "dumpAll" -> dumpAll = v.toBoolean()
            "instance" -> instance = v
            "jpegSample" -> jpegSample = readFile(v)

            "user1AccessToken" -> {
                user1AccessToken = v
                hideValue = true
            }

            "user2AccessToken" -> {
                user2AccessToken = v
                hideValue = true
            }

            "user3AccessToken" -> {
                user3AccessToken = v
                hideValue = true
            }

            "user1Password" -> {
                user1Password = v
                hideValue = true
            }

            "cacheDir" -> cacheDir = File(v)

            else -> error("parseConfigPair: unknown key $k.")
        }
        if (hideValue) {
            App.log.v("parseConfigPair: $k=***")
        } else {
            App.log.v("parseConfigPair: $k=$v")
        }
    }

    // 行頭(空白は読み飛ばす) の # 以降はコメント行
    private val reComment = """\A\s*#.*""".toRegex()

    private fun parseConfigFile(fileName : String, fileContent : String) {
        fileContent.split("\n")
            .map { it.trim().replace(reComment, "") }
            .forEachIndexed { lineIndex, line ->
                try {
                    if (line.isEmpty()) return@forEachIndexed
                    val kv = line
                        .split("=", limit = 2)
                        .map { it.trim() }
                    if (kv.size != 2) error("bad line format: please specify <key>=<value>")
                    parseConfigPair(kv[0], kv[1])
                } catch (ex : Throwable) {
                    configError("$fileName ${lineIndex + 1} : $ex")
                }
            }
    }

    // -key=value 形式のオプション
    private val reOptionKeyValue = """\A-([^=]+)=(.+)""".toRegex()

    fun parseOptions(args : Array<String>) {
        val cwd = Paths.get("").toAbsolutePath().toString()
        App.log.v("current working directory: $cwd")

        App.log.v("arguments: ${args.joinToString(",")}")

        args.forEachIndexed { index, sv ->
            try {
                val m = reOptionKeyValue.find(sv)
                if (m != null) {
                    val k = m.groups[1]?.value?.trim()
                    val v = m.groups[2]?.value?.trim()
                    parseConfigPair(k, v)
                } else when (sv) {
                    "-c" -> clearCache = true
                    else -> parseConfigFile(sv, readFile(sv).toString(Charsets.UTF_8))
                }
            } catch (ex : Throwable) {
                ex.printStackTrace()
                configError("argument ${index + 1} : $ex")
            }
        }
        if (instance.isEmpty()) configError("instance is not specified.")
        if (user1AccessToken.isEmpty()) configError("user1AccessToken is not specified.")

        cacheDir.mkdir()
        if (! cacheDir.isDirectory || ! cacheDir.canWrite()) {
            configError("cacheDir $cacheDir is not directory or can't write.")
        }

        if (hasError) System.exit(1)
    }

}