package utils

import Sample
import com.android.build.gradle.*
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer
import org.gradle.kotlin.dsl.getByType

fun PluginContainer.configure(project: Project) {
    whenPluginAdded {
        when (this) {
            is AppPlugin ->
                project.extensions
                    .getByType<AppExtension>()
                    .configureAppExtension()
            is LibraryPlugin ->
                project.extensions
                    .getByType<LibraryExtension>()
                    .configureLibraryExtension()
        }
    }
}

private fun AppExtension.configureAppExtension() {
    compileSdkVersion(Sample.compileSdk)
    buildToolsVersion(Sample.buildTools)

    defaultConfig {
        minSdkVersion(Sample.minSdk)
        targetSdkVersion(Sample.targetSdk)

        applicationId = Sample.applicationId

        versionCode = Sample.versionCode
        versionName = Sample.versionName
    }

    configureJavaVersion()
}

private fun LibraryExtension.configureLibraryExtension() {
    compileSdkVersion(Sample.compileSdk)
    buildToolsVersion(Sample.buildTools)

    defaultConfig {
        minSdkVersion(Sample.minSdk)
        targetSdkVersion(Sample.targetSdk)
    }

    configureJavaVersion()
}

private fun TestedExtension.configureJavaVersion() {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}