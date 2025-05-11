package org.paragontech.charger

import com.fasterxml.jackson.annotation.JsonProperty
import org.paragontech.typesaliases.ChargerId
import org.paragontech.typesaliases.CommandId
import java.util.*


data class CommandEnvelope(
    @JsonProperty("commandId") val commandId: CommandId = UUID.randomUUID().toString(), // Required for dedup + tracking
    @JsonProperty("chargerId") val chargerId: ChargerId,
    @JsonProperty("commandType") val commandType: CommandType,
    @JsonProperty("payload") val payload:  Map<String, Any>,
    val retriesLeft: Int = 3,
    val timeoutMillis: Long = 10_000L
)

