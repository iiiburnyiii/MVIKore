import plugins.BintrayPublishPlugin

plugins {
    `kotlin-multiplatform`
    `android-library`
}

android {
    compileSdkVersion(30)
    defaultConfig {
        minSdkVersion(15)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

kotlin {
    jvm()

    android {
        publishLibraryVariants("release")
    }

    iosArm32 {
        binaries {
            framework()
        }
    }
    iosArm64 {
        binaries {
            framework()
        }
    }
    iosX64 {
        binaries {
            framework()
        }
    }


    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(Libs.kotlinStdLib)
                implementation(Libs.coroutinesCore)
            }
        }

        getByName("jvmMain") {
            dependencies {
                implementation(Libs.kotlinStdLibJdk8)
            }
        }

        getByName("androidMain") {
            dependencies {
                implementation(Libs.kotlinStdLibJdk8)
            }
        }

        create("nativeMain") {
            dependsOn(getByName("commonMain"))
        }

        getByName("iosArm64Main") {
            dependsOn(getByName("nativeMain"))
        }

        getByName("iosArm32Main") {
            dependsOn(getByName("nativeMain"))
        }

        getByName("iosX64Main") {
            dependsOn(getByName("nativeMain"))
        }
    }
}

apply<BintrayPublishPlugin>()
