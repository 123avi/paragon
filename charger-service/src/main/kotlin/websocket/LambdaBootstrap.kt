package org.paragontech.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.paragontech.Environment
import org.paragontech.EventType
import org.paragontech.routing.OcppDispatcher
import org.paragontech.store.ConnectionStore
import org.paragontech.store.InMemoryConnectionStore
import software.amazon.awssdk.services.sqs.SqsClient

class LambdaBootstrap : RequestHandler<APIGatewayV2WebSocketEvent, Map<String, Any>>{

    private val objectMapper = jacksonObjectMapper()

    //todo wire via DI
    private val connectionStore: ConnectionStore = InMemoryConnectionStore()
    private val dispatcher = OcppDispatcher(
        handlers = OcppHandlerConfig.buildHandlers(),
        objectMapper = objectMapper
    )

    private val handler = WebSocketIngressHandler(dispatcher, objectMapper, connectionStore)

    override fun handleRequest(p0: APIGatewayV2WebSocketEvent?, p1: Context?): Map<String, Any>? {
        TODO("Not yet implemented")
    }

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