package lambda

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.paragontech.lambda.WebSocketIngressHandler


@ExtendWith(MockKExtension::class)
class WebSocketIngressHandlerTest {

    private val publishFn = mockk<(String, String) -> Unit>(relaxed = true)
    private lateinit var handler: WebSocketIngressHandler


    @BeforeEach
    fun setup() {
        handler = WebSocketIngressHandler(publishFn)
    }

    @Test
    fun `should publish to charger_connected on connect`() {
        val event = eventWithRoute("\$connect", mapOf("chargerId" to "CH-01"))

        val response = handler.handle(event)

        verify {
            publishFn("charger.connected", match { it.contains("CH-01") && it.contains("abc123") })
        }

        assertEquals(200, response.statusCode)
    }

    @Test
    fun `should publish to charger_disconnected on disconnect`() {
        val event = eventWithRoute("\$disconnect", mapOf("chargerId" to "CH-01"))

        val response = handler.handle(event)

        verify {
            publishFn("charger.disconnected", match { it.contains("abc123") })
        }
        assertEquals(200, response.statusCode)
    }

    @Test
    fun `should publish to charger_telemetry on default route`() {
        val payload = """{"chargerId":"CH-01","type":"telemetry","data":{"voltage":230}}"""
        val event = eventWithRoute("\$default", mapOf("chargerId" to "CH-01")).apply {
            body = payload
        }

        val response = handler.handle(event)

        verify {
            publishFn("charger.telemetry", payload)
        }
        assertEquals(200, response.statusCode)
    }

    @Test
    fun `should return 500 if chargerId is missing`() {
        val event = eventWithRoute("\$connect").apply {
            queryStringParameters = null
        }

        val response = handler.handle(event)

        assertEquals(500, response.statusCode)
        assert(response.body.contains("Missing chargerId"))
    }

    private fun eventWithRoute(route: String, queryParams: Map<String, String>? = null): APIGatewayV2WebSocketEvent {
        return APIGatewayV2WebSocketEvent().apply {
            requestContext = APIGatewayV2WebSocketEvent.RequestContext().apply {
                routeKey = route
                connectionId = "abc123"
            }
            queryStringParameters = queryParams
        }
    }
}
