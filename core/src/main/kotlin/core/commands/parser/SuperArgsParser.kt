package core.commands.parser

import core.commands.dev.CommandDefineException
import core.utils.log
import core.utils.nextOrNull

class CommandParserException(command: Executable<*>?, s: String) : Exception("${command?.name}コマンド解析エラー:$s")

class SuperArgsParser {
    //定義
    internal val args = mutableListOf<Arg<*>>()
    internal val opts = mutableListOf<Opt<*>>()

    //可変長引数は最後に解析
    val sortedArgs
        get() = args.sortedWith { o1: Arg<*>, o2: Arg<*> ->
            if (o1.vararg != null) {
                1
            } else if (o2.vararg != null) {
                -1
            } else 0
        }

    fun getNextArgTypeOrNull(argList: List<String>): Pair<ArgType<out Any>, String>? {
        var inOption: Opt<*>? = null
        val argListIterator = sortedArgs.listIterator()
        var nextArg: Arg<*>? = null//argListIterator.nextOrNull()
        var lastString: String = ""
        argList.filter { it.isEmpty() || it.isNotBlank() }.forEach { str: String ->
            lastString = str
            //sudoの後など引数にオプションも含めるとき
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
                            return null
                        }
                        o.forEach {
                            if (it.type !is ArgType.Boolean) {
                                inOption = it
                            }
                        }
                    }

                    inOption != null -> {
                        inOption = null
                    }

                    else/*nextArg != null */ -> {
                        if (nextArg?.vararg == null) {
                            nextArg = argListIterator.nextOrNull()
                        }
                    }

                    /*else -> {
                        TODO("💥")
                    }*/
                }
            }
        }

        return (inOption?.type ?: nextArg?.type)?.let { it to lastString }
    }

    //解析
    @Throws(CommandParserException::class)
    fun parse(exe: Executable<*>, argList: List<String>,subCommand: Executable<*>.SubCommand<*>?=null):
            Pair<Executable<out
    Any?>.SubCommand<out Any?>,
            List<String>>? {
        if (subCommand==null&&exe.subCommands.isNotEmpty() && args.isNotEmpty()) throw CommandDefineException("Argsはだめです")
        opts.forEach { it.reset() }
        //可変長引数は最後に持ってくる
        //オプションがあるかどうか
        var inOption: Opt<*>? = null
        //空白は無視
        val normalizedArgs = argList.filter { it.isNotBlank() }

        val argListIterator = sortedArgs.listIterator()
        var nextArg = argListIterator.nextOrNull()
        normalizedArgs.forEachIndexed {  index:Int,str: String ->
            //sudoの後など引数にオプションも含めるとき
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
                            throw CommandParserException(exe, "オプション解析エラー:不明な名前")
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

                    subCommand ==null && exe.subCommands.isNotEmpty() -> {


                        return exe.subCommands.single { it.name==str } to normalizedArgs.drop(index+1).log()

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
            throw CommandParserException(exe, "必須な引数${it.name}が指定されていません")
        }
        opts.filterNot {
            it.value != null || it.multiple != null || !it.required
        }.forEach {
            throw CommandParserException(exe, "必須なオプション${it.name}が指定されていません")
        }
        return null
    }
}