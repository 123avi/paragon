package org.paragontech.ocpp.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import org.paragontech.EventType
import org.paragontech.charger.ChargerMetadata
import org.paragontech.charger.ChargerService
import org.paragontech.notifications.BootNotificationReq
import org.paragontech.ocpp.massages.MessageType
import org.paragontech.ocpp.massages.MessageType.CALL
import org.paragontech.ocpp.massages.MessageType.CALL_RESULT
import org.paragontech.ocpp.massages.OcppAction
import org.paragontech.publishers.EventPublisher
import org.paragontech.route.HandlerResponse
import java.time.Clock

class BootNotificationHandler(
    private val eventPublisher: EventPublisher,
    private val chargerService: ChargerService,
    private val clock: Clock,
    private val objectMapper: ObjectMapper,
  ): OcppMessageHandler<BootNotificationReq> {

    override fun handle(request: BootNotificationReq, messageId: String, chargerId: String): HandlerResponse {
        val metadata = ChargerMetadata(
            chargerId = chargerId,
            model = request.chargePointModel,
            vendor = request.chargePointVendor,
            firmware = request.firmwareVersion
        )

        chargerService.registerCharger(metadata)
        eventPublisher.publish(EventType.CHARGER_CONNECTED, metadata)

        val response = listOf( CALL_RESULT
            , messageId, mapOf(
                "currentTime" to clock.instant().toString(),
                "interval" to 300,
                "status" to "Accepted"
            ))

        return HandlerResponse.Success(body = objectMapper.writeValueAsString(response))

    }
}