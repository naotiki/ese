package me.naotiki.ese.core

/**
 * EseError
 * */
sealed class EseError(msg: String?,val errorName:String) : Throwable(msg) {

    /**
     * コマンドの引数要素数の不一致など
     * このエラーは通常ArgsParserからのみ出力されます。**/
    class CommandParseError internal constructor(msg: String?) : EseError(msg,"コマンド解析エラー")

    //引数のキャストが成功しなかった場合
    class CommandIllegalArgumentError(msg: String?) : EseError(msg,"コマンド引数エラー")

    class CommandExecutionError(msg: String?) : EseError(msg,"コマンド実行時エラー")

    class FilePermissionError internal constructor(msg: String?) : EseError(msg,"ファイル操作拒否エラー")

    class FileNotFoundError internal constructor(msg: String?) : EseError(msg,"ファイル参照エラー")

    class PluginLoadError internal constructor(msg: String?):EseError(msg,"プラグインロードエラー")

    fun buildErrorMessage(): String {
        return "$errorName:\n$message"
    }

}

