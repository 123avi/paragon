package event.handler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent
import io.mockk.*
//import io.mockk.MockK
//import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.InjectMockKs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import io.mockk.junit5.MockKExtension
import org.paragontech.event.handler.EventPublisher
import org.paragontech.event.handler.WebSocketIngressHandler

@ExtendWith(MockKExtension::class)
class WebSocketIngressHandlerTest {

    @MockK
    lateinit var eventPublisher: EventPublisher

    @InjectMockKs
    lateinit var handler: WebSocketIngressHandler

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        clearAllMocks()
    }

    @Test
    fun `should register charger on CONNECT`() {
        val connectionId = "abc123"
        val chargerId = "CH-01"
        val event = createWebSocketEvent(
            routeKey = "\$connect",
            connectionId = connectionId,
            queryParams = mapOf("chargerId" to chargerId)
        )

        every { eventPublisher.publish(any(), any()) } just Runs

        val response = handler.handle(event)

        verify {
            eventPublisher.publish("charger.connected", withArg {
                assert(it.contains("CH-01"))
                assert(it.contains("abc123"))
            })
        }
        assertEquals(200, response.statusCode)
    }

    @Test
    fun `should unregister charger on DISCONNECT`() {
        val connectionId = "abc123"
        val event = createWebSocketEvent(routeKey = "\$disconnect", connectionId = connectionId)

        every { eventPublisher.publish(any(), any()) } just Runs

        val response = handler.handle(event)

        verify {
            eventPublisher.publish("charger.disconnected", match { it.contains(connectionId) })
        }
        assertEquals(200, response.statusCode)
    }

    @Test
    fun `should publish telemetry on default route`() {
        val body = """{"chargerId":"CH-01","type":"telemetry","data":{"voltage":230}}"""
        val event = createWebSocketEvent(
            routeKey = "\$default",
            connectionId = "abc123",
            body = body
        )

        every { eventPublisher.publish(any(), any()) } just Runs

        val response = handler.handle(event)

        verify {
            eventPublisher.publish("charger.telemetry", match { it.contains("voltage") && it.contains("CH-01") })
        }
        assertEquals(200, response.statusCode)
    }

    private fun createWebSocketEvent(
        routeKey: String,
        connectionId: String,
        body: String? = null,
        queryParams: Map<String, String> = emptyMap()
    ): APIGatewayV2WebSocketEvent {
        return APIGatewayV2WebSocketEvent().apply {
            this.requestContext = APIGatewayV2WebSocketEvent.RequestContext().apply {
                this.routeKey = routeKey
                this.connectionId = connectionId
            }
            this.queryStringParameters = queryParams
            this.body = body
        }
    }
}
