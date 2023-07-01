[![Deploy Kotlin/JS site to GitHub Pages](https://github.com/naotiki/Ese/actions/workflows/deploy-pages.yml/badge.svg)](https://github.com/naotiki/Ese/actions/workflows/deploy-pages.yml)
[![Create Release Draft Compose Desktop](https://github.com/naotiki/Ese/actions/workflows/create-release.yml/badge.svg)](https://github.com/naotiki/Ese/actions/workflows/create-release.yml)
[![wakatime](https://wakatime.com/badge/github/naotiki/Ese.svg)](https://wakatime.com/badge/github/naotiki/Ese)

<div align="center">

# Ese - Easy Shell Environment
[日本語](README.md) / English

Like Unix shell 

</div>


# Install
You can install the latest version in [Release](https://github.com/naotiki/Ese/releases/latest)
# Web version
[https://ese.naotiki.me](https://ese.naotiki.me)
## Modules
### `clients`
The Client Apps for Ese
#### `desktopApp` (JVM)
#### `cuiApp` (JVM)
#### `androidApp` (JVM)
#### `webApp` (JS)
(We're planning WASM support)

---
### `ese-core` (JVM / JS)
Ese Core libraries

---

### Ese Plugin
#### `ese-gradle-plugin`
Gradle Plugin for creating Ese plugins (Noodle)
#### `ese-lib`
Example implementing Ese plugins (we plan to separate them to another repository)

Detail of Ese Plugin is in [HowToBuildNoodle.md](docs/HowToBuildNoodle.md)

# How to build
First, Change current directory to `clients`
```shell
cd clients
```
---
## Run Desktop Client
Run `run`
```shell
./gradlew :desktopApp:run
```
## Install Android Client
Run `installDebug`
```shell
./gradlew :androidApp:installDebug
```
## Run Web Client (Experimental)
Run `jsBrowserDevelopmentRun`
```shell
./gradlew :webApp:jsBrowserDevelopmentRun
```
## Desktop Packaging
Run `superReleaseBuild`
Format of `<APP_VERSION>` is `vX.Y.Z-TEXT`
For example, `-PappVersion=v0.9.0-beta`

Default value of `<APP_VERSION>` is `0.0.1-dev`
```shell
./gradlew :desktopApp:superReleaseBuild -PappVersion=<APP_VERSION>
```