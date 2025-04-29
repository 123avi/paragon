package org.paragontech.charger

interface DistributedCommandQueue {
    suspend fun enqueueCommand(command: CommandEnvelope)
    suspend fun nextCommand(): CommandEnvelope?
    suspend fun acknowledgeCommand(commandId: String)
    suspend fun retryCommand(commandId: String)
    suspend fun currentCommand(commandId: String): CommandEnvelope?
    suspend fun queueSize(): Int
}
