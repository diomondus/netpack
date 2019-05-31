import SupportedTypes.BYTE_ARRAY_TYPE
import SupportedTypes.BYTE_TYPE
import SupportedTypes.CLASS_TYPE
import SupportedTypes.INT_ARRAY_TYPE
import SupportedTypes.INT_TYPE
import SupportedTypes.LONG_ARRAY_TYPE
import SupportedTypes.LONG_TYPE
import SupportedTypes.SHORT_ARRAY_TYPE
import SupportedTypes.SHORT_TYPE
import java.nio.ByteBuffer
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
                BYTE_TYPE -> buffer.subBuffer(1).get()
                SHORT_TYPE -> buffer.subBuffer(2).short
                INT_TYPE -> buffer.subBuffer(4).int
                LONG_TYPE -> buffer.subBuffer(8).long
                BYTE_ARRAY_TYPE -> Array(arraysSizeIterator.next()) {
                    buffer.subBuffer(1)
                        .get()
                }
                SHORT_ARRAY_TYPE -> Array(arraysSizeIterator.next() / 2) {
                    buffer.subBuffer(2)
                        .short
                }
                INT_ARRAY_TYPE -> Array(arraysSizeIterator.next() / 4) {
                    buffer.subBuffer(4)
                        .int
                }
                LONG_ARRAY_TYPE -> Array(arraysSizeIterator.next() / 8) {
                    buffer.subBuffer(8)
                        .long
                }
                CLASS_TYPE -> {
                    val packetDefinition = defIterator.next()
                    PacketMapper(packetDefinition).mapToObject(buffer)
                }
                else -> throw IllegalStateException()
            }
            args.add(arg)
        }

        return definition.klass.constructors
            .iterator()
            .next()
            .call(*args.toArray())
    }

    fun mapToBuffer(
        obj: Any,
        order: ByteOrder,
        buffer: ByteBuffer = ByteBuffer.allocate(definition.getPacketSize())
    ): ByteBuffer {
        buffer.order(order)
        val fieldsIterator = obj.javaClass.declaredFields.iterator()
        val typesIterator = definition.fieldTypes.iterator()
        val defIterator = definition.definitions.iterator()

        while (typesIterator.hasNext() && fieldsIterator.hasNext()) {
            val field = fieldsIterator.next()
            field.isAccessible = true
            val any = field.get(obj)
            when (typesIterator.next()) {
                BYTE_TYPE -> buffer.put(any as Byte)
                SHORT_TYPE -> buffer.putShort(any as Short)
                INT_TYPE -> buffer.putInt(any as Int)
                LONG_TYPE -> buffer.putLong(any as Long)
                BYTE_ARRAY_TYPE -> {
                    for (byte in any as Array<Byte>) {
                        buffer.put(byte)
                    }
                }
                SHORT_ARRAY_TYPE -> {
                    for (sh in any as Array<Short>) {
                        buffer.putShort(sh)
                    }
                }
                INT_ARRAY_TYPE -> {
                    for (i in any as Array<Int>) {
                        buffer.putInt(i)
                    }
                }
                LONG_ARRAY_TYPE -> {
                    for (l in any as Array<Long>) {
                        buffer.putLong(l)
                    }
                }
                CLASS_TYPE -> {
                    val packetDefinition = defIterator.next()
                    PacketMapper(packetDefinition).mapToBuffer(any, order, buffer)
                }
                else -> throw IllegalStateException()
            }
        }
        return buffer
    }

    private fun ByteBuffer.subBuffer(sizeFromCurrentPosition: Int): ByteBuffer {
        val array = ByteArray(sizeFromCurrentPosition)
        get(array, 0, sizeFromCurrentPosition)
        return ByteBuffer.wrap(array)
            .order(order())
    }
}