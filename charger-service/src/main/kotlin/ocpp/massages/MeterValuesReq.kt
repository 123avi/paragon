package org.paragontech.notifications

data class MeterValuesReq(
    val connectorId: Int,
    val transactionId: Int? = null,
    val meterValue: List<MeterValue>
)