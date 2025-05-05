package org.paragontech.lambda

import org.paragontech.Environment
import software.amazon.awssdk.services.sqs.SqsClient

class LambdaBootstrap{
    fun webSocketIngressHandler(): WebSocketIngressHandler {
        return WebSocketIngressHandler(env = productionEnvironment() )
    }

    private fun productionEnvironment(): Environment {
        val sqsClient = SqsClient.create()
        val queueUrls = mapOf(
            "charger.connected" to System.getenv("CHARGER_CONNECTED_QUEUE_URL"),
            "charger.disconnected" to System.getenv("CHARGER_DISCONNECTED_QUEUE_URL"),
            "charger.telemetry" to System.getenv("CHARGER_TELEMETRY_QUEUE_URL")
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