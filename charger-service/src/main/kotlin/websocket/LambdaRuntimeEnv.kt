package org.paragontech.websocket

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.paragontech.lambda.WebSocketIngressHandler
import org.paragontech.websocket.routing.OcppDispatcher
import org.paragontech.store.InMemoryConnectionStore

/*
instantiate and wire dependencies (like ObjectMapper, ConnectionStore, etc.).
 */
class LambdaRuntimeEnv {
    private val objectMapper = jacksonObjectMapper()
    private val connectionStore = InMemoryConnectionStore()

    val handler = WebSocketIngressHandler(
        dispatcher = OcppDispatcher(
            handlers = OcppHandlerConfig.buildHandlers(objectMapper),
            objectMapper = objectMapper
        ),
        objectMapper = objectMapper,
        connectionStore = connectionStore
    )
}