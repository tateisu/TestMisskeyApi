package jp.juggler.testmisskeyapi

import com.beust.klaxon.Klaxon
import jp.juggler.testmisskeyapi.utils.LogTagBase
import java.io.File

data class CachedRequest(
    val url : String,
    val jsonParams : String,
    val time : Long? = null,
    val status : String? = null,
    val error : String? = null,
    val content : String? = null
) {

    companion object {
        fun readFile(file : File) : CachedRequest? {
            val json = jp.juggler.testmisskeyapi.utils.readFile(file).toString(Charsets.UTF_8)
            return Klaxon().parse<CachedRequest>(json)
        }
    }

    fun writeFile(file : File) : CachedRequest {
        val json = Klaxon().toJsonString(this)
        jp.juggler.testmisskeyapi.utils.writeFile(file, json.toByteArray(Charsets.UTF_8))

        return this
    }

    fun dumpStatus(log : LogTagBase, ignoreError :Regex? = null) {
        log.v("time: ${time}ms")

        if (error != null ) {
            if( ignoreError?.find(error) != null ){
                // don't show error
            }else{
                log.e(error)
            }
        }

        if (status != null) log.v(status)
    }
}
