package mmz.pocketpc.screens

import android.content.Context
import android.content.res.Resources
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.offsec.nhterm.ShellTermSession
import com.offsec.nhterm.emulatorview.EmulatorView
import com.offsec.nhterm.emulatorview.TermSession
import com.offsec.nhterm.util.TermSettings
import mmz.pocketpc.AppContext

/*val TermTextColor = mutableStateOf(Color.White)
val TermBackgroundColor = mutableStateOf(Color.Black)*/

open class Shell {
    var LineCount = 2000
    val buffer = mutableStateListOf<String>()
    private var initiated = false
    var session : ShellTermSession? = null
    private fun start() {
        session = ShellTermSession(TermSettings(AppContext.getRes() ,AppContext.getPrefs()),
            "/system/bin/sh -","")
    }
    fun init() {
        if (!initiated) {
            initiated = true
            start()
        }
    }

    fun addText(str: String) {
        val lns = str.split("\n")
        val dif = buffer.size + lns.size - LineCount
        if (dif > 0) buffer.removeAll(buffer.subList(0, dif))
        buffer.addAll(lns)
    }
}

class Terminal : Shell() {

    private var view : EmulatorView? = null

    fun getView(context: Context): EmulatorView {
        if(view == null) {
            init()
            return EmulatorView(
                context,
                session as TermSession,
                context.resources.displayMetrics
            ).also { view = it }
        }else
            return view!!
    }

    init{
        /*val buf = remember { buffer }
        val tcolor = remember { TermTextColor }
        val bcolor = remember { TermBackgroundColor }*/
    }

    @Composable
    fun Screen(){
        AndroidView(modifier = Modifier.fillMaxSize(),
        factory = { context ->
            getView(context)
        })
        /*LazyColumn(
            modifier = Modifier
                .background(bcolor.value)
                .fillMaxSize()
        ) {
            buf.forEach { text ->
                this.item {
                    Text(
                        text = text,
                        color = tcolor.value,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }*/
    }
}

val MainTerminals = listOf(Terminal())

@Composable
fun TerminalScreen() {
    MainTerminals[0].Screen()
}