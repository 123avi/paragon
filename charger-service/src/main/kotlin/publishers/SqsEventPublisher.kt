package org.paragontech.publishers

import com.fasterxml.jackson.databind.ObjectMapper
import org.paragontech.EventType
import software.amazon.awssdk.services.sqs.SqsClient
import java.util.UUID

class SqsEventPublisher(
    private val sqsClient: SqsClient,
    private val queueUrl: String,
    private val objectMapper: ObjectMapper
): EventPublisher {
    override fun publish(eventType: EventType, payload: Any) {
        val message = objectMapper.writeValueAsString(payload)
        sqsClient.sendMessage {
            it.queueUrl(queueUrl)
                .messageBody(message)
                .messageGroupId(eventType.name)
                .messageDeduplicationId(UUID.randomUUID().toString())
        }
    }
}