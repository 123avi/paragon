package org.paragontech.charger

import org.paragontech.common.fp.FpQueue
import org.paragontech.common.fp.fpQueueOf

class InMemoryCommandQueueManager(
    private val state: Map<ChargerId, FpQueue<CommandEnvelope>> = emptyMap()
) {
    fun enqueue(command: CommandEnvelope): InMemoryCommandQueueManager {
        val currentQueue = state[command.chargerId] ?: fpQueueOf()
        val updatedQueue = currentQueue.enqueue(command)
        return InMemoryCommandQueueManager(state + (command.chargerId to updatedQueue))
    }

    fun dequeue(chargerId: ChargerId): InMemoryCommandQueueManager {
        val currentQueue = state[chargerId] ?: fpQueueOf()
        val updatedQueue = currentQueue.dequeue()
        return InMemoryCommandQueueManager(state + (chargerId to updatedQueue))
    }

    fun retry(chargerId: ChargerId): InMemoryCommandQueueManager {
        val queue = state[chargerId] ?: fpQueueOf()
        val command = queue.peek() ?: return this

        return if (command.retriesLeft > 0) {
            val retriedCommand = command.copy(retriesLeft = command.retriesLeft - 1)
            val updatedQueue = queue.dequeue().enqueue(retriedCommand)
            InMemoryCommandQueueManager(state + (chargerId to updatedQueue))
        } else {
            dequeue(chargerId)
        }
    }

        fun getQueue(chargerId: ChargerId): FpQueue<CommandEnvelope> = state[chargerId] ?: fpQueueOf()

        fun isEmpty(chargerId: ChargerId): Boolean = state[chargerId]?.isEmpty() ?: true



}