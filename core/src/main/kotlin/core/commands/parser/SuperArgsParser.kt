package core.commands.parser

class CommandParserException(s: String) : Exception("コマンド解析エラー:$s")

class SuperArgsParser {
    val args = mutableListOf<Arg<*>>()
    val opts = mutableListOf<Opt<*>>()

    //解析
    @Throws(CommandIllegalArgsException::class)
    fun parse(argList: List<String>) {
        //初期化
        args.forEach { it.reset() }
        opts.forEach { it.reset() }
        //可変長引数は最後に持ってくる
        val q = args.sortedWith { o1: Arg<*>, o2: Arg<*> ->
            if (o1.vararg != null) {
                1
            } else if (o2.vararg != null) {
                -1
            } else 0
        }.iterator()
        //オプションがあるかどうか
        var inOption: Opt<*>? = null
        //可変長引数用
        var target: Arg<*>? = null
        argList.filter { it.isNotBlank() }.forEachIndexed { index, s ->
            when {
                //オプション(発見)
                s.startsWith("-") -> {
                    val name = s.trimStart('-')
                    val o = opts.filter {
                        if (s.startsWith("--")) {
                            it.name == name
                        } else {
                            // ls -lhaなどのBooleanの複数羅列対応
                            ((it.type is ArgType.Boolean) && (it.shortName?.let { it in name } == true)) || (it.shortName == name)
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
                            target = q.next()
                    }
                    if (target!!.vararg != null) {
                        target?.vararg!!.addValue(s)
                    } else {
                        target!!.updateValue(s)
                    }
                }
            }
        }
        args.filterNot {
            it.hasValue() || it.optional
        }.forEach {
            throw CommandParserException("必須な引数${it.name}が指定されていません")
        }
        opts.filterNot {
            it.hasValue() || !it.required
        }.forEach {
            throw CommandParserException("必須なオプション${it.name}が指定されていません")
        }

    }


}