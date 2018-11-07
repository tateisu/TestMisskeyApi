package jp.juggler.testmisskeyapi.tests

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import jp.juggler.testmisskeyapi.ApiTest
import jp.juggler.testmisskeyapi.Config
import jp.juggler.testmisskeyapi.TestStatus
import jp.juggler.testmisskeyapi.utils.TestSequence
import jp.juggler.testmisskeyapi.utils.jsonObject

@TestSequence
suspend fun testUserRelation(ts : TestStatus) {

    Config.user2Id ?: return

    val userIdSet = HashSet<String>()

    ApiTest(
        caption = "(user2)ローカルTL"
        , path = "/api/notes/local-timeline"
        , params = jsonObject("limit" to 40)
        , accessToken = Config.user2AccessToken
        , checkExists = arrayOf("0.id")
        , idFinder = "0.id"
        , untilId = true
        , sinceId = true
        , after = {
        // TL中のユーザIDを収集する
        for (item in it as JsonArray<*>) {
            if (item is JsonObject) {
                val userId = item.string("userId")
                if (userId != null) userIdSet.add(userId)
            }
        }
    }
    ).run(ts)

    if (userIdSet.isNotEmpty()) {
        ApiTest(
            caption = "(user2)ユーザリレーション"
            , path = "/api/users/relation"
            , params = jsonObject("userId" to JsonArray<String>().apply { addAll(userIdSet) })
            , accessToken = Config.user2AccessToken
            , checkExists = arrayOf("0.isFollowing")
            , after = { ts.log.i("array size=${(it as JsonArray<*>).size}/${userIdSet.size}") }
        ).run(ts)
    }
}
