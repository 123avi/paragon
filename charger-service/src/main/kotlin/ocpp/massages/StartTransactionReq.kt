package org.paragontech.notifications

data class StartTransactionReq(
    val connectorId: Int,
    val idTag: String,
    val meterStart: Int,
    val timestamp: String
)
