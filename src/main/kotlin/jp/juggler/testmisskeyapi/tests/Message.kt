package jp.juggler.testmisskeyapi.tests

import jp.juggler.testmisskeyapi.ApiTest
import jp.juggler.testmisskeyapi.Config
import jp.juggler.testmisskeyapi.TestStatus
import jp.juggler.testmisskeyapi.utils.TestSequence
import jp.juggler.testmisskeyapi.utils.jsonObject
import jp.juggler.testmisskeyapi.utils.lookupSimple

@TestSequence
suspend fun testMessage(ts : TestStatus) {

    Config.user1Id ?: return
    Config.user2Id ?: return

    ApiTest(
        caption = "(user1,user2)メッセージの送信"
        , path = "/api/messaging/messages/create"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("userId" to Config.user2Id, "text" to "がうがう1")
        , checkExists = arrayOf("id", "text", "recipient.username")
    ).run(ts)
    ApiTest(
        caption = "(user1,user2)メッセージの送信"
        , path = "/api/messaging/messages/create"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("userId" to Config.user2Id, "text" to "がうがう2")
        , checkExists = arrayOf("id", "text", "recipient.username")
    ).run(ts)

    ApiTest(
        caption = "(user2)メッセージ履歴"
        , path = "/api/messaging/history"
        , accessToken = Config.user2AccessToken
        , checkExists = arrayOf("0.id", "0.text", "0.recipient.username")
    ).run(ts)

    var messageId : Any? = null
    ApiTest(
        caption = "(user2)メッセージのユーザ別一覧"
        , path = "/api/messaging/messages"
        , accessToken = Config.user2AccessToken
        , params = jsonObject("userId" to Config.user1Id)
        , checkExists = arrayOf("0.id", "0.text", "0.recipientId", "0.user.username")
        // recipient エンティティは含まれない
        // 送信者のエンティティがuserに入る
        , idFinder = "0.id"
        , sinceId = true
        , untilId = true
        , after = { messageId = it.lookupSimple("0.id") }
    ).run(ts)

    if (messageId != null) {
        ApiTest(
            caption = "(user2)メッセージを既読にする"
            , path = "/api/messaging/messages/read"
            , accessToken = Config.user2AccessToken
            , params = jsonObject("messageId" to messageId)
            , check204 = true
        ).run(ts)
    }

}