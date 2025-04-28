package org.paragontech.charger

interface DistributedCommandQueue {
    suspend fun enqueueCommand(chargerId: String, command: CommandEnvelope)
    suspend fun nextCommand(chargerId: String): CommandEnvelope?
    suspend fun acknowledgeCommand(chargerId: String, commandId: String)
    suspend fun retryCommand(chargerId: String, commandId: String)
    suspend fun currentCommand(chargerId: String): CommandEnvelope?
    suspend fun queueSize(chargerId: String): Int
}
