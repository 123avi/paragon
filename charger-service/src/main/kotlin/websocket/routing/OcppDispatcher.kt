package org.paragontech.websocket.routing

import com.fasterxml.jackson.databind.ObjectMapper
import org.paragontech.notifications.AuthorizeReq
import org.paragontech.notifications.BootNotificationReq
import org.paragontech.ocpp.handlers.OcppMessageHandler
import org.paragontech.ocpp.massages.OcppAction
import org.paragontech.route.HandlerResponse
import com.fasterxml.jackson.core.type.TypeReference


class OcppDispatcher(
    private val handlers: Map<OcppAction, OcppMessageHandler<*>>,
    private val objectMapper: ObjectMapper
) {
    inline fun <reified T> ObjectMapper.parse(json : String): T =
        this.readValue(json, object : TypeReference<T>() {})

    fun dispatch(action: OcppAction, payload: String, messageId: String,chargerId: String): HandlerResponse {
        val handler = handlers[action] ?: return HandlerResponse.badRequest("No handler for action: ${action.value}")

        return try {
            @Suppress("UNCHECKED_CAST")
            when(action){
                OcppAction.BOOT_NOTIFICATION -> {
                    val request = objectMapper.parse<BootNotificationReq>(payload)
                    (handler as OcppMessageHandler<BootNotificationReq>).handle(request, messageId, chargerId)
                }
                OcppAction.AUTHORIZE -> {
                    val request = objectMapper.parse<AuthorizeReq>(payload)
                    (handler as OcppMessageHandler<AuthorizeReq>).handle (request, messageId, chargerId)
                }
                else -> {
                    HandlerResponse.notFound("No handler for action $action")
                }
            }
        } catch (e: Exception) {
            HandlerResponse.internalServerError("Error dispatching action $action: ${e.message}")
        }
    }



}