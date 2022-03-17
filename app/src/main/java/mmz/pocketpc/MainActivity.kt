package mmz.pocketpc

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.view.WindowManager
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
import androidx.core.os.persistableBundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import mmz.pocketpc.ui.theme.PocketPCTheme
import kotlinx.coroutines.launch
import mmz.pocketpc.screens.Terminal
import mmz.pocketpc.screens.updateDistroVersions
import mmz.pocketpc.util.GlobalConfig
import mmz.pocketpc.util.*
import java.io.File

var CurrentScreen = DrawerScreens[0]

class MultiThread: ViewModel(){
    fun launch(func: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO){func()}
    }
}

object AppContext {
    @Volatile private lateinit var appContext: Context
    @Volatile private lateinit var windowManager: WindowManager
    private val multiThread = MultiThread()

    var savedInstanceState: Bundle? = null

    val taskBar = TaskBar()
    val arch = System.getProperty("os.arch")
    //val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)

    fun setContext(context: Context) { appContext = context }
    fun setWM(wm: WindowManager) { windowManager = wm }
    fun getContext(): Context { return appContext }

    fun getDataDir(): File? { return appContext.dataDir }
    fun getCacheDir(): File? { return appContext.cacheDir }
    fun getPrefs(): SharedPreferences { return PreferenceManager.getDefaultSharedPreferences(appContext) }
    fun getRes(): Resources { return appContext.resources }
    fun getPM(): PackageManager { return appContext.packageManager }
    fun getDMetrics(): DisplayMetrics {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        return dm
    }
    fun mtLaunch(func: () -> Unit) { multiThread.launch(func) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContext.savedInstanceState = savedInstanceState
        Terminal.TermAct.onCreate(this)
        AppContext.setContext(this)
        AppContext.setWM(this.windowManager)

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
        Chroot.Shell()
    }

    override fun onStart() {
        super.onStart()
        Terminal.TermAct.onStart()
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