package org.paragontech.charger

import kotlinx.serialization.Serializable
import java.util.*

typealias ChargerId = String

@Serializable
data class CommandEnvelope(
    val commandId: String = UUID.randomUUID().toString(), // Required for dedup + tracking
    val chargerId: String,
    val commandType: CommandType,
    val payload: String,
    val retriesLeft: Int = 3,
    val timeoutMillis: Long = 10_000L
)