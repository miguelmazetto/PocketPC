package mmz.pocketpc

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.offsec.nhterm.util.TermSettings
import kotlinx.coroutines.Dispatchers
import mmz.pocketpc.ui.theme.PocketPCTheme
import mmz.pocketpc.util.TaskBar
import kotlinx.coroutines.launch
import mmz.pocketpc.screens.updateDistroVersions
import mmz.pocketpc.screens.updateInstalledDistro
import mmz.pocketpc.util.FileUtils
import mmz.pocketpc.util.GlobalConfig
import mmz.pocketpc.util.getProot
import java.io.File

var CurrentScreen = DrawerScreens[0]

class MultiThread: ViewModel(){
    fun launch(func: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO){func()}
    }
}

object AppContext {
    @Volatile
    private lateinit var appContext: Context
    private val multiThread = MultiThread()

    val taskBar = TaskBar()
    val arch = System.getProperty("os.arch")
    //val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)

    fun setContext(context: Context) { appContext = context }
    fun getDataDir(): File? { return appContext.dataDir }
    fun getCacheDir(): File? { return appContext.cacheDir }
    fun getPrefs(): SharedPreferences { return PreferenceManager.getDefaultSharedPreferences(appContext) }
    fun getRes(): Resources { return appContext.resources }
    fun mtLaunch(func: () -> Unit) { multiThread.launch(func) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContext.setContext(this)
        GlobalConfig.load()
        if(GlobalConfig.installedDistro != "") GlobalConfig.installStatus.value = 2
        updateDistroVersions()
        setContent {
            PocketPCTheme {
                AppMainScreen()
            }
        }
        if(this.cacheDir != null){
            for (f in this.cacheDir.listFiles()!!) { f.delete() }
        }
        getProot()
    }
}

@Composable
fun AppMainScreen() {
    val navController = rememberNavController()
    Surface(color = MaterialTheme.colors.background) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val openDrawer = {
            scope.launch {
                drawerState.open()
            }
        }
        ModalDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                Drawer(
                    onDestinationClicked = { screen ->
                        scope.launch {
                            drawerState.close()
                        }
                        CurrentScreen = screen
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopBar(
                    title = CurrentScreen.title,
                    buttonIcon = Icons.Filled.Menu,
                    onButtonClicked = { openDrawer() }
                )
                AppContext.taskBar.TaskBar()
                NavHost(
                    navController = navController,
                    startDestination = DrawerScreens[0].route
                ) {
                    DrawerScreens.forEach { screen ->
                        composable(screen.route, emptyList(), emptyList(), screen.comp)
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(title: String = "", buttonIcon: ImageVector, onButtonClicked: () -> Unit) {
    TopAppBar(
        title = {Text(text = title)},
        navigationIcon = {
            IconButton(onClick = { onButtonClicked() } ) {
                Icon(buttonIcon, contentDescription = "")
            }
        },
        backgroundColor = MaterialTheme.colors.primaryVariant
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PocketPCTheme {
        AppMainScreen()
    }
}