package core.utils

import core.vfs.Directory
import core.vfs.File
import core.vfs.FireTree
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
data class SaveDataFormat(val shell:FileTree)
@Serializable
data class FileElement(val file: File,val children:List<FileElement>?=null)

@Serializable
data class FileTree(val map: List<FileElement>){

    companion object{
        fun generate(root:Directory){
            buildList<FileElement> {
                root.children
            }
        }
    }
}

object DirectoryAsStringSerializer : KSerializer<Directory> {
    val a=PolymorphicSerializer(File::class)
    private val delegateSerializer = MapSerializer(String.serializer(),a)
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Dir") {
        element<File>("baseFile")
        element<Map<String,File>>("children")
    }


    override fun serialize(encoder: Encoder, value: Directory) {
        encoder.encodeStructure(descriptor){

            encodeSerializableElement(descriptor,0,a,value as File)
            encodeSerializableElement(descriptor,1,delegateSerializer,value.children)
        }

/*
        encodeSerializableValue(File.serializer(),value)
        encodeSerializableValue(delegateSerializer,value.children)*/
    }

    override fun deserialize(decoder: Decoder): Directory {
        return FireTree.root
    }
}
val module = SerializersModule {
    polymorphic(File::class) {
        subclass(Directory::class)
    }
}

val format = Json { serializersModule = module }