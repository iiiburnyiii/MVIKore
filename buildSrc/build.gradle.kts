plugins {
    `kotlin-dsl`

}

repositories {
    mavenCentral()
    jcenter()
    google()
}

dependencies {
    implementation("com.android.tools.build:gradle:7.0.0-alpha04")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
}
