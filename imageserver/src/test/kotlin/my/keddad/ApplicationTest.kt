package my.keddad

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.testing.*
import io.ktor.utils.io.streams.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import my.keddad.models.ImagePostResponse
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testPost() {
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
}