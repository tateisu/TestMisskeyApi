package jp.juggler.testmisskeyapi

import awaitByteArrayResponse
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonBase
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Blob
import com.github.kittinunf.fuel.core.Method
import jp.juggler.testmisskeyapi.utils.*
import kotlinx.coroutines.Dispatchers
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.security.MessageDigest
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ApiTest(

    // 説明文
    private val caption : String,

    // URLのpath部分
    private val path : String,

    // リクエストパラメータ
    private val params : JsonObject = JsonObject(),

    // リクエストパラメータに追加されるアクセストークン
    private val accessToken : String? = null,

    // エラーがなくてもレスポンスの内容を出力する
    private val dump : Boolean = false,

    // レスポンスの検証に使うデータパスの配列を指定する
    private val checkExists : Array<String>? = null,

    // レスポンスが204 no content を返すことを期待する
    private val check204 : Boolean = false,

    // レスポンスが空のリストでもエラーを出力しない
    private val allowEmptyList : Boolean = false,

    // レスポンスを使って何かしたい時にこのコールバックを利用する
    private val after : (JsonBase) -> Unit = {},

    // エラー時のレスポンスボディがこの正規表現にマッチするなら、エラーを出力しない
    private val ignoreError : Regex? = null,

    // 画像アップロードを行う際に必要なパラメータ
    private val uploadName : String? = null,
    private val uploadData : ByteArray? = null,
    private val uploadFileName : String? = null,
    private val uploadMimeType : String? = null,

    // untilId,sinceId,untilDate を使ったページングを行う際、IDをlookupするデータパス
    private val idFinder : String? = null,

    // untilId,sinceId を使ったページングを行うなら真
    private val untilId : Boolean = false,
    private val sinceId : Boolean = false,

    // untilDate を使ったページングを行うなら真
    private val untilDate : Boolean = false,

    // cursor を使ったページングを試すなら真
    private val cursor : Boolean = false,

    // offset を使ったページングを試すなら真
    private val offset : Boolean = false
) {

    companion object {

        private val userAgent = "TestMisskeyApi"

        // jsonを文字列化した後にアクセストークンの情報を隠す
        private val reSafeJson = "\"(i|currentPassword|newPassword)\":\"[^\"]+\"".toRegex()

        // テストごとにテストナンバーを発行することで、繰り返し呼び出す場合にキャッシュキーを変える
        private var testNumSeed = HashMap<String, Int>()

        private fun getTestNum(key : String) : Int {
            synchronized(testNumSeed) {
                val v = 1 + (testNumSeed[key] ?: 0)
                testNumSeed[key] = v
                return v
            }
        }
    }

    private val coroutineContext = App.coroutineContext

    private lateinit var log : LogTagBase
    private lateinit var tsName : String

    // HTTPリクエストの結果をキャッシュから読むか、または実際にサーバから取得する。
    private suspend fun cachedRequest(
        path : String,
        url : String,
        jsonParams : String
    ) : CachedRequest {

        val safeJsonParams = reSafeJson.replace(jsonParams, "\"\$1\":\"**\"")
        val sb = StringBuilder()
        sb.append(url)
        sb.append("脂")
        sb.append(jsonParams)
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(sb.toString().toByteArray())
            .toHex()
        val safePath = path.replace('/', '-').trim { it == '-' || it <= ' ' }

        val testNum = getTestNum(tsName)
        val file = File(Config.cacheDir, "$tsName-$testNum-$safePath-$digest")

        try {
            val item = CachedRequest.readFile(file)
            if (item != null) {
                log.i("CACHED $url $safeJsonParams")
                item.dumpStatus(log, ignoreError = ignoreError)
                return item
            }
        } catch (ex : FileNotFoundException) {
            // don't report
        } catch (ex : Throwable) {
            ex.printStackTrace()
            log.w("cache read failed. : ${ex.javaClass.simpleName} ${ex.message}")
        }


        log.i("POST $url $safeJsonParams")
        val timeStart = System.currentTimeMillis()
        val item : CachedRequest = try {

            val (_, res, result) = when {
                uploadData != null -> {
                    val formData = ArrayList<Pair<String, String>>()
                    for ((k, v) in params.entries) {
                        formData.add(Pair(k, v.toString()))
                    }

                    Fuel.upload(url, Method.POST, formData)
                        .header("user-agent" to userAgent)
                        .timeout(30000)
                        .timeoutRead(30000)
                        .blob { request, _ ->
                            request.names.add(uploadName !!)
                            request.mediaTypes.add(uploadMimeType !!)
                            Blob(uploadFileName !!, uploadData.size.toLong()) {
                                ByteArrayInputStream(uploadData)
                            }
                        }
                        .awaitByteArrayResponse(Dispatchers.IO + coroutineContext)
                }
                else -> Fuel.request(method = Method.POST, path = url)
                    .timeout(30000)
                    .timeoutRead(30000)
                    .header("user-agent" to userAgent)
                    .header("content-type" to "application/json; charset=UTF-8")
                    .body(jsonParams.toByteArray(Charsets.UTF_8))
                    .awaitByteArrayResponse(Dispatchers.IO + coroutineContext)
            }

            val (content, error) = result
            if (error != null) {
                val strContent = error.errorData.toString(Charsets.UTF_8)

                // HTTPのエラー応答など
                CachedRequest(
                    url = url,
                    jsonParams = jsonParams,
                    time = System.currentTimeMillis() - timeStart,
                    error = "HTTP ERROR ${error.message} $url $strContent"
                )
            } else {
                val strContent = content?.toString(Charsets.UTF_8) ?: "" // may empty
                CachedRequest(
                    url = url,
                    jsonParams = jsonParams,
                    time = System.currentTimeMillis() - timeStart,
                    content = strContent,
                    status = "${res.statusCode} ${res.responseMessage} content_size=${content?.size}"
                )
            }
        } catch (ex : Throwable) {
            // java.net.UnknownHostException など
            CachedRequest(
                url = url,
                jsonParams = jsonParams,
                time = System.currentTimeMillis() - timeStart,
                error = "NETWORK ERROR: ${ex.message} $url"
            )
        }
        item.writeFile(file)

        item.dumpStatus(log, ignoreError = ignoreError)
        return item
    }


    // レスポンスの内容をチェックする。
    // 何か問題があればnullを返す。でなければ レスポンスボディをパースしたJsonBaseを返す。
    private fun checkResult(cr : CachedRequest) : JsonBase? {

        // missing response.
        val content = cr.content ?: return null

        val root = try {
            val parser = Parser()
            when {
                content.isEmpty() -> JsonObject() // 204 no content
                content[0] == '{' -> parser.parse(StringBuilder(content)) as JsonObject
                content[0] == '[' -> parser.parse(StringBuilder(content)) as JsonArray<*>
                else -> jsonObject("content" to content)
            }
        } catch (ex : Throwable) {
            log.e("json parse failed. $ex content=$content")
            return null
        }

        // 空リストが許可されているならチェックもafterもページングも行わない
        if (allowEmptyList && root is JsonArray<*> && root.isEmpty()) {
            log.i("empty list is allowed.")
            return root
        }

        if (check204) {
            return if (root is JsonObject && root.isEmpty()) {
                root
            } else {
                log.e("response is not 204?. ${cr.status} content=$content")
                null
            }
        }

        var forceDump = false
        checkExists?.forEach { checkPath ->
            try {
                val data = root.lookupSimple(checkPath) ?: error("null")
                log.i("checkExists $checkPath = $data")
            } catch (ex : Throwable) {
                log.e("checkExists $checkPath $ex")
                forceDump = true
            }
        }

        if (forceDump || dump || Config.dumpAll) log.i(root.toJsonString(prettyPrint = true))

        return root
    }

    // ページングに使うIdを読み取る
    private fun parseOrderId(root : JsonBase) : Any? {
        return if (idFinder == null) {
            log.e("missing idFinder for paging.")
            null
        } else try {
            root.lookupSimple(idFinder)
        } catch (ex : Throwable) {
            log.e("missing id for paging.")
            null
        }
    }

    private fun ZonedDateTime.toEpochMillisecond() : Long =
        (toLocalDate().toEpochDay() * 86400000L
            + toLocalTime().toSecondOfDay().toLong() *1000L
            + (toLocalTime().nano.toLong() / 1000000L)
            - offset.totalSeconds.toLong()
            )

    // ページングに使うIdを読み取る
    private fun parseDate(root : JsonBase) : Any? {
        return if (idFinder == null) {
            log.e("missing idFinder for paging.")
            null
        } else try {
            // 2018-12-19T06:50:58.000Z
            val v = root.lookupSimple(idFinder)
            if (v !is String) return v
            val zdt = ZonedDateTime.parse(v, DateTimeFormatter.ISO_ZONED_DATE_TIME)
            return zdt.toEpochMillisecond()
        } catch (ex : Throwable) {
            log.e("missing id for paging.")
            null
        }
    }

    // ページングに使うIdを読み取る
    private fun parseCursorId(root : JsonBase, key : String) : Any? {
        return try {
            root.lookupSimple(key)
        } catch (ex : Throwable) {
            log.e("missing id for paging.")
            null
        }
    }

    // あるAPIを1回または数回(ページング)呼び出して結果をログに出力する
    suspend fun run(log : LogTagBase, name : String) {

        this.log = log
        this.tsName = name

        log.i("#start $caption $path")

        if (! params.containsKey("limit")) {
            params["limit"] = 1
        }

        if (accessToken != null) {
            params["i"] = accessToken
        }

        var cr = cachedRequest(path, "https://${Config.instance}$path", params.toJsonString(canonical = true))
        var root = checkResult(cr) ?: return

        try {
            after(root)
        } catch (ex : Throwable) {
            ex.printStackTrace()
            log.e("after process failed. $ex")
        }

        // various paging
        if (untilId) {
            cr = cachedRequest(
                path,
                "https://${Config.instance}$path",
                params.shallowClone().apply {
                    put("untilId", parseOrderId(root) ?: return)
                }.toJsonString(canonical = true)
            )
            root = checkResult(cr) ?: return

            if (sinceId) {
                cr = cachedRequest(
                    path,
                    "https://${Config.instance}$path",
                    params.shallowClone().apply {
                        put("sinceId", parseOrderId(root) ?: return)
                    }.toJsonString(canonical = true)
                )
                checkResult(cr)
            }
        } else if (untilDate) {
            val params2 = params.shallowClone()
            params2["untilDate"] = parseDate(root) ?: return
            cr = cachedRequest(path, "https://${Config.instance}$path", params2.toJsonString(canonical = true))
            checkResult(cr)
        } else if (cursor) {
            val params2 = params.shallowClone()
            params2["cursor"] = parseCursorId(root, "next") ?: return
            cr = cachedRequest(path, "https://${Config.instance}$path", params2.toJsonString(canonical = true))
            checkResult(cr)
            // cursor には "next" はあるが "prev" はない
        } else if (offset) {
            val params2 = params.shallowClone()
            params2["offset"] = (root as JsonArray<*>).size
            cr = cachedRequest(path, "https://${Config.instance}$path", params2.toJsonString(canonical = true))
            checkResult(cr)
        }
    }

    suspend fun run(ts : TestStatus) {
        run(ts.log, ts.name)
    }
}