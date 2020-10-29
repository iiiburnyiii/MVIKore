object Libs {

    val coroutinesCore = coroutines("core")
    val coroutinesAndroid = coroutines("android")

    val viewModel = lifecycle("viewmodel-ktx")

    private fun coroutines(module: String, version: String = Versions.coroutines) =
        "org.jetbrains.kotlinx:kotlinx-coroutines-$module:$version"

    private fun lifecycle(module: String, version: String = Versions.lifecycle) =
        "androidx.lifecycle:lifecycle-$module:$version"

}