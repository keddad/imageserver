package my.keddad.plugins

import io.ktor.application.*
import java.io.File

fun Application.createStorageFolder() {
    File("data").mkdir()
}