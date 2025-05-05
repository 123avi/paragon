package org.paragontech

data class Environment(
    val publish: (eventType: EventType, payload: String) -> Unit
)