package org.paragontech.charger

import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.util.UUID

class SqsCommandQueueService(
    private val sqsClient: SqsClient,
    private val queueUrl: String
): CommandQueueService {
    override fun enqueueCommand(chargerId: String, payload: String) {
        val request = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(payload)
            .messageGroupId(chargerId)
            .messageDeduplicationId(UUID.randomUUID().toString())
            .build()
        sqsClient.sendMessage(request)
    }
}