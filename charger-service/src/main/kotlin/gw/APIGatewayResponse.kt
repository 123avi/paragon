package org.paragontech.gw

data class APIGatewayResponse(
    val statusCode: Int,
    val body: String = "",
    val headers: Map<String, String> = mapOf("Content-Type" to "application/json")
) {
    companion object {
        fun ok(body: String = """{"status":"ok"}""") = APIGatewayResponse(200, body)
        fun error(message: String) = APIGatewayResponse(500, """{"error":"$message"}""")
    }
}
