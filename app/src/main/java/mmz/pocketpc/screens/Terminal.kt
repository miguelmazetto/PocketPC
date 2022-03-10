package mmz.pocketpc.screens

import android.content.*
import android.net.Uri
import android.os.*
import android.view.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.startActivity
import com.offsec.nhterm.*
import com.offsec.nhterm.compat.AndroidCompat
import com.offsec.nhterm.emulatorview.EmulatorView
import com.offsec.nhterm.emulatorview.TermSession
import com.offsec.nhterm.util.TermSettings
import mmz.pocketpc.AppContext
import mmz.pocketpc.util.GlobalConfig
import java.io.File
import java.util.*

/*val TermTextColor = mutableStateOf(Color.White)
val TermBackgroundColor = mutableStateOf(Color.Black)*/

private fun execURL(link: String, context: Context) {
    val webLink = Uri.parse(link)
    val openLink = Intent(Intent.ACTION_VIEW, webLink)
    val handlers = AppContext.getPM().queryIntentActivities(openLink, 0)
    if (handlers.size > 0) startActivity(context, openLink, null)
}

class Shell {
    private var initiated = false
    lateinit var session : ShellTermSession
    private val termsett = TermSettings(AppContext.getRes() ,AppContext.getPrefs())

    lateinit var emulatorView : EmulatorView

    /*private fun doUIToggle(x: Int, y: Int, width: Int, height: Int) {
        /*when (mActionBarMode) {
            TermSettings.ACTION_BAR_MODE_NONE -> if (AndroidCompat.SDK >= 11 && (mHaveFullHwKeyboard || y < height / 2)) {
                openOptionsMenu()
                return
            } else {
                doToggleSoftKeyboard()
            }
            TermSettings.ACTION_BAR_MODE_ALWAYS_VISIBLE -> if (!mHaveFullHwKeyboard) {
                doToggleSoftKeyboard()
            }
            TermSettings.ACTION_BAR_MODE_HIDES -> if (mHaveFullHwKeyboard || y < height / 2) {
                doToggleActionBar()
                return
            } else {
                doToggleSoftKeyboard()
            }
        }*/
        emulatorView!!/*getCurrentEmulatorView()*/.requestFocus()
    }*/

    private class EmulatorViewGestureListener(private val view: EmulatorView) :
        GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // Let the EmulatorView handle taps if mouse tracking is active
            if (view.isMouseTrackingActive) return false

            //Check for link at tap location
            val link = view.getURLat(e.x, e.y)
            link?.let { execURL(it, view.context) }
                ?: view.requestFocus()
                /*?: doUIToggle(
                    e.x.toInt(),
                    e.y.toInt(), view.visibleWidth, view.visibleHeight
                )*/
            return true
        }

        /*override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val absVelocityX = Math.abs(velocityX)
            val absVelocityY = Math.abs(velocityY)
            return if (absVelocityX > Math.max(1000.0, 2.0 * absVelocityY)) {
                // Assume user wanted side to side movement
                if (velocityX > 0) {
                    // Left to right swipe -- previous window
                    mViewFlipper.showPrevious()
                } else {
                    // Right to left swipe -- next window
                    mViewFlipper.showNext()
                }
                true
            } else {
                false
            }
        }*/
    }

    private fun start(initialCommand: String = "") {
        val datadir = AppContext.getDataDir()!!
        val shellcmd = "/system/bin/sh"
        val prootcmd = File(datadir, "files/${GlobalConfig.S.prootName}").path +
                " -0 -l --kill-on-exit -r " +
                File(datadir, "rootfs").path +
                " -b /dev -b /proc -b /sys" +
                //" -b ${File(datadir, "rootfs/tmp").path}:/dev/shm" +
                " -w /root" +
                /*" /usr/bin/env -i" +
                " HOME=/root" +
                " PATH=/usr/local/sbin:/usr/local/bin:/bin:/usr/bin:/sbin:/usr/sbin:/usr/games:/usr/local/games" +
                " TERM=vt100" +
                " LANG=C.UTF-8" +*/
                " /bin/bash --login"
        //val shellcmd = "/system/bin/sh ls"
        val env = arrayOf(
            /*"PWD=${datadir.path}",
            "TERM=vt100",
            "SHELL=/system/bin/sh",
            "LOGNAME=u0_a181",
            "USER=u0_a181",
            "TMPDIR=/data/local/tmp",*/
            "PROOT_TMP_DIR=${File(datadir, "files/proot_tmp").path}"
        )
        session = ShellTermSession(termsett, prootcmd, shellcmd, env)
        //session!!.write("\ncd ${datadir.path}\nls\nenv\n$prootcmd\n")
    }
    fun init() {
        if (!initiated) {
            initiated = true
            start()
        }else println("Txt: ${session.transcriptText}")
    }

    fun getView(context: Context): EmulatorView {
        init()
        emulatorView = EmulatorView(
            context,
            session as TermSession,
            context.resources.displayMetrics
        )
        emulatorView.setExtGestureListener(EmulatorViewGestureListener(emulatorView))
        return emulatorView
    }

    @Composable
    fun Screen(){
        AndroidView(modifier = Modifier.fillMaxSize(),
            factory = { context ->
                getView(context)
            })
    }
}

val MainTerminals = listOf(Shell())

@Composable
fun TerminalScreen() {
    if(GlobalConfig.installStatus.value == 2)
        MainTerminals[0].Screen()
    else
        Text("Distro not installed")
}