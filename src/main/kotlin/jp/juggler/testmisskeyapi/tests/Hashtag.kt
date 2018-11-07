package jp.juggler.testmisskeyapi.tests

import jp.juggler.testmisskeyapi.ApiTest
import jp.juggler.testmisskeyapi.TestStatus
import jp.juggler.testmisskeyapi.utils.TestSequence
import jp.juggler.testmisskeyapi.utils.jsonObject

@TestSequence
suspend fun testHashtag(ts : TestStatus) {

    ApiTest(
        caption = "ハッシュタグ検索(前方一致、大文字小文字区別なし)"
        , path = "/api/hashtags/search"
        , params = jsonObject("query" to "Mi")
        , checkExists = arrayOf("0")
        , offset = true
    ).run(ts)

    ApiTest(
        caption = "ハッシュタグトレンド"
        , path = "/api/hashtags/trend"
        , checkExists = arrayOf("0.tag", "0.usersCount")
    ).run(ts)

    ApiTest(
        caption = "投稿をタグで検索"
        , path = "/api/notes/search_by_tag"
        , params = jsonObject("tag" to "MisskeyApi")
        , checkExists = arrayOf("0.id", "0.text")
        // 対応してない,sinceId = true
        , idFinder = "0.id"
        , untilId = true
    ).run(ts)

}