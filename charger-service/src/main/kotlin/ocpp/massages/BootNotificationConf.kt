package org.paragontech.notifications

data class BootNotificationConf(
    val currentTime: String,
    val interval: Int,
    val status: String
)