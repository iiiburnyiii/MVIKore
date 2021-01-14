import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import utils.configure

buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.gradle}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

subprojects {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    plugins.configure(this)
}

tasks.register("clean", Delete::class) {
    group = "build"
    delete(rootProject.buildDir)
}
