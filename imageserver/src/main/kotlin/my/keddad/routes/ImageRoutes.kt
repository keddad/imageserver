package my.keddad.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.utils.io.core.*
import io.netty.buffer.ByteBuf
import my.keddad.models.ImagePostResponse
import java.nio.ByteBuffer
import java.util.*

fun Route.imageRouting() {
    route("/image") {
        post {
            val multipartData = call.receiveMultipart();
            val dataBuffer = ByteBuffer.allocate(1024 * 32) // Max upload size is 32 MB
            print("Meow!")

            multipartData.forEachPart {
                when (it) {
                    is PartData.FileItem -> {
                        val dataBytes = it.streamProvider().readBytes()
                        dataBuffer.put(dataBytes)
                    }
                }
            }

            // TODO Actually write the data somewhere

            call.respond(ImagePostResponse(UUID.randomUUID().toString()))
        }
    }
}

fun Application.registerImageRoutes() {
    routing {
        imageRouting()
    }
}