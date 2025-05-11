package org.paragontech.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import org.paragontech.charger.ChargerServiceImp
import org.paragontech.ocpp.handlers.BootNotificationHandler
import org.paragontech.ocpp.handlers.OcppMessageHandler
import org.paragontech.ocpp.massages.OcppAction
import org.paragontech.publishers.SqsEventPublisher
import software.amazon.awssdk.services.sqs.SqsClient
import java.time.Clock

object OcppHandlerConfig {
    fun buildHandlers(objectMapper: ObjectMapper): Map<OcppAction, OcppMessageHandler<*>>{
        val clock: Clock = Clock.systemUTC()
        val sqsClient = SqsClient.create()
        val queueUrl = System.getenv("CHARGER_EVENT_QUEUE") ?: error("CHARGER_EVENT_QUEUE not set")
        val eventPublisher = SqsEventPublisher(sqsClient, queueUrl, objectMapper)
        val chargerService = ChargerServiceImp(eventPublisher)
        return mapOf(
            OcppAction.BOOT_NOTIFICATION to BootNotificationHandler(
                eventPublisher = eventPublisher,
                chargerService = chargerService,
                clock = clock,
                objectMapper = objectMapper
            )
        )


    }

}