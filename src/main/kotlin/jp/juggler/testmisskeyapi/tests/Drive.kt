package jp.juggler.testmisskeyapi.tests

import jp.juggler.testmisskeyapi.ApiTest
import jp.juggler.testmisskeyapi.Config
import jp.juggler.testmisskeyapi.TestStatus
import jp.juggler.testmisskeyapi.utils.TestSequence
import jp.juggler.testmisskeyapi.utils.jsonArray
import jp.juggler.testmisskeyapi.utils.jsonObject
import jp.juggler.testmisskeyapi.utils.lookupSimple

@TestSequence
suspend fun testDrive1(ts : TestStatus) {

    Config.user1Id ?: return

    ///////////////////////////////////////////////////
    // ドライブの操作

    var folderId : Any? = null
    val folderName = "ApiTestFolder"

    ApiTest(
        caption = "(user1)ドライブのフォルダを作成"
        , path = "/api/drive/folders/create"
        , accessToken = Config.user1AccessToken
        , params = jsonObject("name" to folderName)
        , checkExists = arrayOf("id", "name")
        , after = { folderId = it.lookupSimple("id") }
    ).run(ts)

    if (folderId != null) {

        ApiTest(
            caption = "(user1)ドライブのフォルダの更新"
            , path = "/api/drive/folders/update"
            , accessToken = Config.user1AccessToken
            , params = jsonObject("folderId" to folderId, "name" to folderName)
            , checkExists = arrayOf("id", "name")
        ).run(ts)

        if (Config.jpegSample != null) {

            val fileName = "ApiTestFile.jpg"

            var fileUrl : Any? = null
            var fileId : Any? = null
            var md5 : Any? = null

            ApiTest(
                caption = "(user1)ドライブのファイルを作成"
                , path = "/api/drive/files/create"
                , accessToken = Config.user1AccessToken
                , params = jsonObject("force" to true, "folderId" to folderId)
                , uploadName = "file"
                , uploadData = Config.jpegSample
                , uploadFileName = fileName
                , uploadMimeType = "image/jpeg"
                , checkExists = arrayOf("id", "url", "md5", "name", "type", "datasize"),
                after = {
                    fileId = it.lookupSimple("id")
                    fileUrl = it.lookupSimple("url")
                    md5 = it.lookupSimple("md5")
                }
            ).run(ts)

            var fileId2 : Any? = null

            if (fileUrl != null) {

                ApiTest(
                    caption = "(user1)ドライブのファイルの作成(URL指定)"
                    , path = "/api/drive/files/upload_from_url"
                    , accessToken = Config.user1AccessToken
                    , params = jsonObject("url" to fileUrl, "folderId" to folderId)
                    , checkExists = arrayOf("id", "url", "md5", "name", "type", "datasize")
                    , after = { fileId2 = it.lookupSimple("id") }
                ).run(ts)

                // /drive/files/upload_from_url には force とかisSensitive とかないので
                // md5が一致するデータがあればそれの情報を返す
                // この例だと fileId2 と fileId は同じになる
                // fileId を使うテストが全て終わってから fileId2を削除する
            }

            if (md5 != null) {
                ApiTest(
                    caption = "(user1)ドライブのファイルをMD5ダイジェストで探す"
                    , path = "/api/drive/files/check_existence"
                    , accessToken = Config.user1AccessToken
                    , params = jsonObject("md5" to md5)
                    , checkExists = arrayOf("file.id")
                ).run(ts)
            }

            if (fileId != null) {

                ApiTest(
                    caption = "(user1)ドライブのファイルの更新"
                    , path = "/api/drive/files/update"
                    , accessToken = Config.user1AccessToken
                    , params = jsonObject("fileId" to fileId, "name" to fileName)
                    , checkExists = arrayOf("id", "url", "md5", "name", "type", "datasize")
                ).run(ts)

                ApiTest(
                    caption = "(user1)ドライブのファイルの情報"
                    , path = "/api/drive/files/show"
                    , accessToken = Config.user1AccessToken
                    , params = jsonObject("fileId" to fileId)
                    , checkExists = arrayOf("id", "url", "md5", "name", "type", "datasize")
                ).run(ts)

                ApiTest(
                    caption = "(user1)画像つき投稿"
                    , path = "/api/notes/create"
                    , accessToken = Config.user1AccessToken
                    , params = jsonObject(
                    "text" to "test",
                    "visibility" to "home",
                    "fileIds" to jsonArray(fileId)
                )
                    , checkExists = arrayOf("createdNote.media.0.id")
                ).run(ts)

                ApiTest(
                    caption = "(user1)ドライブのファイルが使われた投稿の一覧"
                    , path = "/api/drive/files/attached_notes"
                    , accessToken = Config.user1AccessToken
                    , params = jsonObject("fileId" to fileId)
                    , checkExists = arrayOf("0.id")
                ).run(ts)


                ApiTest(
                    caption = "(user1)ドライブのファイルを名前で探す"
                    , path = "/api/drive/files/find"
                    , accessToken = Config.user1AccessToken
                    , params = jsonObject("name" to fileName)
                    , checkExists = arrayOf("0.id")
                ).run(ts)

                ApiTest(
                    caption = "(user1)ドライブのファイルを削除"
                    , path = "/api/drive/files/delete"
                    , accessToken = Config.user1AccessToken
                    , params = jsonObject("fileId" to fileId)
                    // 204 No Content
                ).run(ts)
            }

            if (fileId2 != null) {
                ApiTest(
                    caption = "(user1)ドライブのファイルを削除"
                    , path = "/api/drive/files/delete"
                    , accessToken = Config.user1AccessToken
                    , params = jsonObject("fileId" to fileId2)
                    // 204 No Content
                ).run(ts)

                // fileId とfileId2 は同じID になっているので
                // 同一IDに削除を2回呼び出しているが、どちらも204を返すようだ
            }

        }

        ApiTest(
            caption = "(user1)ドライブのフォルダの情報"
            , path = "/api/drive/folders/show"
            , accessToken = Config.user1AccessToken
            , params = jsonObject("folderId" to folderId)
            , checkExists = arrayOf("id", "name")
        ).run(ts)

        ApiTest(
            caption = "(user1)ドライブのフォルダを探す"
            , path = "/api/drive/folders/find"
            , accessToken = Config.user1AccessToken
            , params = jsonObject("name" to folderName)
            , checkExists = arrayOf("0.id", "0.name")
        ).run(ts)

        ApiTest(
            caption = "(user1)ドライブのフォルダを削除"
            , path = "/api/drive/folders/delete"
            , accessToken = Config.user1AccessToken
            , params = jsonObject("folderId" to folderId)
            // 204 no content
        ).run(ts)
    }

}

