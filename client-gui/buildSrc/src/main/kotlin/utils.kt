inline fun <reified E :Enum<E>> String.toEnum(ignoreCase:Boolean=true): E? {
    return enumValues<E>().firstOrNull {
        it.name.equals(this,ignoreCase)
    }
}