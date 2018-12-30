package jp.juggler.testmisskeyapi.tests

import jp.juggler.testmisskeyapi.ApiTest
import jp.juggler.testmisskeyapi.Config
import jp.juggler.testmisskeyapi.TestStatus
import jp.juggler.testmisskeyapi.utils.TestSequence
import jp.juggler.testmisskeyapi.utils.jsonArray
import jp.juggler.testmisskeyapi.utils.jsonObject
import jp.juggler.testmisskeyapi.utils.lookupSimple

@TestSequence
suspend fun testNotePublic(ts : TestStatus) {

    ApiTest(
        caption = "投稿一覧"
        , path = "/api/notes"
        , checkExists = arrayOf("0.id", "0.user.username")
        , idFinder = "0.id"
        , sinceId = true
        , untilId = true
    ).run(ts)

    ApiTest(
        caption = "Featuredな投稿"
        , path = "/api/notes/featured"
        , checkExists = arrayOf("0.id", "0.user.username")
    ).run(ts)

    ApiTest(
        caption = "投稿を検索"
        , path = "/api/notes/search"
        , params = jsonObject("query" to "SubwayTooter")
        , checkExists = arrayOf("0.id", "0.user.username")
        , offset = true
    ).run(ts)

}

@TestSequence
suspend fun testNoteUser1(ts : TestStatus) {
    Config.user1Id ?: return

    ApiTest(
        caption = "(user1)未読のクリア"
        , path = "/api/i/read_all_unread_notes"
        , accessToken = Config.user1AccessToken
        , check204 = true
    ).run(ts)
}


@TestSequence
suspend fun testNotePin(ts : TestStatus) {

    Config.user1Id ?: return

    /////////////
    // 投稿してピンをつけ外ししてから削除する

    var createdNoteId : Any? = null

    ApiTest(
        caption = "(user1)投稿",
        path = "/api/notes/create",
        params = jsonObject(
            "text" to "APIテスト用のダミー投稿です。公開範囲はhomeです。",
            "visibility" to "home",
            "poll" to jsonObject(
                "choices" to jsonArray("a", "b", "c")
            )
        ),
        accessToken = Config.user1AccessToken,
        checkExists = arrayOf("createdNote.id"),
        after = { createdNoteId = it.lookupSimple("createdNote.id") }
    ).run(ts)

    if (createdNoteId != null) {

        ApiTest(
            caption = "(user1)投稿の表示"
            , path = "/api/notes/show"
            , params = jsonObject("noteId" to createdNoteId)
            , accessToken = Config.user1AccessToken
            , checkExists = arrayOf("id", "user.username")
        ).run(ts)

        ApiTest(
            caption = "(user1)投稿のピンを設定"
            , path = "/api/i/pin"
            , accessToken = Config.user1AccessToken
            , params = jsonObject("noteId" to createdNoteId)
            // User entity を返すが、なぜかpin投稿が含まれない
        ).run(ts)

        ApiTest(
            caption = "(user1)ピンされた投稿"
            , path = "/api/users/show"
            , accessToken = Config.user1AccessToken
            , params = jsonObject("userId" to Config.user1Id)
            , checkExists = arrayOf("pinnedNotes.0.id")
        ).run(ts)

        ApiTest(
            caption = "(user1)投稿のピンを解除"
            , path = "/api/i/unpin"
            , accessToken = Config.user1AccessToken
            , params = jsonObject("noteId" to createdNoteId)
            , checkExists = arrayOf("id")
            // User entity を返すが、なぜか削除されるまえのpin投稿を含む
        ).run(ts)


        if (Config.user2Id != null && Config.user3Id != null) {

            ApiTest(
                caption = "(user2)リアクション"
                , path = "/api/notes/reactions/create"
                , accessToken = Config.user2AccessToken
                , params = jsonObject("noteId" to createdNoteId, "reaction" to "hmm")
                , check204 = true
            ).run(ts)

            ApiTest(
                caption = "(user3)リアクション"
                , path = "/api/notes/reactions/create"
                , accessToken = Config.user3AccessToken
                , params = jsonObject("noteId" to createdNoteId, "reaction" to "hmm")
                , check204 = true
            ).run(ts)

            ApiTest(
                caption = "(user1)リアクションの一覧"
                , path = "/api/notes/reactions"
                , accessToken = Config.user1AccessToken
                , params = jsonObject("noteId" to createdNoteId)
                , checkExists = arrayOf("0.id", "0.reaction", "0.user.username")
                , offset = true
            ).run(ts)

            ApiTest(
                caption = "(user2)リアクションの削除"
                , path = "/api/notes/reactions/delete"
                , accessToken = Config.user2AccessToken
                , params = jsonObject("noteId" to createdNoteId)
                , check204 = true
            ).run(ts)

            ApiTest(
                caption = "(user3)リアクションの削除"
                , path = "/api/notes/reactions/delete"
                , accessToken = Config.user3AccessToken
                , params = jsonObject("noteId" to createdNoteId)
                , check204 = true
            ).run(ts)
        }

        if (Config.user3Id != null) {
            ApiTest(
                caption = "(user3)アンケートに投票"
                , path = "/api/notes/polls/vote"
                , accessToken = Config.user3AccessToken
                , params = jsonObject("noteId" to createdNoteId, "choice" to 0)
                , check204 = true
            ).run(ts)


            // 一覧を取得する時にページングできるよう、2つずつ投稿する

            for (i in 1 .. 2) {
                ApiTest(
                    caption = "(user3)renote",
                    path = "/api/notes/create",
                    params = jsonObject("visibility" to "home", "renoteId" to createdNoteId, "text" to "うーむ$i"),
                    accessToken = Config.user3AccessToken,
                    checkExists = arrayOf("createdNote.id")
                ).run(ts)

                ApiTest(
                    caption = "(user3)reply",
                    path = "/api/notes/create",
                    params = jsonObject("visibility" to "home", "replyId" to createdNoteId, "text" to "ほーん$i"),
                    accessToken = Config.user1AccessToken,
                    checkExists = arrayOf("createdNote.id")
                ).run(ts)
            }


            ApiTest(
                caption = "Renote一覧"
                , path = "/api/notes/renotes"
                , params = jsonObject("noteId" to createdNoteId)
                , checkExists = arrayOf("0.id", "0.user.username")
                , idFinder = "0.id"
                , sinceId = true
                , allowEmptyList = true // 2回目は空のリストになる
            ).run(ts)

            ApiTest(
                caption = "返信の一覧"
                , path = "/api/notes/replies"
                , params = jsonObject("noteId" to createdNoteId)
                , checkExists = arrayOf("0.id", "0.user.username")
                , offset = true
            ).run(ts)

            var conversationLast : Any = createdNoteId !!
            for (i in 1 .. 5) {
                ApiTest(
                    caption = "(user3) 会話の流れを作成",
                    path = "/api/notes/create",
                    params = jsonObject("visibility" to "home", "replyId" to conversationLast, "text" to "ぶらー$i"),
                    accessToken = Config.user1AccessToken,
                    checkExists = arrayOf("createdNote.id"),
                    after = { conversationLast = it.lookupSimple("createdNote.id") ?: conversationLast }
                ).run(ts)
            }

            ApiTest(
                caption = "投稿の文脈の表示"
                , path = "/api/notes/conversation"
                , params = jsonObject("noteId" to conversationLast)
                , checkExists = arrayOf("0.id", "0.user.username")
                , offset = true
            ).run(ts)

        }

        // このAPIはgithubにソースがない
        //        ApiTest(
        //            caption = "reposts"
        //            , path = "/api/notes/reposts"
        //            , params = jsonObject("noteId" to createdNoteId)
        //            ,offset =true
        //        ).run(ts)

        ApiTest(
            caption = "(user1)投稿の削除"
            , path = "/api/notes/delete"
            , accessToken = Config.user1AccessToken
            , params = jsonObject("noteId" to createdNoteId)
        ).run(ts)
    }

}

