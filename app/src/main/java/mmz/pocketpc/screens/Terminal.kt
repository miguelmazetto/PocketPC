package mmz.pocketpc.screens

import android.view.ViewGroup
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
        val flip = Terminal.TermAct.mViewFlipper
        flip.context = it
        if(flip.rootView.parent != null)
            (flip.rootView.parent as ViewGroup).removeView(flip.rootView)
        flip.rootView
    },modifier = Modifier.fillMaxSize())
}