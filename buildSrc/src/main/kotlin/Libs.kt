object Libs {

    val coroutinesCore = coroutines("core")

    private fun coroutines(module: String, version: String = Versions.coroutines) =
        "org.jetbrains.kotlinx:kotlinx-coroutines-$module:$version"

}
