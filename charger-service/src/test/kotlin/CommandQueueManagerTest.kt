
import org.paragontech.charger.CommandEnvelope
import org.paragontech.charger.CommandQueueManager
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class CommandQueueManagerTest {

    private fun createCommand(chargerId: String, type: String = "StartTransaction", retries: Int = 3): CommandEnvelope =
        CommandEnvelope(chargerId = chargerId, commandType = type, payload = "{}", retriesLeft = retries)

    @org.junit.jupiter.api.Test
    fun `enqueue should add a command to the correct charger queue`() {
        val manager = CommandQueueManager()
        val cmd = createCommand("charger-1")

        val updated = manager.enqueue(cmd)

        val queue = updated.getQueue("charger-1").toList()
        assertEquals(1, queue.size)
        assertEquals(cmd, queue.first())
    }

    @Test
    fun `dequeue should remove the first command from the queue`() {
        val cmd1 = createCommand("charger-1", "StartTransaction")
        val cmd2 = createCommand("charger-1", "StopTransaction")
        val manager = CommandQueueManager().enqueue(cmd1).enqueue(cmd2)

        val updated = manager.dequeue("charger-1")
        val queue = updated.getQueue("charger-1").toList()

        assertEquals(1, queue.size)
        assertEquals(cmd2, queue.first())
    }

    @Test
    fun `retry should re-enqueue the command with decremented retries`() {
        val cmd = createCommand("charger-1", retries = 2)
        val manager = CommandQueueManager().enqueue(cmd)

        val retried = manager.retry("charger-1")
        val retriedCommand = retried.getQueue("charger-1").peek()

        assertNotNull(retriedCommand)
        assertEquals(1, retriedCommand!!.retriesLeft)
    }

    @Test
    fun `retry should remove the command if retries are exhausted`() {
        val cmd = createCommand("charger-1", retries = 0)
        val manager = CommandQueueManager().enqueue(cmd)

        val retried = manager.retry("charger-1")
        assertTrue(retried.isEmpty("charger-1"))
    }

    @Test
    fun `getQueue should return an empty queue for unknown charger`() {
        val manager = CommandQueueManager()
        val queue = manager.getQueue("nonexistent").toList()

        assertTrue(queue.isEmpty())
    }

    @Test
    fun `isEmpty should return true for an empty or unknown charger queue`() {
        val manager = CommandQueueManager()
        assertTrue(manager.isEmpty("charger-1"))
    }

    @Test
    fun `isEmpty should return false when queue has elements`() {
        val cmd = createCommand("charger-1")
        val manager = CommandQueueManager().enqueue(cmd)

        assertFalse(manager.isEmpty("charger-1"))
    }
}
