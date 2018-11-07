package jp.juggler.testmisskeyapi.utils

import java.util.concurrent.atomic.AtomicInteger

// ログ出力のインタフェース
interface LogTagBase {

    fun e(message : String)
    fun w(message : String)
    fun i(message : String)
    fun v(message : String)
}

// stdoutに出力する。エラーと警告をカウントする。
class LogTag : LogTagBase {

    val countError = AtomicInteger(0)
    val countWarning = AtomicInteger(0)

    override fun e(message : String) {
        val n = countError.incrementAndGet()
        println("ERROR[$n]: $message")
    }

    override fun w(message : String) {
        val n = countWarning.incrementAndGet()
        println("WARN[$n]: $message")
    }

    override fun i(message : String) {
        println("INFO: $message")
    }

    override fun v(message : String) {
        println("VERB: $message")
    }

}

// 内部バッファに貯めて、後から別のログ出力先にまとめて転送する。
class LogBuffer : LogTagBase {

    private enum class LogLevel {
        E, W, I, V
    }

    private val buffer = ArrayList<Pair<LogLevel, String>>()

    private fun addBuffer(l : LogLevel, m : String) {
        buffer.add(Pair(l, m))
    }

    fun copyBuffer(other : LogTagBase) {
        buffer.forEach { pair ->
            when (pair.first) {
                LogLevel.E -> other.e(pair.second)
                LogLevel.W -> other.w(pair.second)
                LogLevel.I -> other.i(pair.second)
                LogLevel.V -> other.v(pair.second)
            }
        }
    }

    override fun e(message : String) = addBuffer(LogLevel.E, message)
    override fun w(message : String) = addBuffer(LogLevel.W, message)
    override fun i(message : String) = addBuffer(LogLevel.I, message)
    override fun v(message : String) = addBuffer(LogLevel.V, message)
}
