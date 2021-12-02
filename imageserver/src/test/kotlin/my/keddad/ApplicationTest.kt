package my.keddad

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.server.testing.*
import io.ktor.utils.io.streams.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import my.keddad.models.ImagePostResponse
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

fun bmpToWebp(a: BufferedImage): BufferedImage {
    val buf = ByteArrayOutputStream()
    ImageIO.write(a, "webp", buf)
    return ImageIO.read(ByteArrayInputStream(buf.toByteArray()))
}

fun imagesEqual(a: BufferedImage, b: BufferedImage): Boolean {
    if (a.height != b.height || a.width != b.width) return false

    for (i in 0 until a.height) {
        for (j in 0 until a.width) {
            if (a.getRGB(i, j) != b.getRGB(i, j)) return false
        }
    }

    return true
}

class ApplicationTest {
    @Test
    fun testImageUpload() {
        withTestApplication({ module() }) {
            with(handleRequest(HttpMethod.Post, "/image") {
                val boundary = "IDontKnowWhatIsThis"
                val fileBytes = File("src/test/kotlin/my/keddad/ktor_logo.png").readBytes()


                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.MultiPart.FormData.withParameter("boundary", boundary).toString()
                )
                setBody(boundary, listOf(
                    PartData.FormItem(
                        "Ktor logo", { }, headersOf(
                            HttpHeaders.ContentDisposition,
                            ContentDisposition.Inline
                                .withParameter(ContentDisposition.Parameters.Name, "description")
                                .toString()
                        )
                    ),
                    PartData.FileItem({ fileBytes.inputStream().asInput() }, {}, headersOf(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.File
                            .withParameter(ContentDisposition.Parameters.Name, "image")
                            .withParameter(ContentDisposition.Parameters.FileName, "ktor_logo.png")
                            .toString()
                    )
                    )
                )
                )
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(36, Json.decodeFromString<ImagePostResponse>(response.content!!).uuid.length)
            }
        }
    }

    @Test
    fun testNotImageUpload() {
        withTestApplication({ module() }) {
            with(handleRequest(HttpMethod.Post, "/image") {
                val boundary = "IDontKnowWhatIsThis"
                val fileBytes = File("src/test/kotlin/my/keddad/not_image").readBytes()


                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.MultiPart.FormData.withParameter("boundary", boundary).toString()
                )
                setBody(boundary, listOf(
                    PartData.FormItem(
                        "Ktor logo", { }, headersOf(
                            HttpHeaders.ContentDisposition,
                            ContentDisposition.Inline
                                .withParameter(ContentDisposition.Parameters.Name, "description")
                                .toString()
                        )
                    ),
                    PartData.FileItem({ fileBytes.inputStream().asInput() }, {}, headersOf(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.File
                            .withParameter(ContentDisposition.Parameters.Name, "image")
                            .withParameter(ContentDisposition.Parameters.FileName, "not_image")
                            .toString()
                    )
                    )
                )
                )
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testDeleteNonExistent() {
        withTestApplication({ module() }) {
            with(handleRequest(HttpMethod.Delete, "/image/nonexistentnotevenuuid")) {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun testDelete() {
        withTestApplication({ module() }) {
            with(handleRequest(HttpMethod.Post, "/image") {
                val boundary = "IDontKnowWhatIsThis"
                val fileBytes = File("src/test/kotlin/my/keddad/ktor_logo.png").readBytes()


                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.MultiPart.FormData.withParameter("boundary", boundary).toString()
                )
                setBody(boundary, listOf(
                    PartData.FormItem(
                        "Ktor logo", { }, headersOf(
                            HttpHeaders.ContentDisposition,
                            ContentDisposition.Inline
                                .withParameter(ContentDisposition.Parameters.Name, "description")
                                .toString()
                        )
                    ),
                    PartData.FileItem({ fileBytes.inputStream().asInput() }, {}, headersOf(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.File
                            .withParameter(ContentDisposition.Parameters.Name, "image")
                            .withParameter(ContentDisposition.Parameters.FileName, "ktor_logo.png")
                            .toString()
                    )
                    )
                )
                )
            }) {
                var uploadedUuid = Json.decodeFromString<ImagePostResponse>(response.content!!).uuid

                with(handleRequest(HttpMethod.Delete, "/image/${uploadedUuid}")) {
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            }
        }
    }

    @Test
    fun testGetPng() {
        withTestApplication({ module() }) {
            with(handleRequest(HttpMethod.Post, "/image") {
                val boundary = "IDontKnowWhatIsThis"
                val fileBytes = File("src/test/kotlin/my/keddad/ktor_logo.png").readBytes()


                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.MultiPart.FormData.withParameter("boundary", boundary).toString()
                )
                setBody(boundary, listOf(
                    PartData.FormItem(
                        "Ktor logo", { }, headersOf(
                            HttpHeaders.ContentDisposition,
                            ContentDisposition.Inline
                                .withParameter(ContentDisposition.Parameters.Name, "description")
                                .toString()
                        )
                    ),
                    PartData.FileItem({ fileBytes.inputStream().asInput() }, {}, headersOf(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.File
                            .withParameter(ContentDisposition.Parameters.Name, "image")
                            .withParameter(ContentDisposition.Parameters.FileName, "ktor_logo.png")
                            .toString()
                    )
                    )
                )
                )
            }) {
                var imageUuid = Json.decodeFromString<ImagePostResponse>(response.content!!).uuid

                val in_r = handleRequest(HttpMethod.Get, "/image/${imageUuid}.png") {}
                assertEquals(HttpStatusCode.OK, in_r.response.status())
                assert(
                    imagesEqual(
                        ImageIO.read(ByteArrayInputStream(File("src/test/kotlin/my/keddad/ktor_logo.png").readBytes())),
                        ImageIO.read(ByteArrayInputStream(in_r.response.byteContent))
                    )
                )

            }
        }
    }

    @Test
    fun testGetWebp() {
        withTestApplication({ module() }) {
            with(handleRequest(HttpMethod.Post, "/image") {
                val boundary = "IDontKnowWhatIsThis"
                val fileBytes = File("src/test/kotlin/my/keddad/ktor_logo.png").readBytes()


                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.MultiPart.FormData.withParameter("boundary", boundary).toString()
                )
                setBody(boundary, listOf(
                    PartData.FormItem(
                        "Ktor logo", { }, headersOf(
                            HttpHeaders.ContentDisposition,
                            ContentDisposition.Inline
                                .withParameter(ContentDisposition.Parameters.Name, "description")
                                .toString()
                        )
                    ),
                    PartData.FileItem({ fileBytes.inputStream().asInput() }, {}, headersOf(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.File
                            .withParameter(ContentDisposition.Parameters.Name, "image")
                            .withParameter(ContentDisposition.Parameters.FileName, "ktor_logo.png")
                            .toString()
                    )
                    )
                )
                )
            }) {
                var imageUuid = Json.decodeFromString<ImagePostResponse>(response.content!!).uuid

                val in_r = handleRequest(HttpMethod.Get, "/image/${imageUuid}.webp") {}
                assertEquals(HttpStatusCode.OK, in_r.response.status())
                assert(
                    imagesEqual(
                        bmpToWebp(ImageIO.read(ByteArrayInputStream(File("src/test/kotlin/my/keddad/ktor_logo.png").readBytes()))),
                        ImageIO.read(ByteArrayInputStream(in_r.response.byteContent))
                    )
                )

            }
        }
    }
}