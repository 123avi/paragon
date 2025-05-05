package org.paragontech.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent

class WebSocketRouterHandler: RequestHandler<APIGatewayV2WebSocketEvent, Map<String, Any>> {

    private val handler = LambdaBootstrap().webSocketIngressHandler()

    override fun handleRequest(
        input: APIGatewayV2WebSocketEvent,
        context: Context
    ): Map<String, Any>? {
        val response = handler.handle(input)
        return mapOf(
            "statusCode" to response.statusCode,
            "body" to response.body,
            "headers" to response.headers
        )
    }
}