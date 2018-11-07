package jp.juggler.testmisskeyapi.tests

import jp.juggler.testmisskeyapi.ApiTest
import jp.juggler.testmisskeyapi.Config
import jp.juggler.testmisskeyapi.TestStatus
import jp.juggler.testmisskeyapi.utils.TestSequence
import jp.juggler.testmisskeyapi.utils.jsonObject

@TestSequence
suspend fun testUserBlockList(ts : TestStatus) {

    Config.user2Id ?: return

    ApiTest(
        caption = "(user2)ブロックしたユーザの一覧(あらかじめ2件以上登録しておいてください)"
        , path = "/api/blocking/list"
        , accessToken = Config.user2AccessToken
        , checkExists = arrayOf("0.id")
        , idFinder = "0.id"
        , untilId = true
        , sinceId = true
    ).run(ts)
}

@TestSequence
suspend fun testUserBlockAction(ts : TestStatus) {
    Config.user1Id ?: return
    Config.user2Id ?: return

    ApiTest(
        caption = "(user1,user2)ブロックAPIテストの前準備"
        , path = "/api/blocking/delete"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("userId" to Config.user2Id)
        , ignoreError = "already not blocking".toRegex()
    ).run(ts)

    ApiTest(
        caption = "(user1,user2)ユーザ1からユーザ2をブロック"
        , path = "/api/blocking/create"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("userId" to Config.user2Id)
    ).run(ts)

    ApiTest(
        caption = "(user1,user2)ユーザ1からユーザ2をブロック解除"
        , path = "/api/blocking/delete"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("userId" to Config.user2Id)
    ).run(ts)
}