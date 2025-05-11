package org.paragontech.charger.repository
import com.paragontech.generated.jooq.tables.references.CHARGERS
import com.paragontech.generated.jooq.tables.pojos.Chargers
import org.jooq.DSLContext
import org.paragontech.charger.model.Charger
import org.paragontech.typesaliases.ChargerId
import org.springframework.stereotype.Repository

@Repository
class ChargerRepositoryImp
    (private val dsl: DSLContext
)  : ChargerRepository {


    override fun findById(chargerId: ChargerId): Charger? =
        dsl.selectFrom(CHARGERS)
            .where(CHARGERS.ID.eq(id))
            .fetchOneInto(Chargers::class.java)


    override fun save(charger: Charger): Charger {
        TODO("Not yet implemented")
    }

    override fun delete(chargerId: String) {
        TODO("Not yet implemented")
    }

    override fun findAll(): List<Charger> {
        TODO("Not yet implemented")
    }

//}
}