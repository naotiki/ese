
abstract class File
    (var name: String, var attribute: Int = Attribute.None)
{

}

enum class FileType{

}
object Attribute{
    const val None=0
    const val Hide=1

}
