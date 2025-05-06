package org.paragontech.notifications

data class BootNotificationReq(
    val chargePointModel: String,
    val chargePointVendor: String,
    val firmwareVersion: String? = null,
    val chargeBoxSerialNumber: String? = null,
    val chargePointSerialNumber: String? = null,
    val iccid: String? = null,
    val imsi: String? = null,
    val meterType: String? = null,
    val meterSerialNumber: String? = null
)