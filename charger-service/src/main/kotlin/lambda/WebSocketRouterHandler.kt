package org.paragontech.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent
import org.paragontech.route.HandlerResponse

class WebSocketRouterHandler(
    private val handler: WebSocketIngressHandler  = LambdaBootstrap().webSocketIngressHandler()
) : RequestHandler<APIGatewayV2WebSocketEvent, Map<String, Any>> {

    override fun handleRequest(input: APIGatewayV2WebSocketEvent, context: Context): Map<String, Any> {
        return try {
            handler.handle(input).toApiGatewayResponse()
        } catch (e: Exception) {
            val message = "Unhandled exception: ${e.message}"
            context.logger.log(message)
           HandlerResponse.internalServerError(message).toApiGatewayResponse()
        }
    }
}