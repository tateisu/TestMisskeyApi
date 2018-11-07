# TestMisskeyApi

## 概要

Misskey https://github.com/syuilo/misskey 
の API を実際に呼び出してテストします。

## テスト記述状況
https://docs.google.com/spreadsheets/d/13eaarSkVmE7U3g5F_5PmFcgB0ilxIHVoEkwZwdeTeU0/edit?usp=sharing

## 使い方

### 設定ファイルの準備
```
cp tma.conf.sample tma.conf
emacs tma.conf
```

- 設定ファイルの文字コードはUTF-8です。
- 空行と#ではじまる行は無視されます。
- 行に (key)=(value) が書かれていれば設定項目(key)に(value)を設定します。
- 設定項目については後述

### テストの起動

```
java -jar TestMisskeyApi.jar tma.conf (options)
```
- オプション以外のコマンドライン引数は「設定ファイルの指定」として扱われます。
- 設定ファイルとオプションは「コマンドラインに指定した順」適用され、同じ設定項目に対しては後から指定した内容で上書きされます。

## コマンドラインオプション

|オプション|説明|
|-|-|
|-c|テスト開始前にキャッシュファイルをクリアする。|
|-(key)=(value)|設定項目(key)に(value)を設定する。|

## 設定項目

|キー名|値の形式|デフォルト値|説明|
|-|-|-|-|
|clearCache|boolean|false|trueならテスト開始前にキャッシュファイルをクリアする。|
|dumpAll|boolean|false|trueならレスポンス中のjsonデータを全てstdoutに出力する。|
|instance|string||インスタンス名。例えば misskex.xyz |
|user1AccessToken|string||テストに使うユーザアカウントのアクセストークン。読み書きを行うテストアカウント。|
|user2AccessToken|string||テストに使うユーザアカウントのアクセストークン。既にある程度のデータを持つアカウント。|
|user3AccessToken|string||テストに使うユーザアカウントのアクセストークン。フォロー承認制のテストアカウント。|
|user1Password|string||user1のパスワード。パスワード変更APIのテストに使う。 省略するとそのテストは行われない。|
|cacheDir|string|./cache|キャッシュデータを置くディレクトリ。|
|jpegSample|string|./sample.jpg|画像アップロードのテストに使うJPEGファイル。|

- アクセストークンはサードアプリのものではなく、WebUIの設定ダイアログにあるアクセストークンを使ってください。利用できるAPIの数が異なります。
- boolean には 文字列 true か false を指定します。
