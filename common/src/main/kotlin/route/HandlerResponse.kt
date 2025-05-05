package org.paragontech.route
import org.springframework.http.HttpStatus

sealed class HandlerResponse {
    abstract val statusCode: Int
    abstract val body: String
    val headers: Map<String, String> = mapOf("Content-Type" to "application/json")

    fun toApiGatewayResponse(): Map<String, Any> =  mapOf(
        "statusCode" to statusCode,
        "body" to body,
        "headers" to headers)

    data class Success(
        override val statusCode: Int = HttpStatus.OK.value(),
        override val body: String = """{"status":"ok"}""",
    ) : HandlerResponse()

    data class Error(
        val errorType: HttpStatus,
        val errorMessage: String,
    ) : HandlerResponse(){
        override val body: String = errorMessage
        override val statusCode: Int = errorType.value()
    }

    companion object {

        fun badRequest(message: String): HandlerResponse = Error(errorType = HttpStatus.BAD_REQUEST, message)

        fun unauthorized(message: String = "Unauthorized"): HandlerResponse = Error(HttpStatus.UNAUTHORIZED, message)

        fun forbidden(message: String = "Forbidden"): HandlerResponse = Error(HttpStatus.FORBIDDEN, message)

        fun notFound(message: String = "Not Found"): HandlerResponse = Error(HttpStatus.NOT_FOUND, message)

        fun tooManyRequests(message: String = "Rate limit exceeded"): HandlerResponse =
            Error(HttpStatus.TOO_MANY_REQUESTS, message)

        fun internalServerError(message: String = "Internal server error"): HandlerResponse =
            Error(HttpStatus.INTERNAL_SERVER_ERROR, message)
    }


}