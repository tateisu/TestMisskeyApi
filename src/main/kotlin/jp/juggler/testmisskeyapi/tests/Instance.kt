package jp.juggler.testmisskeyapi.tests

import jp.juggler.testmisskeyapi.ApiTest
import jp.juggler.testmisskeyapi.TestStatus
import jp.juggler.testmisskeyapi.utils.TestSequence
import jp.juggler.testmisskeyapi.utils.jsonObject


@TestSequence
suspend fun testInstance(ts : TestStatus) {

    ApiTest(
        caption = "インスタンス情報"
        , path = "/api/meta"
        , checkExists = arrayOf("version", "clientVersion", "maxNoteTextLength")
    ).run(ts)
}

@TestSequence
suspend fun testUsernameAvailable(ts : TestStatus) {

    ApiTest(
        caption = "ユーザ名が使われてるかどうか"
        , path = "/api/username/available"
        , params = jsonObject("username" to "tateisu")
        , checkExists = arrayOf("available")
    ).run(ts)
}

@TestSequence
suspend fun testStats(ts : TestStatus) {

    ApiTest(
        caption = "インスタンスの統計情報"
        , path = "/api/stats",
        checkExists = arrayOf(
            "notesCount"
            , "usersCount"
            , "originalNotesCount"
            , "originalUsersCount"
            , "driveUsageLocal"
            , "driveUsageRemote"
            , "instances"
        )
    ).run(ts)
}