object Libs {

    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-common:${Versions.kotlin}"
    const val kotlinStdLibJdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val coroutinesCore = coroutines("core")

    private fun coroutines(module: String, version: String = Versions.coroutines) =
        "org.jetbrains.kotlinx:kotlinx-coroutines-$module:$version"

}