@TestSequence
suspend fun testDriveShow(ts : TestStatus) {
    Config.user2Id ?: return

    ApiTest(
        caption = "(user2)ドライブの情報"
        , path = "/api/drive"
        , accessToken = Config.user2AccessToken
        , params = jsonObject("userId" to Config.user2Id)
        , checkExists = arrayOf("capacity", "usage")
    ).run(ts)

    ApiTest(
        caption = "(user2)ドライブのルートフォルダのファイル一覧"
        , path = "/api/drive/files"
        , accessToken = Config.user2AccessToken
        , idFinder = "0.id"
        , sinceId = true
        , untilId = true
        , checkExists = arrayOf("0.id", "0.name", "0.url")
    ).run(ts)

    ApiTest(
        caption = "(user2)ドライブのフォルダ一覧"
        , path = "/api/drive/folders"
        , accessToken = Config.user2AccessToken
        , idFinder = "0.id"
        , sinceId = true
        , untilId = true
        , checkExists = arrayOf("0.id", "0.name")
    ).run(ts)

    ApiTest(
        caption = "(user2)ドライブのファイル一覧(更新順)"
        , path = "/api/drive/stream"
        , accessToken = Config.user2AccessToken
        , idFinder = "0.id"
        , sinceId = true
        , untilId = true
        , checkExists = arrayOf("0.id", "0.name", "0.url")
    ).run(ts)

}
