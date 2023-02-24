package core.export

import core.user.Group
import core.user.User
import core.utils.log
import core.vfs.Directory
import kotlinx.serialization.Serializable
import java.nio.ByteBuffer
import java.util.zip.Deflater
import java.util.zip.Inflater

object EseSave {
    /**
     * [ByteArray]を圧縮します。
     * 先頭4バイトには圧縮前のデータサイズが格納されます。
     * 展開には[inflate]を使用してください
     * */
    fun compress(byteArray: ByteArray): ByteArray {
        val compressedData = ByteArray(byteArray.size)
        val compressor = Deflater()

        compressor.setInput(byteArray)
        compressor.finish()
        val compressedDataLength = compressor.deflate(compressedData)

        return ByteBuffer.allocate(Int.SIZE_BYTES + compressedDataLength).putInt(byteArray.size.log()).put(
            compressedData.copyOfRange(
                0,
                compressedDataLength
            )
        ).array()
    }

    /**
     * [compress]で圧縮された[ByteArray]を展開します。
     */
    fun inflate(compressedData: ByteArray): ByteArray {
        val a = ByteBuffer.wrap(compressedData)
        val originalDataSize = a.int.log()
        val originalData = ByteArray(originalDataSize)
        val inflater = Inflater()
        inflater.setInput(a.array().copyOfRange(Int.SIZE_BYTES, compressedData.size))
        inflater.finished()
        val originalDataLength = inflater.inflate(originalData)
        return originalData.copyOfRange(0, originalDataLength)
    }
}

@Serializable
data class EseExportData(
    val groups: List<Group>,
    val users: List<ExportUser>,

    val rootDir: ExportableFile
) {
    fun importUsers(): List<User> = users.map {
        User(it.name, groups.single { g -> g.id == it.groupId }, it.uid)
    }

    fun generateFileTree() {
        val users = importUsers()
        if (rootDir.data !is ExportableData.DirectoryData) TODO()
        val root = rootDir.run {
            Directory(
                fileName, null,
                users.single {
                    it.id == userID
                },
                groups.single {
                    it.id == groupID
                },
                permission, hidden
            )
        }
        rootDir.data.children


    }
}