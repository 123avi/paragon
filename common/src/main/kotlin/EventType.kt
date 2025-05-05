package org.paragontech

enum class EventType(val queueKey : String) {
    CHARGER_CONNECTED("charger.connected"),
    CHARGER_DISCONNECTED("charger.disconnected"),
    CHARGER_TELEMETRY("charger.telemetry");

    override fun toString(): String = queueKey

    companion object {
        fun fromString(value: String): EventType {
            return entries.firstOrNull { it.queueKey == value }
                ?: throw IllegalArgumentException("Unknown event type: $value")
        }
    }

}