package core.commands.parser

import core.utils.nextOrNull

class CommandParserException(command: Command<*>?,s: String) : Exception("${command?.name}コマンド解析エラー:$s")

class SuperArgsParser {
    val args = mutableListOf<Arg<*>>()
    val opts = mutableListOf<Opt<*>>()

    //解析
    @Throws(CommandParserException::class)
    fun parse(origin: Command<*>,argList: List<String>) {
        //初期化
        args.forEach { it.reset() }
        opts.forEach { it.reset() }
        //可変長引数は最後に持ってくる
        var index = 0
        //オプションがあるかどうか
        var inOption: Opt<*>? = null
        //可変長引数用
        var target: Arg<*>? = null
        val normalizedArgs = argList.filter { it.isNotBlank() }


        val arxgs = args.sortedWith { o1: Arg<*>, o2: Arg<*> ->
            if (o1.vararg != null) {
                1
            } else if (o2.vararg != null) {
                -1
            } else 0
        }.listIterator()
        var nextArg = arxgs.nextOrNull()
        //var argIndex = 0
        normalizedArgs.forEach { str: String ->
            val includeOption = nextArg?.vararg?.includeOption == true

            //オプションも脳死で入れてく
            if (includeOption) {
                //NonNull確定
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
                                ((opt.type is ArgType.Boolean) && (opt.shortName?.let { it in name } == true)) || (opt
                                    .shortName == name)
                            }
                        }
                        if (o.isEmpty()) {
                            throw CommandParserException(origin,"オプション解析エラー:不明な名前")
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
                            nextArg = arxgs.nextOrNull()
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

        //
        /*normalizedArgs.forEach { s ->
            when {
                //オプション(発見)
                s.startsWith("-") -> {
                    val name = s.trimStart('-')
                    val o = opts.filter { opt: Opt<*> ->
                        if (s.startsWith("--")) {
                            opt.name == name
                        } else {
                            // ls -lhaなどのBooleanの複数羅列対応
                            ((opt.type is ArgType.Boolean) && (opt.shortName?.let { it in name } == true)) || (opt
                                .shortName == name)
                        }
                    }
                    if (o.isEmpty()) {
                        throw CommandParserException("オプション解析エラー:不明な名前")
                    }
                    o.forEach {
                        if (it.type is ArgType.Boolean) {
                            it.updateValue("true")
                        } else inOption = it

                    }
                }
                //オプション(代入)
                inOption != null -> {
                    if (inOption!!.multiple != null) {
                        inOption!!.multiple!!.addValue(s)
                    } else inOption!!.updateValue(s)
                    inOption = null
                }
                //引数
                else -> {
                    if (target?.vararg == null) {
                        target = q[index++]
                    }

                    if (target!!.vararg != null) {
                        target?.vararg!!.addValue(s)
                    } else {
                        target!!.updateValue(s)
                    }
                }
            }
        }*/
        args.filterNot {
            it.value!=null||it.vararg!=null  || it.optional
        }.forEach {
            throw CommandParserException(origin,"必須な引数${it.name}が指定されていません")
        }
        opts.filterNot {
            it.value!=null||it.multiple!=null || !it.required
        }.forEach {
            throw CommandParserException(origin,"必須なオプション${it.name}が指定されていません")
        }

    }


}