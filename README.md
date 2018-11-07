# TestMisskeyApi

## 概要

Misskey の API を実際に呼び出してテストします。
- リクエストのURLとパラメータをキーにしてレスポンスをキャッシュするので、繰り返し実行してもタンスに負荷がかかりません。
- APIの網羅性はまだ低いです。

## 使い方

```
java -jar TestMisskeyApi.jar tma.conf (options)
```

- オプション以外の引数は設定ファイルとして扱われます
- 設定ファイルとオプションは「コマンドラインに指定した順」適用され、同じ設定項目に対しては後から指定した内容で上書きされます。

## オプション

```
-c             テスト開始前にキャッシュファイルをクリア
-(key)=(value) 設定項目(key)に(value)を設定する

```

## 設定ファイル
空行と#ではじまる行は無視されます。
行に (key)=(value) が書かれていれば設定項目(key)に(value)を設定します。

## 設定項目

- clearCache=(boolean) trueならテスト開始前にキャッシュファイルをクリアする
- dumpAll=(boolean) trueならレスポンス中のjsonデータを全て出力する
- instance=(string) インスタンス名（例えば misskex.xyz )
- user1AccessToken=(string) テストに使うユーザアカウントのアクセストークン。読み書きを行うテストアカウント。WebUIの設定ダイアログにあるアクセストークン。
- user2AccessToken=(string) テストに使うユーザアカウントのアクセストークン。既にある程度のデータを持つアカウント。WebUIの設定ダイアログにあるアクセストークン。
- user3AccessToken=(string) テストに使うユーザアカウントのアクセストークン。フォロー承認制のテストアカウント。WebUIの設定ダイアログにあるアクセストークン。
- user1Password=(string) user1のパスワード。パスワード変更APIのテストに使う。 省略可能。
- cacheDir=(string) キャッシュデータを置くディレクトリ名。省略時は ./cache
- jpegSample=(string) 画像アップロードのテストに使うJPEGファイルのファイルパス。省略時は ./sample.jpg
