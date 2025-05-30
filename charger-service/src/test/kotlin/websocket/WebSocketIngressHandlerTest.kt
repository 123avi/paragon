package websocket

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent
import com.paragontech.TestEnvironment
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.paragontech.EventType
import org.paragontech.lambda.WebSocketIngressHandler
import org.paragontech.route.ErrorType
import java.util.concurrent.atomic.AtomicReference


@ExtendWith(MockKExtension::class)
class WebSocketIngressHandlerTest {

    private lateinit var captured: AtomicReference<Pair<EventType, String>?>
    private lateinit var handler: WebSocketIngressHandler


    @BeforeEach
    fun setup() {
        captured = AtomicReference()
        val env = TestEnvironment.withCapturedPublisher(captured)
        handler = WebSocketIngressHandler(env)
    }

    @Test
    fun `should publish to charger_connected on connect`() {
        val event = eventWithRoute("\$connect", mapOf("chargerId" to "CH-01"))

        val response = handler.handle(event)

        val (topic, message) = captured.get()!!
        assertEquals(200, response.statusCode)
        assertEquals(EventType.CHARGER_CONNECTED, topic)
        assert(message.contains("CH-01"))
        assert(message.contains("abc123"))
    }

    @Test
    fun `should publish to charger_disconnected on disconnect`() {
        val event = eventWithRoute("\$disconnect", mapOf("chargerId" to "CH-01"))

        val response = handler.handle(event)

        val (topic, message) = captured.get()!!
        assertEquals(200, response.statusCode)
        assertEquals(EventType.CHARGER_DISCONNECTED, topic)
        assert(message.contains("abc123"))
    }

    @Test
    fun `should publish to charger_telemetry on default route`() {
        val payload = """{"chargerId":"CH-01","type":"telemetry","data":{"voltage":230}}"""
        val event = eventWithRoute("\$default", mapOf("chargerId" to "CH-01")).apply {
            body = payload
        }

        val response = handler.handle(event)

        val (topic, message) = captured.get()!!
        assertEquals(200, response.statusCode)
        assertEquals(EventType.CHARGER_TELEMETRY, topic)
        assertEquals(payload, message)
    }

    @Test
    fun `should return 400 if chargerId is missing`() {
        val event = eventWithRoute("\$connect")

        val response = handler.handle(event)

        assertEquals(ErrorType.BAD_REQUEST.code, response.statusCode)
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
