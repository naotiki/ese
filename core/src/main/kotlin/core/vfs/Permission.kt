package core.vfs

import kotlin.Boolean

enum class PermissionTarget {
    OtherX,
    OtherW,
    OtherR,
    GroupX,
    GroupW,
    GroupR,
    OwnerX,
    OwnerW,
    OwnerR
}

/**
 * @param value 0~511 (OCT:777)(BIN:111_111_111)までの範囲
 */
@JvmInline
value class Permission(val value: Int) {
    constructor(string: String) : this(string.toInt(8))

    constructor(vararg targets: PermissionTarget) : this(targets.fold(0) { acc, permissionTarget ->
        acc or permissionTarget.getFlag()
    })


    companion object {
        private val permissionLabel = listOf("x", "w", "r")

        //rw-rw-r--
        val fileDefault = Permission(0b110_110_100)
        val exeDefault = fileDefault + PermissionTarget.OwnerX + PermissionTarget.GroupX + PermissionTarget.OtherX

        //rwxrwxr-x
        val dirDefault = Permission(0b111_111_101)
    }

    operator fun plus(target: PermissionTarget): Permission {
        return Permission(value or target.getFlag())
    }

    /**
     * 有効な[PermissionTarget]を取得します
     * @return [PermissionTarget]の[List]を返します。
     * */
    fun getPermissions(): List<PermissionTarget> {
        return PermissionTarget.values().filter {
            has(it)
        }
    }

    /**
     * 対象の権限があるかを確認します。
     * @param permission 対象の権限
     */
    fun has(permission: PermissionTarget): Boolean = value and permission.getFlag() != 0

    override fun toString(): String {
        var str = ""
        repeat(9) {
            str = (if (value and (1 shl it) != 0) {
                permissionLabel[(it) % 3]
            } else "-") + str
        }
        return str
    }

}

fun PermissionTarget.getFlag(): Int {
    return 1 shl this.ordinal
}