package org.paragontech.charger.repository

import com.paragontech.generated.jooq.tables.references.CHARGERS
import com.paragontech.generated.jooq.tables.pojos.Chargers
import org.springframework.stereotype.Repository
import org.jooq.DSLContext
import org.paragontech.charger.model.Charger
import org.paragontech.typesaliases.ChargerId

interface ChargerRepository {
    fun findById(chargerId: String): Charger?
    fun save(charger: Charger): Charger
    fun delete(chargerId: String)
    fun findAll(): List<Charger>
}


//@Repository
//class ChargerRepository(
//    private val dsl: DSLContext
//) {
//    fun getChargerId(id: ChargerId): Chargers? =
//        dsl.selectFrom(CHARGERS)
//            .where(CHARGERS.ID.eq(id))
//            .fetchOneInto(Chargers::class.java)
//
//
//    fun getChargerStatus(): String {
//        // Simulate fetching the charger status from a database or configuration
//        return "Available"
//    }
//
//    fun getChargerLocation(): String {
//        // Simulate fetching the charger location from a database or configuration
//        return "Location A"
//    }
//
//}