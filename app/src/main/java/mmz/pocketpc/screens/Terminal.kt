package mmz.pocketpc.screens

import android.content.Context
import android.content.res.Resources
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.offsec.nhterm.ShellTermSession
import com.offsec.nhterm.emulatorview.EmulatorView
import com.offsec.nhterm.emulatorview.TermSession
import com.offsec.nhterm.util.TermSettings
import mmz.pocketpc.AppContext
import mmz.pocketpc.util.GlobalConfig
import java.io.File

/*val TermTextColor = mutableStateOf(Color.White)
val TermBackgroundColor = mutableStateOf(Color.Black)*/

open class Shell {
    var LineCount = 2000
    val buffer = mutableStateListOf<String>()
    private var initiated = false
    var session : ShellTermSession? = null
    private fun start(initialCommand: String = "") {
        val datadir = AppContext.getDataDir()
        val shellcmd = File(datadir, "files/${GlobalConfig.S.prootName}").path +
                " -0 -l --kill-on-exit -r " +
                File(datadir, "rootfs").path +
                " -b /dev -b /proc -b /sys" +
                " -b ${File(datadir, "rootfs/tmp").path}:/dev/shm" +
                " -w /root" +
                //" /usr/bin/env -i" +
                //" HOME=/root" +
                //" PATH=/usr/local/sbin:/usr/local/bin:/bin:/usr/bin:/sbin:/usr/sbin:/usr/games:/usr/local/games" +
                //" TERM=vt100" +
                //" LANG=C.UTF-8" +
                " /bin/bash --login"
        val env = arrayOf(
            "PWD=$datadir",
            "PROOT_TMP_DIR=${File(datadir, "files/proot_tmp").path}"
        )
        session = ShellTermSession(TermSettings(AppContext.getRes() ,AppContext.getPrefs()),
            initialCommand, shellcmd, env)
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

    //private var view : EmulatorView? = null

    /*fun getView(context: Context): EmulatorView {
        if(view == null) {
            init()
            return EmulatorView(
                context,
                session as TermSession,
                context.resources.displayMetrics
            ).also { view = it }
        }else {
            (view!!.parent as ViewGroup).removeView(view)
            return view!!
        }
    }*/

    fun getView(context: Context): EmulatorView {
        init()
        return EmulatorView(
            context,
            session as TermSession,
            context.resources.displayMetrics
        )
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
    if(GlobalConfig.installStatus.value == 2)
        MainTerminals[0].Screen()
    else
        Text("Distro not installed")
}