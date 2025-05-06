package org.paragontech.notifications

data class StartTransactionConf(
    val transactionId: Int,
    val idTagInfo: IdTagInfo
)