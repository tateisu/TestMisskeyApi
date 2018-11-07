package jp.juggler.testmisskeyapi.utils

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonBase
import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import java.io.*

fun copyStream(inStream : InputStream, outStream : OutputStream) {
    val tmp = ByteArray(4096)
    while (true) {
        val delta = inStream.read(tmp, 0, tmp.size)
        if (delta <= 0) break
        outStream.write(tmp, 0, delta)
    }
}

fun readStream(inStream : InputStream) : ByteArray {
    val bao = ByteArrayOutputStream()
    copyStream(inStream, bao)
    return bao.toByteArray()
}

fun readFile(file : File) : ByteArray =
    FileInputStream(file).use { readStream(it) }

fun readFile(fileName : String) : ByteArray =
    readFile(File(fileName))

fun writeFile(file : File, content : ByteArray) : Unit =
    FileOutputStream(file).use { it.write(content) }

//fun writeFile(fileName : String, content : ByteArray) : Unit =
//    writeFile(File(fileName), content)

val digitsLower : CharArray = ArrayList<Char>().apply { "0123456789abcdef".forEach { c -> add(c) } }.toCharArray()

fun ByteArray.toHex() : String {
    val sb = StringBuilder(size * 2)
    for (idx in 0 until size) {
        val b : Int = get(idx).toInt() and 255
        sb.append(digitsLower[b shr 4])
        sb.append(digitsLower[b and 15])
    }
    return sb.toString()
}

private val reSplitPath = "[/.]".toRegex()
fun JsonBase.lookupSimple(path : String) : Any? {
    var parent : Any? = this
    for (entry in path.split(reSplitPath).filter { it.isNotEmpty() }) {
        parent = when (parent) {
            is JsonObject -> parent[entry]
            is JsonArray<*> -> parent[entry.toInt()]
            else -> error("$parent has no child $entry.")
        }
    }
    return parent
}

fun JsonObject.shallowClone() : JsonObject {
    val dst = JsonObject()
    for ((k, v) in entries) {
        dst.put(k, v)
    }
    return dst
}

fun jsonObject(vararg args : Pair<String, Any?>) : JsonObject =
    json { obj(*args) }

fun jsonArray(vararg args : Any?) : JsonArray<*> = json { array(*args) }
