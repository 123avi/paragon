package org.paragontech.charger.repository

import org.paragontech.charger.model.Charger
import org.paragontech.typesaliases.ChargerId

class ChargerDao {
    fun getChargerById(id: ChargerId): Chargers? {
        // Simulate a database lookup
        return Charger(id, "Charger $id", "Available")
    }

    fun saveCharger(charger: Charger) {
        // Simulate saving to a database
        println("Charger saved: $charger")
    }
}