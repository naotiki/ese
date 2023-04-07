# Ese Linux
Unixシェルを模した動作をするアプリ
# インストール
[Release](https://github.com/naotiki/EseLinux/releases/latest)からダウンロードできます。
# Web版
[https://ese.naotiki.me](https://ese.naotiki.me)

※初回アクセス時、約15MBの通信を行います。
## モジュール
### `client-cui` (JVM)
Ese LinuxのCUIクライアントアプリ
### `client-gui` (JVM/JS)
Ese LinuxのGUIクライアントアプリ
### `ese-core` (JVM/JS)
Ese Linuxのコアライブラリ

# ビルド方法
まず、`client-gui`フォルダに移動
```shell
cd client-gui
```
---
## デスクトップクライアントを実行
タスク`run`を実行
```shell
./gradlew run
```
## Webクライアントを実行 (Experimental)
タスク`jsBrowserDevelopmentRun`を実行
```shell
./gradlew jsBrowserDevelopmentRun
```
## パッケージング
タスク`superReleaseBuild`を実行
`vX.X.X-TEXT`の形式で`<APP_VERSION>`を指定できます。

例:`-PappVersion=v0.9.0-beta`
指定されなければ`v0.0.0-dev`で実行されます。
```shell
./gradlew superReleaseBuild -PappVersion=<APP_VERSION>
```