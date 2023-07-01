[![Deploy Kotlin/JS site to GitHub Pages](https://github.com/naotiki/Ese/actions/workflows/deploy-pages.yml/badge.svg)](https://github.com/naotiki/Ese/actions/workflows/deploy-pages.yml)
[![Create Release Draft Compose Desktop](https://github.com/naotiki/Ese/actions/workflows/create-release.yml/badge.svg)](https://github.com/naotiki/Ese/actions/workflows/create-release.yml)
[![wakatime](https://wakatime.com/badge/github/naotiki/Ese.svg)](https://wakatime.com/badge/github/naotiki/Ese)

<div align="center">

# Ese - Easy Shell Environment
日本語 / [English](README-EN.md)

Unixシェルを模した動作をするアプリ
</div>

## 概要
「Easy Shell Environment (Ese)」は簡単で手軽に試せるシェルアプリです。
仮想のファイル操作などができ、権限の設定もできます。
現在、拡張機能開発用SDKを公開しており、今後はSDKで開発された拡張機能を簡単にダウンロードできる機能を実装予定です。
Compose Multiplatformを採用し、幅広いプラットフォームに対応しています。

# インストール
[Release](https://github.com/naotiki/Ese/releases/latest)からインストーラーをダウンロードできます。
# Web版
[https://ese.naotiki.me](https://ese.naotiki.me)

## モジュール

### `clients` Eseのクライアントアプリ
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
#### ~~`ese-lib`~~
~~Eseプラグインの実装例 (別リポジトリに分離予定)~~

[naotiki/ese-noodle-template](https://github.com/naotiki/ese-noodle-template)
に移行


Eseプラグインの詳細は[HowToBuildNoodle.md](docs/HowToBuildNoodle.md)

# ビルド方法
## 共通
まず、`clients`フォルダに移動
```shell
cd clients
```
---
## デスクトップクライアントを実行
`desktopApp`のタスク`run`を実行
```shell
./gradlew :desktopApp:run
```
## Androidクライアントを端末にインストール
`androidApp`のタスク`installDebug`を実行
```shell
./gradlew :androidApp:installDebug
```
## Webクライアントを実行 (Experimental)
`webApp`のタスク`jsBrowserDevelopmentRun`を実行
```shell
./gradlew :webApp:jsBrowserDevelopmentRun
```
## デスクトップのパッケージング
`desktopApp`のタスク`superReleaseBuild`を実行
`vX.Y.Z(-TEXT)`の形式で`<APP_VERSION>`を指定できます。

例:`-PappVersion=v0.9.0-beta`

指定されなければ`v0.0.0-dev`で実行されます。
```shell
./gradlew :desktopApp:superReleaseBuild -PappVersion=<APP_VERSION>
```