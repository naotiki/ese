# EseLinux (~~エセ~~Linux)
Linuxのコマンドやファイルを再現したシェルもどきです。
仮想OSなどそんな大層なものではありません。

もちろんすべて**Kotlin**で書かれています。
## `core`
中核となるコマンド解析機、ファイル変数、入出力機能が備わっています。
## `client-gui`
`core`を操作するためのGUIを兼ね備えたスタンドアロンアプリです。
Compose Multiplatformで書かれています。

## `server`
WIP

## ビルド方法

`./gradlew build` 