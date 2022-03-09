package mmz.pocketpc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import mmz.pocketpc.ui.theme.PocketPCTheme
import mmz.pocketpc.screens.*

typealias ComposableFun = @Composable (NavBackStackEntry) -> Unit
data class DrawerScreen(val title: String, val route: String, val comp: ComposableFun)

val DrawerScreens = listOf(
    DrawerScreen("Home", "home") { Home() },
    DrawerScreen("Distro Setup", "distrosetup") { DistroSetup() },
    DrawerScreen("Terminal", "termscreen") { TerminalScreen() },
    DrawerScreen("Packages", "packages") { Packages() }
)

@Composable
fun Drawer(onDestinationClicked: (screen: DrawerScreen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
            .selectableGroup()
    ) {
        DrawerScreens.forEach { screen ->
            Text(
                text = screen.title,
                fontSize = 24.sp,
                modifier = Modifier
                    .selectable(
                        selected = CurrentScreen == screen,
                        onClick = { onDestinationClicked(screen) }
                    )
                    .fillMaxWidth()
                    .padding(all = 10.dp)
            )
        }
    }
}

@Preview
@Composable
fun DrawerPreview() {
    PocketPCTheme {
        Drawer{}
    }
}