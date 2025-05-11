package org.paragontech.charger.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "chargers")
data class Charger (
    @Id val id: String,
    val name: String,
    val location: String,
    val status: String,
    val vendor: String,
    val firmware: String?,
    val model: String?,
    val lastSeen: Long? = null

)
