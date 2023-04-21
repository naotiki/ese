[![Deploy Kotlin/JS site to GitHub Pages](https://github.com/naotiki/Ese/actions/workflows/deploy-pages.yml/badge.svg)](https://github.com/naotiki/Ese/actions/workflows/deploy-pages.yml)
[![Create Release Draft Compose Desktop](https://github.com/naotiki/Ese/actions/workflows/create-release.yml/badge.svg)](https://github.com/naotiki/Ese/actions/workflows/create-release.yml)
[![wakatime](https://wakatime.com/badge/github/naotiki/EseLinux.svg)](https://wakatime.com/badge/github/naotiki/EseLinux)
# Ese - Easy Shell Environment 
Unixシェルを模した動作をするアプリ
# インストール
[Release](https://github.com/naotiki/Ese/releases/latest)からダウンロードできます。
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