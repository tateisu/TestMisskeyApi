package jp.juggler.testmisskeyapi.tests

import jp.juggler.testmisskeyapi.ApiTest
import jp.juggler.testmisskeyapi.Config
import jp.juggler.testmisskeyapi.TestStatus
import jp.juggler.testmisskeyapi.utils.TestSequence
import jp.juggler.testmisskeyapi.utils.jsonObject

@TestSequence
suspend fun testUserFollowAction(ts : TestStatus) {

    Config.user1Id ?: return
    Config.user3Id ?: return

    // ユーザ3からユーザ１をフォローして解除

    ApiTest(
        caption = "(user3,user1)フォロー解除(前準備)"
        , path = "/api/following/delete"
        , accessToken = Config.user3AccessToken
        , params = jsonObject("userId" to Config.user1Id)
        , ignoreError = "already not following".toRegex()
    ).run(ts)

    ApiTest(
        caption = "(user3,user1)フォロー"
        , path = "/api/following/create"
        , accessToken = Config.user3AccessToken
        , params = jsonObject("userId" to Config.user1Id)
    ).run(ts)

    ApiTest(
        caption = "(user3,user1)ストーキング"
        , path = "/api/following/stalk"
        , accessToken = Config.user3AccessToken
        , params = jsonObject("userId" to Config.user1Id)
        , check204 = true
    ).run(ts)


    ApiTest(
        caption = "(user3,user1)ストーキング解除"
        , path = "/api/following/unstalk"
        , accessToken = Config.user3AccessToken
        , params = jsonObject("userId" to Config.user1Id)
        , check204 = true
    ).run(ts)

    ApiTest(
        caption = "(user3,user1)フォロー解除"
        , path = "/api/following/delete"
        , accessToken = Config.user3AccessToken
        , params = jsonObject("userId" to Config.user1Id)
    ).run(ts)

}

@TestSequence
suspend fun testUserFollowList(ts : TestStatus) {
    Config.user2Id ?: return

    ApiTest(
        caption = "(user2)フォロワー一覧"
        , path = "/api/users/followers"
        , accessToken = Config.user2AccessToken
        , params = jsonObject("userId" to Config.user2Id)
        , checkExists = arrayOf("users.0.id", "users.0.followersCount", "users.0.isFollowing")
        , cursor = true
        // フォロワー一覧のレスポンスはユーザ関係やフォローカウントなどを含む (2018/11/6)
    ).run(ts)

    ApiTest(
        caption = "(user2)フォロー一覧"
        , path = "/api/users/following"
        , accessToken = Config.user2AccessToken
        , params = jsonObject("userId" to Config.user2Id)
        , checkExists = arrayOf("users.0.id", "users.0.followersCount", "users.0.isFollowing")
        , cursor = true
        // フォロワー一覧のレスポンスはユーザ関係やフォローカウントなどを含む (2018/11/6)
    ).run(ts)
}

@TestSequence
suspend fun testUserFollowRequest(ts : TestStatus) {
    Config.user1Id ?: return
    Config.user2Id ?: return
    Config.user3Id ?: return


    suspend fun prepareFollowRequest(id : Any?, token : String, caption : String) {
        id ?: return

        ApiTest(
            caption = "($caption,user3)フォロー解除(前準備)"
            , path = "/api/following/delete"
            , accessToken = token
            , params = jsonObject("userId" to Config.user3Id)
            , ignoreError = "already not following".toRegex()
        ).run(ts)

        ApiTest(
            caption = "($caption,user3)フォロー申請の取り下げ(前準備)"
            , path = "/api/following/requests/cancel"
            , accessToken = token
            , params = jsonObject("userId" to Config.user3Id)
            , ignoreError = "request not found".toRegex()
        ).run(ts)

        ApiTest(
            caption = "($caption,user3)フォローリクエスト"
            , path = "/api/following/create"
            , accessToken = token
            , params = jsonObject("userId" to Config.user3Id)
        ).run(ts)
    }

    ////////////////
    // 承認のテスト

    suspend fun acceptFollowRequest(targetId : Any?, caption : String) {
        targetId ?: return
        ApiTest(
            caption = "(user3,$caption)フォロー申請の承認"
            , path = "/api/following/requests/accept"
            , accessToken = Config.user3AccessToken
            , params = jsonObject("userId" to targetId)
            , check204 = true
        ).run(ts)
    }

    prepareFollowRequest(Config.user1Id, Config.user1AccessToken, "user1")

    ApiTest(
        caption = "(user3)フォローリクエスト一覧"
        , path = "/api/following/requests/list"
        , accessToken = Config.user3AccessToken
        , checkExists = arrayOf("0.id", "0.follower.id", "0.followee.id")
    ).run(ts)

    acceptFollowRequest(Config.user1Id, "user1")

    ////////////////
    // 却下のテスト

    suspend fun rejectFollowRequest(targetId : Any?, caption : String) {
        targetId ?: return
        ApiTest(
            caption = "(user3,$caption)フォロー申請の却下"
            , path = "/api/following/requests/reject"
            , accessToken = Config.user3AccessToken
            , params = jsonObject("userId" to targetId)
            , check204 = true
        ).run(ts)
    }

    prepareFollowRequest(Config.user1Id, Config.user1AccessToken, "user1")
    rejectFollowRequest(Config.user1Id, "user1")

    ////////////////
    // 取り下げのテスト

    suspend fun cancelFollowRequest(id : Any?, token : String, caption : String) {
        id ?: return

        ApiTest(
            caption = "($caption,user3)フォロー申請の取り下げ"
            , path = "/api/following/requests/cancel"
            , accessToken = token
            , params = jsonObject("userId" to Config.user3Id)
            , checkExists = arrayOf("id", "avatarUrl")
            // 詳細情報のないユーザオブジェクトを返す。何に使うのかはよくわからない 2018/11/6
        ).run(ts)
    }

    prepareFollowRequest(Config.user1Id, Config.user1AccessToken, "user1")
    cancelFollowRequest(Config.user1Id, Config.user1AccessToken, "user1")


}
