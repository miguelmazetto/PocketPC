package mmz.pocketpc.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

import jackpal.androidterm.Term

object Terminal{
    val TermAct = Term()
}

@Composable
fun TerminalScreen() {
    AndroidView(factory = {
        Terminal.TermAct.mViewFlipper.context = it
        Terminal.TermAct.mViewFlipper.rootView
    },modifier = Modifier.fillMaxSize())
}