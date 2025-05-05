package lambda

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.paragontech.Environment
import org.paragontech.EventType
import org.paragontech.lambda.WebSocketIngressHandler
import java.util.concurrent.atomic.AtomicReference

class LambdaBootstrapTest {
    private lateinit var fakeEnv: Environment
    private lateinit var handler: WebSocketIngressHandler

    @BeforeEach
    fun setup() {
        fakeEnv = Environment(publish = mockk(relaxed = true))
        handler = WebSocketIngressHandler(fakeEnv)
    }

    @Test
    fun `should create a working handler`() {
        assertNotNull(handler)
    }

    @Test
    fun `should publish to correct topic using injected environment`() {
        // Arrange: capture topic and message
        val captured = AtomicReference<Pair<EventType, String>?>(null)
        val env = Environment { topic, message ->
            captured.set(topic to message)
        }
        val handler = WebSocketIngressHandler(env)

        val event = APIGatewayV2WebSocketEvent().apply {
            requestContext = APIGatewayV2WebSocketEvent.RequestContext().apply {
                routeKey = "\$disconnect"
                connectionId = "abc123"
            }
        }

        // Act
        val response = handler.handle(event)

        // Assert
        val (topic, message) = captured.get()!!
        assert(response.statusCode == 200)
        assert(topic == EventType.CHARGER_DISCONNECTED)
        assert(message.contains("abc123"))
    }
}
