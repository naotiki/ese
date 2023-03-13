package secure

class EsePermission {
    enum class InspectTarget{
        Owner,
        FuncCall
    }
    enum class Permissions{
        FileAccess,
        Reflection,
        ClassLoad,
        ExternalExecute,
        NativeCall,
    }
}