package org.paragontech.notifications

data class StopTransactionReq(
    val meterStop: Int,
    val timestamp: String,
    val transactionId: Int,
    val idTag: String? = null,
    val reason: String? = null
)