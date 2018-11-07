package jp.juggler.testmisskeyapi.tests

import jp.juggler.testmisskeyapi.ApiTest
import jp.juggler.testmisskeyapi.Config
import jp.juggler.testmisskeyapi.TestStatus
import jp.juggler.testmisskeyapi.utils.TestSequence

@TestSequence
suspend fun testNotification1(ts : TestStatus) {

    Config.user1Id ?: return

    ApiTest(
        caption = "(user1)通知タイムライン"
        , path = "/api/i/notifications"
        , accessToken = Config.user1AccessToken
        , checkExists = arrayOf("0.id")
        , idFinder = "0.id"
        , untilId = true
        , sinceId = true
    ).run(ts)

    ApiTest(
        caption = "(user1)通知を全て既読にする"
        , path = "/api/notifications/mark_all_as_read"
        , accessToken = Config.user1AccessToken
        // 204 No Content
    ).run(ts)
}
