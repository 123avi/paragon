package websocket

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.paragontech.lambda.WebSocketIngressHandler
import org.paragontech.lambda.WebSocketRouterHandler
import org.paragontech.route.HandlerResponse
import org.junit.jupiter.api.BeforeEach


class WebSocketRouterHandlerTest {

    private lateinit var handler: WebSocketIngressHandler
    private lateinit var context: Context
    private lateinit var router: WebSocketRouterHandler

    @BeforeEach
    fun setup() {
        handler = mockk()
        context = mockk()
        router = WebSocketRouterHandler(handler)
    }

    @Test
    fun `should return 200 when handler returns success`(){
        val event = createEvent("\$default", """{"chargerId":"CH-01","type":"telemetry"}""")
        every { handler.handle(any()) } returns HandlerResponse.Success()

        val response = router.handleRequest(event, context)

        assertEquals(200, response["statusCode"])
        assertEquals("""{"status":"ok"}""", response["body"])
        assertEquals("application/json", (response["headers"] as Map<*, *>)["Content-Type"])

    }

    @Test
    fun `should return 500 when handler throws`() {
        val msg = "kaboom"
        val event = createEvent("\$default", """{"chargerId":"CH-01","type":"telemetry"}""")

        every { handler.handle(any()) } throws RuntimeException(msg)
        every { context.logger.log(any<String>()) } returns Unit

        // When
        val response = router.handleRequest(event, context)

        // Then
        assertEquals(500, response["statusCode"])
        assertTrue((response["body"] as String).contains(msg))
    }


    private fun createEvent(routeKey: String, body: String): APIGatewayV2WebSocketEvent {
        return APIGatewayV2WebSocketEvent().apply {
            requestContext = APIGatewayV2WebSocketEvent.RequestContext().apply {
               this.routeKey = routeKey
                this.connectionId = "abc123"
            }
            this.body = body
        }
    }
}