package org.paragontech.ocpp.handlers

import org.paragontech.route.HandlerResponse

interface OcppMessageHandler<T> {
    fun handle(request:T, message: String, chargerId: String): HandlerResponse
}