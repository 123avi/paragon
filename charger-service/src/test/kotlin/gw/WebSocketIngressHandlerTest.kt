package gw
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent
import com.paragontech.TestEnvironment
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.paragontech.Environment
import org.paragontech.lambda.WebSocketIngressHandler
import java.util.concurrent.atomic.AtomicReference

@ExtendWith(MockKExtension::class)
class WebSocketIngressHandlerTest {

//    private lateinit var publishFn: (String, String) -> Unit
    private lateinit var fakeEnv: Environment
    private lateinit var captured: AtomicReference<Pair<String, String>?>
    private lateinit var handler: WebSocketIngressHandler

    @BeforeEach
    fun setup() {
        // mock the lambda (function2)
//        publishFn = mockk(relaxed = true)
        captured = AtomicReference()
        val fakeEnv = TestEnvironment.withCapturedPublisher(captured)
        handler = WebSocketIngressHandler(fakeEnv)
    }

    @Test
    fun `should publish to charger_connected on connect`() {
        val event = eventWithRoute("\$connect", mapOf("chargerId" to "CH-01"))

        val response = handler.handle(event)

        val (topic, message) = captured.get()!!
        assertEquals(200, response.statusCode)
        assertEquals("charger.connected", topic)
        assert(message.contains("CH-01"))
        assert(message.contains("abc123"))
        assertEquals(200, response.statusCode)
    }

    @Test
    fun `should publish to charger_disconnected on disconnect`() {
        val event = eventWithRoute("\$disconnect")

        val response = handler.handle(event)

        val (topic, message) = captured.get()!!

        assertEquals("charger.disconnected", topic)
        assert(message.contains("abc123"))
        assertEquals(200, response.statusCode)
    }

    @Test
    fun `should publish to charger_telemetry on default route`() {
        val payload = """{"chargerId":"CH-01","type":"telemetry","data":{"voltage":230}}"""
        val event = eventWithRoute("\$default", mapOf("chargerId" to "CH-01")).apply {
            body = payload
        }

        val response = handler.handle(event)

        val (topic, message) = captured.get()!!
        assertEquals("charger.telemetry", topic)
        assertEquals(payload, message)
        assertEquals(200, response.statusCode)
    }

    @Test
    fun `should return 500 if chargerId is missing on connect`() {
        val event = eventWithRoute("\$connect", null)

        val response = handler.handle(event)

        assertEquals(500, response.statusCode)
        assert(response.body.contains("Missing chargerId"))
    }

    private fun eventWithRoute(
        route: String,
        queryParams: Map<String, String>? = null
    ): APIGatewayV2WebSocketEvent {
        return APIGatewayV2WebSocketEvent().apply {
            requestContext = APIGatewayV2WebSocketEvent.RequestContext().apply {
                routeKey = route
                connectionId = "abc123"
            }
            queryStringParameters = queryParams
        }
    }
}
