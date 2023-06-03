package me.naotiki.ese.core

/**
 * Not fatal error in Ese.
 * Executing functions would be suspended, but Ese Client continues to work.
 * @param msg Error message
 * @param errorName Localized error name
 * */
sealed class EseError(msg: String?,val errorName:String) : Throwable(msg) {

    class CommandParseError internal constructor(msg: String?) : EseError(msg,"コマンド解析エラー")

    /**
     * Command args cast error
     */
    class CommandIllegalArgumentError(msg: String?) : EseError(msg,"コマンド引数エラー")

    /**
     * Error when executing commands
     */
    class CommandExecutionError(msg: String?) : EseError(msg,"コマンド実行時エラー")

    class FilePermissionError internal constructor(msg: String?) : EseError(msg,"ファイル操作拒否エラー")

    class FileNotFoundError internal constructor(msg: String?) : EseError(msg,"ファイル参照エラー")

    class PluginLoadError internal constructor(msg: String?):EseError(msg,"プラグインロードエラー")

    fun buildErrorMessage(): String {
        return "$errorName:\n$message"
    }

}

