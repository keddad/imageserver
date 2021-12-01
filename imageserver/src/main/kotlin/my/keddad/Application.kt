package my.keddad

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import my.keddad.plugins.*
import my.keddad.routes.registerImageRoutes

fun Application.module() {
    configureRouting()
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    registerImageRoutes()
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") { module() }.start(wait = true)
}