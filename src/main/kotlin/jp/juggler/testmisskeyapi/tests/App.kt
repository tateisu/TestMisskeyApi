package jp.juggler.testmisskeyapi.tests

import jp.juggler.testmisskeyapi.ApiTest
import jp.juggler.testmisskeyapi.Config
import jp.juggler.testmisskeyapi.TestStatus
import jp.juggler.testmisskeyapi.utils.TestSequence
import jp.juggler.testmisskeyapi.utils.jsonObject
import jp.juggler.testmisskeyapi.utils.lookupSimple

@TestSequence
suspend fun testApp(ts : TestStatus) {

    var appId : Any? = null
    var appSecret : Any? = null

    ApiTest(
        caption = "アプリケーション登録(非ログイン状態)",
        path = "/api/app/create",
        params = jsonObject(
            "name" to "TestMisskeyApi",
            "description" to "APIの試験です。このアプリは実際には使われません。",
            "callbackUrl" to "testmisskeyapi://unused_url/",
            "permission" to Config.permissionArray
        ),
        checkExists = arrayOf("id", "name", "secret"),
        after = {
            appId = it.lookupSimple("id")
            appSecret = it.lookupSimple("secret")
        }
    ).run(ts)

    if (appId != null) {
        ApiTest(
            caption = "アプリケーション情報"
            , path = "/api/app/show"
            , params = jsonObject("appId" to appId)
            , checkExists = arrayOf("id", "name")
        ).run(ts)

    }
    if (appSecret != null) {

        var token : Any? = null

        ApiTest(
            caption = "認証セッション開始"
            , path = "/api/auth/session/generate"
            , params = jsonObject("appSecret" to appSecret)
            , checkExists = arrayOf("token", "url"),
            after = {
                token = it.lookupSimple("token")
            }
        ).run(ts)

        if (token != null) {

            ApiTest(
                caption = "認証セッション表示",
                path = "/api/auth/session/show",
                params = jsonObject("token" to token),
                checkExists = arrayOf(
                    if(Config.apiVersion >= 11){
                        "app.id"
                    }else{
                        "appId"
                    },
                    "token",
                    "app.name"
                )
            ).run(ts)

            if (Config.user1Id != null) {
                ApiTest(
                    caption = "認証セッションの連携許可"
                    , path = "/api/auth/accept"
                    , params = jsonObject("token" to token)
                    , accessToken = Config.user1AccessToken
                    ,check204 = true
                ).run(ts)

                ApiTest(
                    caption = "認証セッション完了"
                    , path = "/api/auth/session/userkey"
                    , params = jsonObject("token" to token, "appSecret" to appSecret)
                    , checkExists = arrayOf("accessToken", "user.id")
                ).run(ts)
            }
        }
    }
}

@TestSequence
suspend fun testAppUser1(ts : TestStatus) {

    Config.user1Id ?: return

    ApiTest(
        caption = "(user1)アプリケーション登録(ログイン状態)"
        , path = "/api/app/create"
        , accessToken = Config.user1AccessToken,
        params = jsonObject(
            "name" to "TestMisskeyApi",
            "description" to "APIの試験です。このアプリは実際には使われません。",
            "callbackUrl" to "testmisskeyapi://unused_url/",
            "permission" to Config.permissionArray
        )
        , checkExists = arrayOf("id", "name", "secret")
    ).run(ts)

    ApiTest(
        caption = "(user1)ユーザが作成したアプリ"
        , path = "/api/my/apps"
        , accessToken = Config.user1AccessToken
        , checkExists = arrayOf("0.id", "0.name")
    ).run(ts)
}

@TestSequence
suspend fun testAppUser2(ts : TestStatus) {

    Config.user2Id ?: return

    ApiTest(
        caption = "(user2)連携済みアプリ(WebUIのアクセストークンでは成功する)"
        , path = "/api/i/authorized_apps"
        , accessToken = Config.user2AccessToken
        , checkExists = arrayOf("0.id", "0.name")
    ).run(ts)
}
