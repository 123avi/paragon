package org.paragontech.charger

class ChargerServiceImp(
    private val commandQueueService: DistributedCommandQueue,
    private val chargerRepo: ChargerRepository,
    private val connectionRegistry: ChargerConnectionRegistry,
    private val ocppCommandSendr: OcppCommandSender): ChargerService {
        override fun registerCharger(metadata: ChargerMetadata) {}
        override fun sendCommand(command: CommandEnvelope) {
            val connection = connectionRegistry
        }
}