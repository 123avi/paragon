package org.paragontech.charger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.*

class SqsCommandQueueManager(
    private val queueUrl: String,
    private val sqsClient: SqsClient,
    private val json: Json = Json
) : DistributedCommandQueue {

    private val inFlightCommands: MutableMap<String, String> = mutableMapOf()
    private val currentCommandCache: MutableMap<String, CommandEnvelope> = mutableMapOf()

    override suspend fun enqueueCommand(chargerId: String, command: CommandEnvelope) {
        val messageBody = json.encodeToString(command)

        val request = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageGroupId(chargerId)
            .messageDeduplicationId(command.commandId)
            .messageBody(messageBody)
            .build()

        sqsClient.sendMessage(request)
    }

    override suspend fun nextCommand(chargerId: String): CommandEnvelope? {

        val request = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(1)
            .waitTimeSeconds(5)
            .messageAttributeNames("All")
            .build()

        val messages = sqsClient.receiveMessage(request).messages()
        if (messages.isEmpty()) return null

        val msg = messages.first()
        val command = json.decodeFromString<CommandEnvelope>(msg.body())

        // Keep the receipt handle for later deletion
        inFlightCommands[command.commandId] = msg.receiptHandle()

        return command
    }

    override suspend fun acknowledgeCommand(chargerId: String, commandId: String) {
        val receiptHandle = inFlightCommands.remove(commandId) ?: return

        val deleteRequest = DeleteMessageRequest.builder()
            .queueUrl(queueUrl)
            .receiptHandle(receiptHandle)
            .build()

        sqsClient.deleteMessage(deleteRequest)
    }

    override suspend fun retryCommand(chargerId: String, commandId: String) {
        val receiptHandle = inFlightCommands.remove(commandId) ?: return
        val originalCommand = currentCommandCache.remove(commandId) ?: return

        // Delete the failed message from SQS
        val deleteRequest = DeleteMessageRequest.builder()
            .queueUrl(queueUrl)
            .receiptHandle(receiptHandle)
            .build()
        sqsClient.deleteMessage(deleteRequest)

        // Retry only if retries are left
        if (originalCommand.retriesLeft > 0) {
            val retriedCommand = originalCommand.copy(retriesLeft = originalCommand.retriesLeft - 1)
            enqueueCommand(chargerId, retriedCommand)
        }
    }

    override suspend fun currentCommand(chargerId: String): CommandEnvelope? {
        return currentCommandCache.values.find { it.chargerId == chargerId }
    }

    override suspend fun queueSize(chargerId: String): Int {
        val attributes = sqsClient.getQueueAttributes(
            GetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributeNames(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES)
                .build()
        ).attributes()

        return attributes[QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES]?.toIntOrNull() ?: 0
    }

}
