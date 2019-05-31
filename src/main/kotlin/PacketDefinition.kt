import java.util.*
import kotlin.reflect.KClass

data class PacketDefinition<T : Any>(
    private val klass: KClass<T>,
    private val bytesCount: Int,
    val fieldTypes: LinkedList<Int>,
    val arraysSize: LinkedList<Int>,
    val definitions: LinkedList<PacketDefinition<*>>
) {
    fun getPacketSize(): Int {
        return if (definitions.isEmpty()) {
            bytesCount
        } else {
            bytesCount + definitions.map { it.getPacketSize() }
                .reduce { c1, c2 -> c1 + c2 }
        }
    }

    fun createPacket(args: List<Any>): T {
        return klass.constructors
            .iterator()
            .next()
            .call(*args.toTypedArray())
    }
}