# TestMisskeyApi

## 概要

Misskey https://github.com/syuilo/misskey 
の API を実際に呼び出してテストします。

## 使い方

```
cp tma.conf.sample tma.conf
emacs tma.conf
(設定項目については後述の説明を参照してください)

java -jar TestMisskeyApi.jar tma.conf (options)
```

- オプション以外の引数は設定ファイルとして扱われます。
- 設定ファイルとオプションは「コマンドラインに指定した順」適用され、同じ設定項目に対しては後から指定した内容で上書きされます。

## オプション

```
-c             テスト開始前にキャッシュファイルをクリアする。
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

## テスト記述状況
https://docs.google.com/spreadsheets/d/13eaarSkVmE7U3g5F_5PmFcgB0ilxIHVoEkwZwdeTeU0/edit?usp=sharing
