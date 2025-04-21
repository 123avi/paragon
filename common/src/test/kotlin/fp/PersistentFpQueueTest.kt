package fp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.paragontech.fp.fpQueueOf

class PersistentFpQueueTest {

    @Test
    fun `enqueue adds an item to the queue`() {
        val queue = fpQueueOf<Int>().enqueue(1)
        assertFalse(queue.isEmpty())
        assertEquals(1, queue.peek())
        assertEquals(listOf(1), queue.toList())
    }

    @Test
    fun `enqueue multiple items preserves order`() {
        val queue = fpQueueOf<Int>().enqueue(1).enqueue(2).enqueue(3)
        assertEquals(listOf(1, 2, 3), queue.toList())
    }

    @Test
    fun `dequeue removes the first item`() {
        val queue = fpQueueOf(1, 2, 3)
        val dequeued = queue.dequeue()
        assertEquals(listOf(2, 3), dequeued.toList())
    }

    @Test
    fun `dequeue on empty queue returns same empty queue`() {
        val queue = fpQueueOf<String>()
        val dequeued = queue.dequeue()
        assertTrue(dequeued.isEmpty())
        assertEquals(emptyList<String>(), dequeued.toList())
    }

    @Test
    fun `peek returns first item without removing`() {
        val queue = fpQueueOf("a", "b", "c")
        assertEquals("a", queue.peek())
        assertEquals(listOf("a", "b", "c"), queue.toList())
    }

    @Test
    fun `peek on empty queue returns null`() {
        val queue = fpQueueOf<Any>()
        assertNull(queue.peek())
    }

    @Test
    fun `isEmpty returns true only for empty queue`() {
        assertTrue(fpQueueOf<Int>().isEmpty())
        assertFalse(fpQueueOf(42).isEmpty())
    }

    @Test
    fun `toList returns the correct sequence`() {
        val queue = fpQueueOf("x", "y", "z")
        assertEquals(listOf("x", "y", "z"), queue.toList())
    }
}
