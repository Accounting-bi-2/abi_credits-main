package bi.accounting.controller

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.hateoas.JsonError

@Controller
class GlobalExceptionHandler {

    @Error(global = true)
    fun handleIllegalArgumentException(request: HttpRequest<*>, e: IllegalArgumentException): HttpResponse<JsonError> {
        return HttpResponse.badRequest(JsonError(e.message)).status(HttpStatus.BAD_REQUEST)
    }

    @Error(global = true)
    fun handleNullPointerException(request: HttpRequest<*>, e: NullPointerException): HttpResponse<JsonError> {
        return HttpResponse.status<String>(HttpStatus.INTERNAL_SERVER_ERROR).body(JsonError(e.message))
    }

    @Error(global = true)
    fun handleRuntimeException(request: HttpRequest<*>, e: RuntimeException): HttpResponse<JsonError> {
        return HttpResponse.status<String>(HttpStatus.INTERNAL_SERVER_ERROR).body(JsonError(e.message))
    }
}