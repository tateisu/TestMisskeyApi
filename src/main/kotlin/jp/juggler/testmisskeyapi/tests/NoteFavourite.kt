package jp.juggler.testmisskeyapi.tests

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import jp.juggler.testmisskeyapi.ApiTest
import jp.juggler.testmisskeyapi.Config
import jp.juggler.testmisskeyapi.TestStatus
import jp.juggler.testmisskeyapi.utils.TestSequence
import jp.juggler.testmisskeyapi.utils.jsonObject

@TestSequence
suspend fun testNoteFavourite(ts : TestStatus) {

    // グローバルTLを読んで投稿IDを取得してからお気に入りの追加と削除を行う

    var favNoteId : Any? = null

    ApiTest(
        caption = "グローバルTL"
        , path = "/api/notes/global-timeline"
        , params = jsonObject("limit" to 40)
        , checkExists = arrayOf("0.id")
        , idFinder = "0.id"
        , untilId = true
        , sinceId = true,
        after = { root ->
            for (item in root as JsonArray<*>) {
                if (item is JsonObject) {
                    if (item["renoteId"] != null) continue
                    if (item["replyId"] != null) continue
                    favNoteId = item["id"]
                    break
                }
            }
        }
    ).run(ts)

    if (favNoteId == null) {
        ts.log.e("missing note id to test favorite")
        return
    }

    Config.user1Id ?: return

    ApiTest(
        caption = "(user1)お気に入りの追加"
        , path = "/api/notes/favorites/create"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("noteId" to favNoteId)
    ).run(ts)

    ApiTest(
        caption = "(user1)お気に入りの削除"
        , path = "/api/notes/favorites/delete"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("noteId" to favNoteId)
    ).run(ts)
}