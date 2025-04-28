package charger

import io.mockk.*
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

        manager.enqueueCommand("CH-01", command)

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

        val result = manager.nextCommand("CH-01")

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
        manager.nextCommand("CH-01")


        every { sqsClient.deleteMessage(any<DeleteMessageRequest>()) } returns DeleteMessageResponse.builder().build()

        manager.acknowledgeCommand("CH-01", commandId)

        verify {
            sqsClient.deleteMessage(withArg<DeleteMessageRequest> {
                assertEquals("abc123", it.receiptHandle())
            })
        }
    }
}
