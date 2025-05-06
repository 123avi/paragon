package org.paragontech.publishers

import org.paragontech.EventType

interface EventPublisher {
    fun publish(eventType: EventType, payload: Any)
}