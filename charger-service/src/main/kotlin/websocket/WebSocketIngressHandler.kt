package org.paragontech.lambda

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import org.paragontech.Environment
import org.paragontech.notifications.AuthorizeReq
import org.paragontech.notifications.BootNotificationReq
import org.paragontech.ocpp.massages.MessageType.CALL
import org.paragontech.route.HandlerResponse
import org.paragontech.ocpp.massages.OcppAction
import org.paragontech.routing.OcppDispatcher
import org.paragontech.store.ConnectionStore

//@Component
class WebSocketIngressHandler(
    private val dispatcher: OcppDispatcher,
    private val objectMapper: ObjectMapper,
    private val connectionStore: ConnectionStore // Interface to look up chargerId by connectionId
) {


    inline fun <reified T> ObjectMapper.parse(json: String): T =
        this.readValue(json, object : TypeReference<T>() {})

    fun handle(event: APIGatewayV2WebSocketEvent): HandlerResponse{
        return try {
            val routeKey = event.requestContext.routeKey
            return when (routeKey) {
                "\$connect" -> handleConnect(event)
                "\$disconnect" -> handleDisconnect(event)
                else -> handleDefault(event)
            }
        } catch (e: Exception){
            HandlerResponse.internalServerError("Error handling websocket route event: ${e.message}")
        }
    }

    fun handleDefault(event: APIGatewayV2WebSocketEvent): HandlerResponse {
        return try {
            val connectionId = extractConnectionId(event)
            val chargerId = connectionStore.getChargerId(connectionId)
                ?: return HandlerResponse.badRequest("Unknown connectionId: $connectionId")
            val rawBody = event.body ?: return HandlerResponse.badRequest("Missing body")
            val message = objectMapper.parse<List<Any>>(rawBody)
            val messageTypeId = (message[0] as Number).toInt()
            val messageId = message[1] as String
            val actionStr = message[2] as String
            val action = OcppAction.fromString(actionStr) ?: return HandlerResponse.badRequest("Unsupported action: $actionStr")
            val payloadJson = jacksonObjectMapper().writeValueAsString(message[3])
            return when(messageTypeId){
                CALL -> dispatcher.dispatch(action, payloadJson, messageId, chargerId)
                else -> HandlerResponse.badRequest("unsupported message type : $messageTypeId")
            }

        } catch (ex: Exception) {
            HandlerResponse.internalServerError("Error handling websocket event: ${ex.message}")
        }

    }
    fun handleConnect(event: APIGatewayV2WebSocketEvent): HandlerResponse {
        val connectionId = extractConnectionId(event)
        val chargerId = extractChargerId(event)

        connectionStore.storeConnection(connectionId, chargerId)
        return HandlerResponse.Success()

    }

    fun handleDisconnect(event: APIGatewayV2WebSocketEvent): HandlerResponse {
        val connectionId = extractConnectionId(event)

        connectionStore.removeConnection(connectionId)
        return HandlerResponse.Success()

    }

    private fun extractChargerId(event: APIGatewayV2WebSocketEvent): String {
        return event.queryStringParameters?.get("chargerId")
            ?: throw IllegalArgumentException("Missing chargerId in queryStringParameters")
    }

    private fun extractConnectionId(event: APIGatewayV2WebSocketEvent): String {
        return event.requestContext.connectionId
            ?: throw IllegalArgumentException("Missing connectionId on connect")

    }
}
