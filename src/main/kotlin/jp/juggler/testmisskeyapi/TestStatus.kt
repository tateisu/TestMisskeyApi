package jp.juggler.testmisskeyapi

import jp.juggler.testmisskeyapi.utils.LogBuffer
import kotlin.reflect.KFunction

class TestStatus( val kFunction : KFunction<*> ){
    fun makeName(i : Int) {
        this.name = "$i${kFunction.name}"
    }

    val log = LogBuffer()

    lateinit var name : String
}