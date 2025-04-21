package org.paragontech.event.handler

interface EventPublisher {
    fun publish(topic: String, message: String)
}