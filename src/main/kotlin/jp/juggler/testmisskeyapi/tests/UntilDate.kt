package jp.juggler.testmisskeyapi.tests

import jp.juggler.testmisskeyapi.ApiTest
import jp.juggler.testmisskeyapi.TestStatus
import jp.juggler.testmisskeyapi.utils.TestSequence


@TestSequence
suspend fun testUntilDate(ts : TestStatus) {

    ApiTest(
        caption = "グローバルTL(Date順)"
        , path = "/api/notes/global-timeline"
        , checkExists = arrayOf("0.id", "0.user.username","0.createdAt")
        , idFinder = "0.createdAt"
        , untilDate = true
    // TODO: sinceDateもテストする？    , sinceId = true
    ).run(ts)
}