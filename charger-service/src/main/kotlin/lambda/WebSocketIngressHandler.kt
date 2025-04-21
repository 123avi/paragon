package org.paragontech.lambda

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.paragontech.gw.APIGatewayResponse

//import org.springframework.boot.availability.AvailabilityChangeEvent.publish


//@Component
class WebSocketIngressHandler(
    private val publish: (topic: String, payload: String) -> Unit
) {
    private val objectMapper = jacksonObjectMapper()

    fun handle(event: APIGatewayV2WebSocketEvent): APIGatewayResponse {
        val routeKey = event.requestContext.routeKey
        val connectionId = event.requestContext.connectionId


        return try {
            when (routeKey) {
                "\$connect" -> {
                    val chargerId = event.queryStringParameters?.get("chargerId")
                        ?: return APIGatewayResponse.error("Missing chargerId")
                    val payload = """{"chargerId":"$chargerId","connectionId":"$connectionId"}"""
                    publish("charger.connected", payload)
                }

                "\$disconnect" -> {
                    val payload = """{"connectionId":"$connectionId"}"""
                    publish("charger.disconnected", payload)
                }

                else -> {
                    // e.g., telemetry or status update
                    val body = event.body ?: return APIGatewayResponse.error("Missing body")
                    publish("charger.telemetry", body)
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
