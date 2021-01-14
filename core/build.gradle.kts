import plugins.BintrayPublishPlugin

plugins {
    id(Plugins.kotlinMultiplatform)
//    id(Plugins.androidLibrary)
}

kotlin {
    jvm()

//    android {
//        publishLibraryVariants("release")
//    }

    ios {
        binaries { framework() }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Libs.coroutinesCore)
            }
        }

        val jvmMain by getting
//        val androidMain by getting

        val iosMain by getting
    }
}

apply<BintrayPublishPlugin>()
