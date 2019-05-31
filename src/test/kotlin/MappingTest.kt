import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class MyPacket(val a: Byte, val b: Short)

data class MyPacketWrap(val a: Byte, val b: Array<Int>, val myPacket: MyPacket, val c: Int)

class MappingTest {

    @Test
    fun test() {
        val packetDefinition = PacketDefinitionBuilder(MyPacket::class)
            .addByte()
            .addShort()
            .build()
        val packetDefinitionWrap = PacketDefinitionBuilder(MyPacketWrap::class)
            .addByte()
            .addIntArray(2)
            .addClass(packetDefinition)
            .addInt()
            .build()

        val array = UByteArray(16)
        array[0] = 33u
        array[8] = 8u
        array[9] = 1u
        array[10] = 0u
        array[11] = 1u
        array[15] = 4u

        val packetMapper = PacketMapper(packetDefinitionWrap)
        val buffer = ByteBuffer.wrap(array.toByteArray())
            .order(ByteOrder.LITTLE_ENDIAN)
        val packet = packetMapper.mapToObject(buffer)
//    println(packet)
        val buffer1 = packetMapper.mapToBuffer(packet, ByteOrder.LITTLE_ENDIAN)

//        buffer1.array()
//            .map { i -> print("$i ") }

//        println()
        buffer1.position(0)
        val packet2 = packetMapper.mapToObject(buffer1.order(ByteOrder.LITTLE_ENDIAN))
//    println(packet2)

        packet == packet2
    }
}