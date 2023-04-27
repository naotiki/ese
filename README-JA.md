[![Deploy Kotlin/JS site to GitHub Pages](https://github.com/naotiki/Ese/actions/workflows/deploy-pages.yml/badge.svg)](https://github.com/naotiki/Ese/actions/workflows/deploy-pages.yml)
[![Create Release Draft Compose Desktop](https://github.com/naotiki/Ese/actions/workflows/create-release.yml/badge.svg)](https://github.com/naotiki/Ese/actions/workflows/create-release.yml)
[![wakatime](https://wakatime.com/badge/github/naotiki/Ese.svg)](https://wakatime.com/badge/github/naotiki/Ese)

<div align="center">

# Ese - Easy Shell Environment
 [English](README.md) / 日本語

 Unixシェルを模した動作をするアプリ
</div>


# インストール
[Release](https://github.com/naotiki/Ese/releases/latest)からダウンロードできます。
# Web版
[https://ese.naotiki.me](https://ese.naotiki.me)

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

---

### Ese Plugin
#### `ese-gradle-plugin`
Eseプラグイン (Noodle)を作成するためのGradle Plugin
#### `ese-lib`
Eseプラグインの実装例 (別リポジトリに分離予定)

Eseプラグインの詳細は[HowToBuildNoodle.md](docs/HowToBuildNoodle.md)

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