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
### `clients`
Eseのクライアントアプリ
#### `desktopApp` (JVM)
#### `cuiApp` (JVM)
#### `androidApp` (JVM)
#### `webApp` (JS)
(WASM対応予定)

---
### `ese-core` (JVM/JS)
Eseのコアライブラリ

# ビルド方法
まず、`clients`フォルダに移動
```shell
cd clients
```
---
## デスクトップクライアントを実行
タスク`run`を実行
```shell
./gradlew :desktopApp:run
```
## Androidクライアントを端末にインストール
タスク`installDebug`を実行
```shell
./gradlew :androidApp:installDebug
```
## Webクライアントを実行 (Experimental)
タスク`jsBrowserDevelopmentRun`を実行
```shell
./gradlew :webApp:jsBrowserDevelopmentRun
```
## デスクトップのパッケージング
タスク`superReleaseBuild`を実行
`vX.Y.Z-TEXT`の形式で`<APP_VERSION>`を指定できます。

例:`-PappVersion=v0.9.0-beta`

指定されなければ`0.0.0-dev`で実行されます。
```shell
./gradlew :desktopApp:superReleaseBuild -PappVersion=<APP_VERSION>
```