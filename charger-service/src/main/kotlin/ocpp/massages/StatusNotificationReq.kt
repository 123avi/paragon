package org.paragontech.notifications

data class StatusNotificationReq(
    val connectorId: Int,
    val errorCode: String,
    val status: String,
    val info: String? = null,
    val timestamp: String? = null,
    val vendorId: String? = null,
    val vendorErrorCode: String? = null
)