package utils

import org.gradle.api.Project
import java.io.FileInputStream
import java.util.*

inline fun Project.readProperties(
    propertiesFileName: String,
    crossinline block: Properties.() -> Unit
) {
    val file = rootProject.file("$propertiesFileName.properties")
    if (file.exists()) {
        val properties = Properties().apply {
            load(FileInputStream(file))
        }

        block(properties)
    }
}