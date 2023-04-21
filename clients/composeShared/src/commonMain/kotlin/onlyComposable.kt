import androidx.compose.runtime.Composable

@Composable
fun onlyComposable(platform: PlatformBackend, content:@Composable ()->Unit){
    if (platform ==platform) {
        content()
    }
}
@Composable
fun onlyComposable(platform: PlatformBackend, wrapper:@Composable (@Composable ()->Unit )->Unit, content:@Composable ()->Unit){
    if (platform ==platform) {
        wrapper(content)
    }
}