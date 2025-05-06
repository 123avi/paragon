package org.paragontech.notifications

data class IdTagInfo(
    val status: String,
    val expiryDate: String? = null,
    val parentIdTag: String? = null
)
