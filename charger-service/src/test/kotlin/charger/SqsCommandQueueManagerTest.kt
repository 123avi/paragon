package charger

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.paragontech.charger.CommandEnvelope
import org.paragontech.charger.CommandType
import org.paragontech.charger.SqsCommandQueueManager
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SqsCommandQueueManagerTest {

    private lateinit var sqsClient: SqsClient
    private lateinit var manager: SqsCommandQueueManager
    private val queueUrl = "https://sqs.fake-region.amazonaws.com/123456789012/queue.fifo"
    private val json = Json

    @BeforeEach
    fun setup() {
        sqsClient = mockk(relaxed = true)
        manager = SqsCommandQueueManager(queueUrl, sqsClient, json)
    }

    @Test
    fun `enqueueCommand should send message to SQS`() = runTest {
        val command = CommandEnvelope(
            chargerId = "CH-01",
            commandType = CommandType.StartTransaction,
            payload = """{"connectorId":1}"""
        )

        every { sqsClient.sendMessage(any<SendMessageRequest>()) } returns SendMessageResponse.builder().messageId("msg-1").build()

        manager.enqueueCommand( command)

        verify {
            sqsClient.sendMessage(withArg<SendMessageRequest> {
                assert(it.queueUrl() == queueUrl)
                assert(it.messageBody().contains("StartTransaction"))
                assert(it.messageGroupId() == "CH-01")
            })
        }
    }

    @Test
    fun `nextCommand should return the next command from SQS`() = runTest {
        val command = CommandEnvelope(
            chargerId = "CH-01",
            commandType = CommandType.StartTransaction,
            payload = """{"connectorId":1}""",
            commandId = "cmd-123"
        )
        val sqsMessage = Message.builder()
            .body(json.encodeToString(CommandEnvelope.serializer(), command))
            .receiptHandle("abc123")
            .build()

        every { sqsClient.receiveMessage(any<ReceiveMessageRequest>()) } returns ReceiveMessageResponse.builder()
            .messages(listOf(sqsMessage))
            .build()

        val result = manager.nextCommand()

        assertNotNull(result)
        assertEquals("cmd-123", result.commandId)
        assertEquals(CommandType.StartTransaction, result.commandType)
    }

    @Test
    fun `acknowledgeCommand should delete the message from SQS`() = runTest {
        val commandId = "cmd-123"
        // Step 1: Mock receiveMessage
        every { sqsClient.receiveMessage(any<ReceiveMessageRequest>()) } returns ReceiveMessageResponse.builder()
            .messages(
                listOf(
                    Message.builder()
                        .body(json.encodeToString(CommandEnvelope(
                            commandId = commandId,
                            chargerId = "CH-01",
                            commandType = CommandType.StartTransaction,
                            payload = """{"connectorId":1}"""
                        )))
                        .receiptHandle("abc123")
                        .build()
                )
            )
            .build()

        // Step 2: Populate the inFlightCommands by calling nextCommand
        manager.nextCommand()


        every { sqsClient.deleteMessage(any<DeleteMessageRequest>()) } returns DeleteMessageResponse.builder().build()

        manager.acknowledgeCommand(commandId)

        verify {
            sqsClient.deleteMessage(withArg<DeleteMessageRequest> {
                assertEquals("abc123", it.receiptHandle())
            })
        }
    }
    @Test
    fun `retryCommand should re-enqueue the command with decremented retries`() = runTest {
        val command = CommandEnvelope(
            commandId = "cmd-123",
            chargerId = "CH-01",
            commandType = CommandType.StartTransaction,
            payload = """{"connectorId":1}""",
            retriesLeft = 2
        )

        // 1. Mock receiveMessage to simulate message delivery
        val message = Message.builder()
            .body(json.encodeToString(CommandEnvelope.serializer(), command))
            .receiptHandle("receipt-123")
            .build()

        every { sqsClient.receiveMessage(any<ReceiveMessageRequest>()) } returns ReceiveMessageResponse.builder()
            .messages(listOf(message))
            .build()
        // 2. Call nextCommand to populate inFlightCommands
        manager.nextCommand()
        // 3. Mock deleteMessage to simulate message deletion

        every { sqsClient.deleteMessage(any<DeleteMessageRequest>()) } returns DeleteMessageResponse.builder().build()
        every { sqsClient.sendMessage(any<SendMessageRequest>()) } returns SendMessageResponse.builder()
            .messageId("msg-retry")
            .build()
        // 4. Call retryCommand
        manager.retryCommand(command.commandId)

        // 5. Verify deleteMessage was called for cleanup
        verify {
            sqsClient.deleteMessage(withArg<DeleteMessageRequest> {
                assertEquals("receipt-123", it.receiptHandle())
            })
        }
        // 6. Verify enqueueCommand was called with decremented retries
        verify {
            sqsClient.sendMessage(withArg<SendMessageRequest> {
                val retriedCommand = json.decodeFromString(CommandEnvelope.serializer(), it.messageBody())
                assertEquals(1, retriedCommand.retriesLeft)
                assertEquals("CH-01", it.messageGroupId())
                assertEquals(command.commandId, retriedCommand.commandId) // the same commandId retained
            })
        }
    }

    @Test
    fun `retryCommand should remove the command if no retries are left`() = runTest {
        val command = CommandEnvelope(
            commandId = "cmd-123",
            chargerId = "CH-01",
            commandType = CommandType.StartTransaction,
            payload = """{"connectorId":1}""",
            retriesLeft = 0
        )
        every { sqsClient.deleteMessage(any<DeleteMessageRequest>()) } returns DeleteMessageResponse.builder().build()

        manager.retryCommand(command.commandId)

        coVerify(exactly = 0) {
            manager.enqueueCommand(command )
        }
    }

    @Test
    fun `queueSize should return the approximate number of messages`() = runTest {
        every { sqsClient.getQueueAttributes(any<GetQueueAttributesRequest>()) } returns GetQueueAttributesResponse.builder()
            .attributes(mapOf(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES to "5"))
            .build()

        val size = manager.queueSize()

        assertEquals(5, size)
    }

   @Test
   fun `nextCommand should return null when no messages are available`() = runTest {
       every { sqsClient.receiveMessage(any<ReceiveMessageRequest>()) } returns ReceiveMessageResponse.builder()
           .messages(emptyList())
           .build()

       val result = manager.nextCommand()

       assertEquals(null, result)
   }
}
