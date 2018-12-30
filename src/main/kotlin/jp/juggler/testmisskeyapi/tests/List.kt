package jp.juggler.testmisskeyapi.tests

import jp.juggler.testmisskeyapi.ApiTest
import jp.juggler.testmisskeyapi.Config
import jp.juggler.testmisskeyapi.TestStatus
import jp.juggler.testmisskeyapi.utils.TestSequence
import jp.juggler.testmisskeyapi.utils.jsonObject
import jp.juggler.testmisskeyapi.utils.lookupSimple

@TestSequence
suspend fun testListList(ts : TestStatus) {
    Config.user2Id ?: return

    var listId : Any? = null
    ApiTest(
        caption = "(user2)リストの一覧"
        , path = "/api/users/lists/list"
        , accessToken = Config.user2AccessToken
        , checkExists = arrayOf("0.id", "0.title")
        , after = { listId = it.lookupSimple("0.id") }
    ).run(ts)

    if (listId != null) {
        ApiTest(
            caption = "(user2)リストのTL"
            , path = "/api/notes/user-list-timeline"
            , accessToken = Config.user2AccessToken,
            params = jsonObject("listId" to listId)
            , checkExists = arrayOf("0.id", "0.user.username")
            , idFinder = "0.id"
            , sinceId = true
            , untilId = true
        ).run(ts)
    }
}

@TestSequence
suspend fun testListAction(ts : TestStatus) {
    Config.user1Id ?: return
    Config.user2Id ?: return

    var listId : Any? = null
    ApiTest(
        caption = "(user1)リストの作成"
        , path = "/api/users/lists/create"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("title" to "TestMisskeyApi")
        , checkExists = arrayOf("id", "title", "userIds")
        , after = { listId = it.lookupSimple("id") }
    ).run(ts)

    listId ?: return

    ApiTest(
        caption = "(user1)リストにユーザーを追加"
        , path = "/api/users/lists/push"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("listId" to listId, "userId" to Config.user2Id)
        ,check204 = true
    ).run(ts)

    ApiTest(
        caption = "(user1)リストからユーザーを削除"
        , path = "/api/users/lists/pull"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("listId" to listId, "userId" to Config.user2Id)
        ,check204 = true
    ).run(ts)

    ApiTest(
        caption = "(user1)リストのタイトル変更"
        , path = "/api/users/lists/update"
        , accessToken = Config.user1AccessToken
        , checkExists = arrayOf("id", "title", "userIds.0")
        , params = jsonObject("listId" to listId, "title" to "TestMisskeyApi2")
    ).run(ts)


    ApiTest(
        caption = "(user1)リストの情報を表示"
        , path = "/api/users/lists/show"
        , accessToken = Config.user1AccessToken
        , checkExists = arrayOf("id", "title", "userIds.0")
        , params = jsonObject("listId" to listId)
    ).run(ts)

    ApiTest(
        caption = "(user1)リストの削除"
        , path = "/api/users/lists/delete"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("listId" to listId)
        ,check204 = true
    ).run(ts)
}