@TestSequence
suspend fun testNoteTimelineNonPublic(ts : TestStatus) {

    Config.user2Id ?: return

    ApiTest(
        caption = "(user2)ホームTL"
        , path = "/api/notes/timeline"
        , accessToken = Config.user2AccessToken
        , checkExists = arrayOf("0.id")
        , idFinder = "0.id"
        , untilId = true
        , sinceId = true
    ).run(ts)

    ApiTest(
        caption = "(user2)ソーシャルTL"
        , path = "/api/notes/hybrid-timeline"
        , accessToken = Config.user2AccessToken
        , checkExists = arrayOf("0.id")
        , idFinder = "0.id"
        , untilId = true
        , sinceId = true
    ).run(ts)

    ApiTest(
        caption = "(user2)お気に入りした投稿の一覧(2つ以上のお気に入りを事前に登録してください)"
        , path = "/api/i/favorites"
        , accessToken = Config.user2AccessToken
        , checkExists = arrayOf("0.id", "0.note.id")
        , idFinder = "0.id"
        , untilId = true
        , sinceId = true
    ).run(ts)

    ApiTest(
        caption = "(user2)おすすめのアンケート一覧"
        , path = "/api/notes/polls/recommendation"
        , accessToken = Config.user2AccessToken
        , checkExists = arrayOf("0.id", "0.poll.choices.0.text") // "0.text" はnullの場合がある
        , offset = true
    ).run(ts)


    ApiTest(
        caption = "(user2)自分に言及している投稿の一覧を取得します"
        , path = "/api/notes/mentions"
        , accessToken = Config.user2AccessToken
        , checkExists = arrayOf("0.id", "0.text", "0.user.username")
        , idFinder = "0.id"
        , sinceId = true
        , untilId = true
    ).run(ts)

    // このAPIはドキュメントがあるし呼べば [] を返すが、 githubにコードがなくサーバにファイルが残っているだけのようだ。
//        ApiTest(
//            caption = "trendな投稿"
//            , path = "/api/notes/trend"
//            , accessToken = Config.user2AccessToken
//            , checkExists = arrayOf("0.id", "0.text", "0.user.username")
//        ).run()

}
