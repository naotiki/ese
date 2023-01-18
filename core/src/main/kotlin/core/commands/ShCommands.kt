package core.commands

object If:Command<Unit>("if"){
    override suspend fun execute(args: List<String>) {

    }

}

object Test:Command<Boolean>("test"){
    override suspend fun execute(args: List<String>):Boolean {

        return true
    }
}