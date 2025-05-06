package org.paragontech.ocpp.massages

enum class OcppAction(val value: String) {
    BOOT_NOTIFICATION("BootNotification"),
    AUTHORIZE("Authorize"),
    START_TRANSACTION("StartTransaction"),
    STOP_TRANSACTION("StopTransaction"),
    STATUS_NOTIFICATION("StatusNotification"),
    METER_VALUES("MeterValues"),
    UNKNOWN("Unknown");

    companion object {
        fun fromString(action: String): OcppAction? =
            entries.find { it.value == action } ?: UNKNOWN
    }
}