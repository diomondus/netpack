import java.nio.ByteBuffer
import java.nio.ByteBuffer.allocate
import java.nio.ByteBuffer.wrap
import java.nio.ByteOrder
import java.util.*

class PacketMapper<T : Any>(private val definition: PacketDefinition<T>) {

    fun mapToObject(buffer: ByteBuffer): T {
        val typesIterator = definition.fieldTypes.iterator()
        val arraysSizeIterator = definition.fieldTypes.iterator()
        val defIterator = definition.definitions.iterator()
        val args = LinkedList<Any>()
        while (typesIterator.hasNext()) {
            val arg = when (typesIterator.next()) {
                BYTE_TYPE -> buffer.extractByte()
                SHORT_TYPE -> buffer.extractShort()
                INT_TYPE -> buffer.extractInt()
                LONG_TYPE -> buffer.extractLong()
                BYTE_ARRAY_TYPE -> Array(arraysSizeIterator.next()) { buffer.extractByte() }
                SHORT_ARRAY_TYPE -> Array(arraysSizeIterator.next() / 2) { buffer.extractShort() }
                INT_ARRAY_TYPE -> Array(arraysSizeIterator.next() / 4) { buffer.extractInt() }
                LONG_ARRAY_TYPE -> Array(arraysSizeIterator.next() / 8) { buffer.extractLong() }
                CLASS_TYPE -> {
                    val packetDefinition = defIterator.next()
                    PacketMapper(packetDefinition).mapToObject(buffer)
                }
                else -> throw IllegalStateException()
            }
            args.add(arg)
        }
        return definition.createPacket(args)
    }

    fun mapToBuffer(obj: Any, order: ByteOrder, buffer: ByteBuffer = allocate(definition.getPacketSize())): ByteBuffer {
        buffer.order(order)
        val fieldsIterator = obj.javaClass.declaredFields.iterator()
        val typesIterator = definition.fieldTypes.iterator()
        val defIterator = definition.definitions.iterator()

        while (typesIterator.hasNext() && fieldsIterator.hasNext()) {
            val field = fieldsIterator.next()
            field.isAccessible = true
            val fieldValue = field.get(obj)
            when (typesIterator.next()) {
                BYTE_TYPE -> buffer.put(fieldValue as Byte)
                SHORT_TYPE -> buffer.putShort(fieldValue as Short)
                INT_TYPE -> buffer.putInt(fieldValue as Int)
                LONG_TYPE -> buffer.putLong(fieldValue as Long)
                BYTE_ARRAY_TYPE -> (fieldValue as Array<Byte>).map { buffer.put(it) }
                SHORT_ARRAY_TYPE -> (fieldValue as Array<Short>).map { buffer.putShort(it) }
                INT_ARRAY_TYPE -> (fieldValue as Array<Int>).map { buffer.putInt(it) }
                LONG_ARRAY_TYPE -> (fieldValue as Array<Long>).map { buffer.putLong(it) }
                CLASS_TYPE -> {
                    val packetDefinition = defIterator.next()
                    PacketMapper(packetDefinition).mapToBuffer(fieldValue, order, buffer)
                }
                else -> throw IllegalStateException()
            }
        }
        return buffer
    }

    private fun ByteBuffer.extractLong() = subBuffer(8).long
    private fun ByteBuffer.extractInt() = subBuffer(4).int
    private fun ByteBuffer.extractShort() = subBuffer(2).short
    private fun ByteBuffer.extractByte() = subBuffer(1).get()

    private fun ByteBuffer.subBuffer(sizeFromCurrentPosition: Int): ByteBuffer {
        val array = ByteArray(sizeFromCurrentPosition)
        get(array, 0, sizeFromCurrentPosition)
        return wrap(array)
            .order(order())
    }
}