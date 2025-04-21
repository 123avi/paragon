package org.paragontech.charger

import org.paragontech.common.fp.FpQueue
import org.paragontech.common.fp.fpQueueOf

class CommandQueueManager(
    private val state: Map<ChargerId, FpQueue<CommandEnvelope>> = emptyMap()
) {
    fun enqueue(command: CommandEnvelope): CommandQueueManager {
        val currentQueue = state[command.chargerId] ?: fpQueueOf()
        val updatedQueue = currentQueue.enqueue(command)
        return CommandQueueManager(state + (command.chargerId to updatedQueue))
    }

    fun dequeue(chargerId: ChargerId): CommandQueueManager {
        val currentQueue = state[chargerId] ?: fpQueueOf()
        val updatedQueue = currentQueue.dequeue()
        return CommandQueueManager(state + (chargerId to updatedQueue))
    }

    fun retry(chargerId: ChargerId): CommandQueueManager {
        val queue = state[chargerId] ?: fpQueueOf()
        val command = queue.peek() ?: return this

        return if (command.retriesLeft > 0) {
            val retriedCommand = command.copy(retriesLeft = command.retriesLeft - 1)
            val updatedQueue = queue.dequeue().enqueue(retriedCommand)
            CommandQueueManager(state + (chargerId to updatedQueue))
        } else {
            dequeue(chargerId)
        }
    }

        fun getQueue(chargerId: ChargerId): FpQueue<CommandEnvelope> = state[chargerId] ?: fpQueueOf()

        fun isEmpty(chargerId: ChargerId): Boolean = state[chargerId]?.isEmpty() ?: true



}