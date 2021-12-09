package my.keddad.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import my.keddad.models.ImagePostResponse
import java.io.File
import java.util.*
import javax.imageio.*
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.IIOImage
import javax.imageio.ImageWriter
import javax.imageio.plugins.jpeg.JPEGImageWriteParam
import javax.imageio.stream.FileImageOutputStream
import kotlin.io.path.writeBytes


fun Route.imageRouting() {
    post("/image") {
        // TODO Handle blocking method calls
        val multipartData = call.receiveMultipart()
        val uuid = UUID.randomUUID().toString()
        val targetFile = File("data/${uuid}/${uuid}.png")

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

        File("data/${uuid}/").mkdirs()

        ImageIO.write(image, "png", targetFile)

        call.respond(ImagePostResponse(uuid))

    }

    delete("/image/{id}") {
        val uuid = call.parameters["id"]

        if (uuid == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@delete
        }

        val targetFile = File("data/${uuid}/")

        if (!targetFile.exists()) {
            call.respond(HttpStatusCode.NotFound)
            return@delete
        }

        targetFile.delete()
        call.respond(HttpStatusCode.OK)
    }

    get("/image/{id...}") {
        val params = call.parameters.getAll("id")?.first()?.split(".")

        if (params?.size != 2) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val uuid = params.first()
        val targetFormat = params.last()

        val origFile = File("data/${uuid}/${uuid}.png")
        val resultFile = File("data/${uuid}/${uuid}.${targetFormat}")

        if (!origFile.exists()) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        if (!resultFile.exists()) {
            ImageIO.write(ImageIO.read(origFile), targetFormat, resultFile)
        }

        call.respondFile(resultFile)
    }

    get("/image/compressed/{id...}") {
        val params = call.parameters.getAll("id")?.first()?.split(".")

        if (params?.size != 2) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val uuid = params.first()
        val targetFormat = params.last()

        if (targetFormat != "jpg" && targetFormat != "jpeg") { // TODO Compression of other formats
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val origFile = File("data/${uuid}/${uuid}.png")
        val resultFile = File("data/${uuid}/compressed_${uuid}.${targetFormat}")

        if (!origFile.exists()) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        if (!resultFile.exists()) {
            val jpegParams = JPEGImageWriteParam(null)
            jpegParams.compressionMode = ImageWriteParam.MODE_EXPLICIT
            jpegParams.compressionQuality = 0.6f

            val writer = ImageIO.getImageWritersByFormatName(targetFormat).next()
            writer.output = FileImageOutputStream(resultFile)
            writer.write(null, IIOImage(ImageIO.read(origFile), null, null), jpegParams)
        }

        call.respondFile(resultFile)
    }
}

fun Application.registerImageRoutes() {
    routing {
        imageRouting()
    }
}