package my.keddad.plugins

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.features.*
import io.ktor.application.*
import io.ktor.response.*


fun Application.configureRouting() {


    routing {
        install(StatusPages) {
            exception<AuthenticationException> { cause ->
                call.respond(HttpStatusCode.Unauthorized)
            }
            exception<AuthorizationException> { cause ->
                call.respond(HttpStatusCode.Forbidden)
            }

        }

    }
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()
