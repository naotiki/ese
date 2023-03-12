package core

/**
 * EseError
 * */
sealed class EseError(msg: String?) : Throwable(msg) {
    /**
     * コマンドの引数要素数の不一致など
     * このエラーは通常ArgsParserからのみ出力されます。**/
    class CommandParseError internal constructor(msg: String?) : EseError(msg)

    //引数のキャストが成功しなかった場合
    class CommandIllegalArgumentError(msg: String?) : EseError(msg)

    class CommandExecutionError(msg: String?) : EseError(msg)

    class FileOperationDenyError internal constructor(msg: String?) : EseError(msg)

    class FileNotFoundError internal constructor(msg: String?) : EseError(msg)

    class PluginLoadError internal constructor(msg: String?):EseError(msg)
}

