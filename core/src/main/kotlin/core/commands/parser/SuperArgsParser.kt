package core.commands.parser

import core.utils.nextOrNull

class CommandParserException(command: Executable<*>?, s: String) : Exception("${command?.name}コマンド解析エラー:$s")

class SuperArgsParser {
    val args = mutableListOf<Arg<*>>()
    val opts = mutableListOf<Opt<*>>()

    //解析
    @Throws(CommandParserException::class)
    fun parse(origin: Executable<*>, argList: List<String>) {
        //初期化
        args.forEach { it.reset() }
        opts.forEach { it.reset() }
        //可変長引数は最後に持ってくる
        //オプションがあるかどうか
        var inOption: Opt<*>? = null
        //可変長引数用
        val normalizedArgs = argList.filter { it.isNotBlank() }
        //可変長引数は最後に解析
        val argListIterator = args.sortedWith { o1: Arg<*>, o2: Arg<*> ->
            if (o1.vararg != null) {
                1
            } else if (o2.vararg != null) {
                -1
            } else 0
        }.listIterator()
        var nextArg = argListIterator.nextOrNull()
        normalizedArgs.forEach { str: String ->
            val includeOptionInArg = nextArg?.vararg?.includeOptionInArg == true

            if (includeOptionInArg) {
                //オプションも脳死で入れてく
                //nextArgはNonNull確定
                nextArg!!.vararg!!.addValue(str)
            } else {
                when {
                    str.startsWith("-") -> {
                        val name = str.trimStart('-')
                        val o = opts.filter { opt: Opt<*> ->
                            if (str.startsWith("--")) {
                                opt.name == name
                            } else {
                                // ls -lhaなどのBooleanの複数羅列対応
                                ((opt.type is ArgType.Boolean) && (opt.shortName?.let { it in name } == true))
                                        || opt.shortName == name
                            }
                        }
                        if (o.isEmpty()) {
                            throw CommandParserException(origin, "オプション解析エラー:不明な名前")
                        }
                        o.forEach {
                            if (it.type is ArgType.Boolean) {
                                it.updateValue("true")
                            } else inOption = it
                        }
                    }

                    inOption != null -> {
                        if (inOption!!.multiple != null) {
                            inOption!!.multiple!!.addValue(str)
                        } else inOption!!.updateValue(str)
                        inOption = null
                    }

                    nextArg != null -> {
                        if (nextArg!!.vararg == null) {
                            nextArg!!.updateValue(str)
                            nextArg = argListIterator.nextOrNull()
                        } else {
                            nextArg!!.vararg!!.addValue(str)
                        }
                    }

                    else -> {
                        TODO("💥")
                    }
                }
            }
        }

        args.filterNot {
            it.value != null || it.vararg != null || it.optional
        }.forEach {
            throw CommandParserException(origin, "必須な引数${it.name}が指定されていません")
        }
        opts.filterNot {
            it.value != null || it.multiple != null || !it.required
        }.forEach {
            throw CommandParserException(origin, "必須なオプション${it.name}が指定されていません")
        }
    }
}