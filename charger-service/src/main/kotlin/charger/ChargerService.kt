package org.paragontech.charger
/*
domain service responsible for handling business logic related to chargers — especially when one connects, sends metadata, or updates status.
 */
interface ChargerService {
    fun registerCharger(metadata: ChargerMetadata)
    fun sendCommand(command: CommandEnvelope)
//    fun updateConnectionState(chargerId: String, state: ConnectionState)
//    fun getChargerStatus(chargerId: String): ChargerStatus
}