package org.paragontech.event.handler

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent
import org.springframework.stereotype.Component
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@Component
class WebSocketIngressHandler(
    private val eventPublisher: EventPublisher
) {
    private val objectMapper = jacksonObjectMapper()

    fun handle(event: APIGatewayV2WebSocketEvent): APIGatewayResponse {
        val routeKey = event.requestContext.routeKey
        val connectionId = event.requestContext.connectionId

        return try {
            when (routeKey) {
                "\$connect" -> {
                    val chargerId = extractChargerId(event)
                    val payload = mapOf("chargerId" to chargerId, "connectionId" to connectionId)
                    eventPublisher.publish("charger.connected", objectMapper.writeValueAsString(payload))
                }

                "\$disconnect" -> {
                    val payload = mapOf("connectionId" to connectionId)
                    eventPublisher.publish("charger.disconnected", objectMapper.writeValueAsString(payload))
                }

                else -> {
                    // e.g., telemetry or status update
                    val payload = event.body ?: "{}"
                    eventPublisher.publish("charger.telemetry", payload)
                }
            }

            APIGatewayResponse.ok()
        } catch (ex: Exception) {
            ex.printStackTrace()
            APIGatewayResponse.error("Handler error: ${ex.message}")
        }
    }

    private fun extractChargerId(event: APIGatewayV2WebSocketEvent): String {
        return event.queryStringParameters?.get("chargerId")
            ?: throw IllegalArgumentException("Missing chargerId in queryStringParameters")
    }
}
