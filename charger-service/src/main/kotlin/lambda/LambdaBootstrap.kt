package org.paragontech.lambda

import org.paragontech.Environment
import org.paragontech.EventType
import software.amazon.awssdk.services.sqs.SqsClient

class LambdaBootstrap{
    fun webSocketIngressHandler(): WebSocketIngressHandler {
        return WebSocketIngressHandler(env = productionEnvironment() )
    }

    private fun productionEnvironment(): Environment {
        val sqsClient = SqsClient.create()
        val queueUrls = mapOf(
            EventType.CHARGER_CONNECTED to System.getenv("CHARGER_CONNECTED_QUEUE_URL"),
            EventType.CHARGER_DISCONNECTED to System.getenv("CHARGER_DISCONNECTED_QUEUE_URL"),
            EventType.CHARGER_TELEMETRY to System.getenv("CHARGER_TELEMETRY_QUEUE_URL")
        )

        return Environment { topic, message ->
            val queueUrl = queueUrls[topic]
                ?: throw IllegalArgumentException("Unknown topic: $topic")
            sqsClient.sendMessage {
                it.queueUrl(queueUrl).messageBody(message)
            }
        }
    }

}