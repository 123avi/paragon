package org.paragontech.lambda

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent
import org.paragontech.Environment
import org.paragontech.EventType
import org.paragontech.gw.APIGatewayResponse


//@Component
class WebSocketIngressHandler(
    private val env: Environment
) {


    fun handle(event: APIGatewayV2WebSocketEvent): APIGatewayResponse {
        val routeKey = event.requestContext.routeKey
        val connectionId = event.requestContext.connectionId
        val publish = env.publish


        return try {
            when (routeKey) {
                "\$connect" -> {
                    val chargerId = event.queryStringParameters?.get("chargerId")
                        ?: return APIGatewayResponse.error("Missing chargerId")
                    val payload = """{"chargerId":"$chargerId","connectionId":"$connectionId"}"""
                    publish(EventType.CHARGER_CONNECTED, payload)
                }

                "\$disconnect" -> {
                    val payload = """{"connectionId":"$connectionId"}"""
                    publish(EventType.CHARGER_DISCONNECTED, payload)
                }

                else -> {
                    // e.g., telemetry or status update
                    val body = event.body ?: return APIGatewayResponse.error("Missing body")
                    publish(EventType.CHARGER_TELEMETRY, body)
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
