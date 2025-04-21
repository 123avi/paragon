package org.paragontech.charger

typealias ChargerId = String

data class CommandEnvelope(
    val chargerId: String,
    val commandType: String,
    val payload: String,
    val retriesLeft: Int = 3,
    val timeoutMillis: Long = 10_000L
)