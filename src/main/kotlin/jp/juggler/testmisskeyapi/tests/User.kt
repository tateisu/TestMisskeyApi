package jp.juggler.testmisskeyapi.tests

import jp.juggler.testmisskeyapi.ApiTest
import jp.juggler.testmisskeyapi.Config
import jp.juggler.testmisskeyapi.TestStatus
import jp.juggler.testmisskeyapi.utils.TestSequence
import jp.juggler.testmisskeyapi.utils.jsonObject

@TestSequence
suspend fun testUser1(ts : TestStatus) {

    Config.user1Id ?: return

    if (Config.user1Password.isNotEmpty()) {
        ApiTest(
            caption = "(user1)パスワード変更(WebUIのアクセストークンでは成功する)"
            , path = "/api/i/change_password"
            , params = jsonObject("currentPassword" to Config.user1Password, "newPassword" to Config.user1Password)
            , accessToken = Config.user1AccessToken
            , check204 = true
        ).run(ts)
    }

    ApiTest(
        caption = "(user1)ユーザ説明文の更新"
        , path = "/api/i/update"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("description" to "がうがう")
        , checkExists = arrayOf("description")
    ).run(ts)
}

@TestSequence
suspend fun testUser2(ts : TestStatus) {

    Config.user2Id ?: return

    ApiTest(
        caption = "(user2)ログイン履歴"
        , path = "/api/i/signin_history"
        , accessToken = Config.user2AccessToken
        , checkExists = arrayOf("0.id", "0.ip", "0.success")
        , idFinder = "0.id"
        , sinceId = true
        , untilId = true
    ).run(ts)

}


@TestSequence
suspend fun testUserShow(ts : TestStatus) {
    Config.user1Id ?: return
    Config.user2Id ?: return

    ApiTest(
        caption = "(user1,user2)ユーザ1からユーザ2の情報を参照する"
        , path = "/api/users/show"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("userId" to Config.user2Id)
    ).run(ts)

    ApiTest(
        caption = "(user1,user2)ユーザ1からユーザ2のTLを参照する(あらかじめ2件以上登録しておいてください)"
        , path = "/api/users/notes"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("userId" to Config.user2Id)
        , checkExists = arrayOf("0.id")
        , idFinder = "0.id"
        , untilId = true
        , sinceId = true
    ).run(ts)

}

@TestSequence
suspend fun testUserRecommendation(ts : TestStatus) {
    Config.user2Id ?: return

    ApiTest(
        caption = "(user2)おすすめのユーザー一覧"
        , path = "/api/users/recommendation"
        , accessToken = Config.user2AccessToken
        , checkExists = arrayOf("0.id", "0.username")
        , offset = true
    ).run(ts)

}

@TestSequence
suspend fun testUserFrequentlyReplied(ts : TestStatus) {
    Config.user2Id ?: return

    ApiTest(
        caption = "よくリプライするユーザ"
        , path = "/api/users/get_frequently_replied_users"
        , params = jsonObject("userId" to Config.user2Id)
        , checkExists = arrayOf("0.weight", "0.user.id", "0.user.username")
    ).run(ts)
}

@TestSequence
suspend fun testUserSearch(ts : TestStatus) {

    ApiTest(
        caption = "ユーザを検索"
        , path = "/api/users/search"
        , params = jsonObject("query" to "tateisu")
        , checkExists = arrayOf("0.id", "0.username")
        , offset = true
    ).run(ts)
}

@TestSequence
suspend fun testUsers(ts : TestStatus) {
    Config.user2Id ?: return

    ApiTest(
        caption = "ユーザー一覧"
        , path = "/api/users"
        , params = jsonObject("sort" to "+follower")
        , checkExists = arrayOf("0.id", "0.username")
        , offset = true
    ).run(ts)
}
