package org.paragontech.charger

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.time.Instant
import mu.KotlinLogging


@Service
class CommandQueueManager (

    private val commandQueueService: DistributedCommandQueue,
    private val objectMapper: ObjectMapper
){
    private val logger = KotlinLogging.logger {}

    suspend fun queueRemoteStartTransaction(
        chargerId: String,
        idTag: String
    ) {
        val payload = buildStartTransactionPayload(idTag)
        val cmd = CommandEnvelope(chargerId = chargerId, commandType = CommandType.StartTransaction, payload = payload)

        logger.info { "Enqueuing command: $cmd" }

        commandQueueService.enqueueCommand(cmd)
    }

    fun buildStartTransactionPayload(
        idTag: String,
        connectorId: Int = 1,
        timestamp: String = Instant.now().toString()
    ): Map<String, Any> {
        val payload = mapOf(
            "connectorId" to connectorId,
            "idTag" to idTag,
            "timestamp" to timestamp
        )
        return payload
    }

}