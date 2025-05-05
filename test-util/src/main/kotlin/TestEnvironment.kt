package com.paragontech

import org.paragontech.Environment
import org.paragontech.EventType
import java.util.concurrent.atomic.AtomicReference

object TestEnvironment {

    fun withCapturedPublisher(capture: AtomicReference<Pair<EventType, String>?>): Environment {
        return Environment { topic, message ->
            capture.set(topic to message)
        }
    }

    fun noop(): Environment {
        return Environment { _, _ -> }
    }
}