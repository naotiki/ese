import androidx.compose.runtime.Composable

@Composable
fun onlyComposable(platform: ClientPlatform, content:@Composable ()->Unit){
    if (clientPlatform==platform) {
        content()
    }
}
@Composable
fun onlyComposable(platform: ClientPlatform, wrapper:@Composable (@Composable ()->Unit )->Unit, content:@Composable ()->Unit){
    if (clientPlatform==platform) {
        wrapper(content)
    }
}