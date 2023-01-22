package core.vfs

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
 * @param value 0~511 (OCT:777)(BIN:111111111)までの範囲
 */
@JvmInline
value class PermissionValue (val value: Int) {
    constructor(string: String):this(string.toInt(8))
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
}

fun PermissionTarget.getFlag(): Int {
    return 1 shl this.ordinal
}