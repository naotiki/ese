package me.naotiki.ese

import AppContainer
import Terminal
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import initializeComposeCommon
import kotlinx.coroutines.CoroutineScope
import me.naotiki.ese.Screen.Companion.buildRoute
import me.naotiki.ese.core.PlatformImpl
import me.naotiki.ese.core.appName
import me.naotiki.ese.core.initializePlatformImpl
import targetActivity
import java.io.File



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("Starting... ${appName}")

        targetActivity = this
        initializeComposeCommon()
        initializePlatformImpl(object :PlatformImpl{
            override fun getEseHomeDir(): File? {
                return getExternalFilesDir(null)
            }
        })
        var file: File? = null
        /*val launcher = registerForActivityResult(object : ActivityResultContracts.OpenDocumentTree() {
            override fun createIntent(context: Context, input: Uri?): Intent {
                return super.createIntent(context, input).apply {
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
            }
        }) {
            if (it != null) {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                DocumentFile.fromTreeUri(this, it)

            }
        }*/

        setContent {
            AppContainer {
                val navController = rememberNavController()
                Scaffold(topBar = {
                    TopAppBar(title = {
                        Text("Ese Android")
                    }, actions = {
                        IconButton({
                            navController.navigate(Screen.Settings.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items

                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                    
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }) {
                            Icon(Icons.Default.Settings,null)
                        }
                    })
                }) { paddingValues ->
                    val coroutineScope= rememberCoroutineScope()
                    NavHost(navController, Screen.Terminal.route, Modifier.padding(paddingValues)) {
                        buildRoute(coroutineScope)
                    }

                }
            }
        }
    }
}

enum class Screen(val route: String, val content: @Composable (NavBackStackEntry,CoroutineScope) -> Unit) {
    Terminal("terminal", {_,c->
        Terminal()
    }),
    Settings("settings", {a,b->
        Text("設定")
    });

    companion object{
        fun NavGraphBuilder.buildRoute(coroutineScope: CoroutineScope) {
            values().forEach {
                composable(it.route,content={nav->
                    it.content(nav,coroutineScope)

                })
            }
        }
    }
}