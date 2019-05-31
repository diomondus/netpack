import SupportedTypes.BYTE_ARRAY_TYPE
import SupportedTypes.BYTE_TYPE
import SupportedTypes.INT_ARRAY_TYPE
import SupportedTypes.INT_TYPE
import SupportedTypes.LONG_TYPE
import SupportedTypes.SHORT_ARRAY_TYPE
import SupportedTypes.SHORT_TYPE
import java.util.*
import kotlin.reflect.KClass

class PacketDefinitionBuilder<T : Any>(val klass: KClass<T>) {
    var bytesCount = 0
    val fieldsType = LinkedList<Int>()
    val arraysSize = LinkedList<Int>()
    val definitions = LinkedList<PacketDefinition<*>>()

    fun addByte(): PacketDefinitionBuilder<T> {
        fieldsType.add(BYTE_TYPE)
        bytesCount++
        return this
    }

    fun addByteArray(size: Int): PacketDefinitionBuilder<T> {
        fieldsType.add(BYTE_ARRAY_TYPE)
        arraysSize.add(size)
        bytesCount += size
        return this
    }

    fun addShort(): PacketDefinitionBuilder<T> {
        fieldsType.add(SHORT_TYPE)
        bytesCount += 2
        return this
    }

    fun addShortArray(size: Int): PacketDefinitionBuilder<T> {
        fieldsType.add(SHORT_ARRAY_TYPE)
        arraysSize.add(size)
        bytesCount += (size shl 1)
        return this
    }

    fun addInt(): PacketDefinitionBuilder<T> {
        fieldsType.add(INT_TYPE)
        bytesCount += 4
        return this
    }

    fun addIntArray(size: Int): PacketDefinitionBuilder<T> {
        fieldsType.add(INT_ARRAY_TYPE)
        arraysSize.add(size)
        bytesCount += (size shl 2)
        return this
    }

    fun addLong(): PacketDefinitionBuilder<T> {
        fieldsType.add(LONG_TYPE)
        bytesCount += 8
        return this
    }

    fun addLongArray(size: Int): PacketDefinitionBuilder<T> {
        fieldsType.add(INT_ARRAY_TYPE)
        arraysSize.add(size)
        bytesCount += (size shl 3)
        return this
    }

    fun addClass(definition: PacketDefinition<*>): PacketDefinitionBuilder<T> {
        fieldsType.add(0)
        definitions.add(definition)
        return this
    }

    fun build(): PacketDefinition<T> = PacketDefinition(klass, bytesCount, fieldsType, arraysSize, definitions)
}