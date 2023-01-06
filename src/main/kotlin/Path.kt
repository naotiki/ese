
@JvmInline
value class Path(val value: String)
object PathManager {
    private var path: Path = Path("/")
    fun getPath(): Path {
        return path
    }
    fun setPath(path: Path) {
        this.path = path
    }
}