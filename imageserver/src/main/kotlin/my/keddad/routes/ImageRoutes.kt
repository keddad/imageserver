package my.keddad.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.utils.io.core.*
import io.netty.buffer.ByteBuf
import my.keddad.models.ImagePostResponse
import java.nio.ByteBuffer
import java.util.*
import java.io.File
import javax.imageio.ImageIO
import kotlin.io.path.writeBytes

fun Route.imageRouting() {
    route("/image") {
        post { // TODO Handle blocking method calls
            val multipartData = call.receiveMultipart()
            val uuid = UUID.randomUUID().toString()
            val targetFile = File("data/${uuid}.png")

            val tmpFile = kotlin.io.path.createTempFile()

            multipartData.forEachPart {
                when (it) {
                    is PartData.FileItem -> {
                        tmpFile.writeBytes(it.streamProvider().readBytes())
                    }
                    else -> null
                }
            }

            val image = ImageIO.read(tmpFile.toFile())

            if (image == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            ImageIO.write(image, "png", targetFile)

            call.respond(ImagePostResponse(uuid))
        }
    }
}

fun Application.registerImageRoutes() {
    routing {
        imageRouting()
    }
}