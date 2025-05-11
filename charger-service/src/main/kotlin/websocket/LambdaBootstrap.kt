package org.paragontech.websocket

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent

/*
AWS Lambda entry point for handling WebSocket events.
 */
class LambdaBootstrap : RequestHandler<APIGatewayV2WebSocketEvent, Map<String, Any>>{

        override fun handleRequest(input: APIGatewayV2WebSocketEvent, context: Context): Map<String, Any> {
            return LambdaRuntimeEnv().handler.handle(input).toApiGatewayResponse()
        }
}