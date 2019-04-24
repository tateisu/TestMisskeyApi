package jp.juggler.testmisskeyapi.tests

import jp.juggler.testmisskeyapi.ApiTest
import jp.juggler.testmisskeyapi.Config
import jp.juggler.testmisskeyapi.TestStatus
import jp.juggler.testmisskeyapi.utils.TestSequence
import jp.juggler.testmisskeyapi.utils.jsonObject

@TestSequence
suspend fun testUserMuteList(ts : TestStatus) {

    Config.user2Id ?: return

    ApiTest(
        caption = "(user2)ミュートしたユーザの一覧(あらかじめ2件以上登録しておいてください)"
        , path = "/api/mute/list"
        , accessToken = Config.user2AccessToken
        , checkExists = arrayOf("0.id")
        , idFinder = "0.id"
        , untilId = true
        , sinceId = true
    ).run(ts)
}

@TestSequence
suspend fun testUserMuteAction(ts : TestStatus) {
    Config.user1Id ?: return
    Config.user2Id ?: return

    // mute
    ApiTest(
        caption = "(user1,user2)ユーザ1からユーザ2をミュート解除(前準備)"
        , path = "/api/mute/delete"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("userId" to Config.user2Id)
        , ignoreError = "already not |You are not muting that user".toRegex()
    ).run(ts)

    ApiTest(
        caption = "(user1,user2)ユーザ1からユーザ2をミュート"
        , path = "/api/mute/create"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("userId" to Config.user2Id)
    ).run(ts)


    ApiTest(
        caption = "(user1,user2)ユーザ1からユーザ2をミュート解除"
        , path = "/api/mute/delete"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("userId" to Config.user2Id)
    ).run(ts)

}
