package core.commands

object If:Command<Unit>("if"){
    override fun execute(args: List<String>) {

    }

}

object Test:Command<Boolean>("test"){
    override fun execute(args: List<String>):Boolean {

        return true
    }
}