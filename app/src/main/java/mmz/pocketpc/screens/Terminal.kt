package mmz.pocketpc.screens

import android.os.Parcel
import android.os.Parcelable
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.termux.shared.logger.Logger
import com.termux.shared.terminal.TermuxTerminalSessionClientBase
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import mmz.pocketpc.AppContext
import mmz.pocketpc.util.GlobalConfig
import java.io.File

private var initiated = false
var TSession : TerminalSession? = null
var TClient : TerminalSessionClient? = null
var TVClient : TermViewClient? = null
var TView : TerminalView? = null

class TerminalSessionClient() : TermuxTerminalSessionClientBase(), Parcelable {

    constructor(parcel: Parcel) : this() {

    }

    fun onStart(){
        TView!!.onScreenUpdated()
    }

    override fun onTextChanged(changedSession: TerminalSession?) {
        println("onTextChanged: ${changedSession!!.emulator.screen.transcriptText}")
        TView!!.onScreenUpdated()
    }

    override fun onTitleChanged(updatedSession: TerminalSession?) {}

    override fun onSessionFinished(finishedSession: TerminalSession?) {}

    override fun onCopyTextToClipboard(session: TerminalSession?, text: String?) {}

    override fun onPasteTextFromClipboard(session: TerminalSession?) {}

    override fun onBell(session: TerminalSession?) {}

    override fun onColorsChanged(changedSession: TerminalSession?) {}

    override fun onTerminalCursorStateChange(state: Boolean) {}


    override fun getTerminalCursorStyle(): Int? {
        return null
    }


    override fun logError(tag: String?, message: String?) {
        Logger.logError(tag, message)
    }

    override fun logWarn(tag: String?, message: String?) {
        Logger.logWarn(tag, message)
    }

    override fun logInfo(tag: String?, message: String?) {
        Logger.logInfo(tag, message)
    }

    override fun logDebug(tag: String?, message: String?) {
        Logger.logDebug(tag, message)
    }

    override fun logVerbose(tag: String?, message: String?) {
        Logger.logVerbose(tag, message)
    }

    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {
        Logger.logStackTraceWithMessage(tag, message, e)
    }

    override fun logStackTrace(tag: String?, e: Exception?) {
        Logger.logStackTrace(tag, e)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TerminalSessionClient> {
        override fun createFromParcel(parcel: Parcel): TerminalSessionClient {
            return TerminalSessionClient(parcel)
        }

        override fun newArray(size: Int): Array<TerminalSessionClient?> {
            return arrayOfNulls(size)
        }
    }

}

class TermViewClient() : TerminalViewClient {

    override fun onScale(scale: Float): Float {
        return scale
    }

    override fun onSingleTapUp(e: MotionEvent?) {

    }

    override fun shouldBackButtonBeMappedToEscape(): Boolean {
        return true
    }

    override fun shouldEnforceCharBasedInput(): Boolean {
        return false
    }

    override fun shouldUseCtrlSpaceWorkaround(): Boolean {
        return true
    }

    override fun isTerminalViewSelected(): Boolean {
        return true
    }

    override fun copyModeChanged(copyMode: Boolean) {

    }

    override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean {
        return false
    }

    override fun onKeyUp(keyCode: Int, e: KeyEvent?): Boolean {
        return false
    }

    override fun onLongPress(event: MotionEvent?): Boolean {
        return false
    }

    override fun readControlKey(): Boolean {
        return false
    }

    override fun readAltKey(): Boolean {
        return false
    }

    override fun readShiftKey(): Boolean {
        return false
    }

    override fun readFnKey(): Boolean {
        return false
    }

    override fun onCodePoint(
        codePoint: Int,
        ctrlDown: Boolean,
        session: TerminalSession?
    ): Boolean {
        return false
    }

    override fun onEmulatorSet() {
        println("onEmulatorSet")
    }

    override fun logError(tag: String?, message: String?) {
        Logger.logError(tag, message)
    }

    override fun logWarn(tag: String?, message: String?) {
        Logger.logWarn(tag, message)
    }

    override fun logInfo(tag: String?, message: String?) {
        Logger.logInfo(tag, message)
    }

    override fun logDebug(tag: String?, message: String?) {
        Logger.logDebug(tag, message)
    }

    override fun logVerbose(tag: String?, message: String?) {
        Logger.logVerbose(tag, message)
    }

    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {
        Logger.logStackTraceWithMessage(tag, message, e)
    }

    override fun logStackTrace(tag: String?, e: Exception?) {
        Logger.logStackTrace(tag, e)
    }

}

private fun initiateSession(){
    if(initiated) return
    initiated = true
    TClient = TerminalSessionClient(Parcel.obtain())
    TVClient = TermViewClient()
    val datadir = AppContext.getDataDir()
    /*argsList.addAll(("-0 -l --kill-on-exit -r ${
        File(datadir, "rootfs").path
    } -b /dev -b /proc -b /sys -b ${
        File(datadir, "rootfs/tmp").path}:/dev/shm -w /root /usr/bin/env -i").split(" "))
    argsList.addAll(arrayOf(
        "HOME=/root",
        "PATH=/usr/local/sbin:/usr/local/bin:/bin:/usr/bin:/sbin:/usr/sbin:/usr/games:/usr/local/games",
        "TERM=vt100",
        "LANG=C.UTF-8",
        "/bin/bash","--login"
        ))*/
    val shellcmd = arrayOf(
            "-0","-l","--kill-on-exit","-r",File(datadir, "rootfs").path,
            "-b","/dev","-b","/proc","-b","/sys",
            "-b","${File(datadir, "rootfs/tmp").path}:/dev/shm","-w","/root",
            "/usr/bin/env","-i", "HOME=/root", "TERM=vt100", "LANG=C.UTF-8",
            "PATH=/usr/local/sbin:/usr/local/bin:/bin:/usr/bin:/sbin:/usr/sbin:/usr/games:/usr/local/games",
            "/bin/bash", "--login")
    val env = arrayOf(
        "PWD=/",
        "PROOT_TMP_DIR=${File(datadir, "files/proot_tmp").path}"
    )
    TSession = TerminalSession(
        File(datadir, "files/${GlobalConfig.S.prootName}").path,
        "/",
        shellcmd,
        env,
        10,
        TClient)
}

@Composable
fun TerminalScreen() {
    AndroidView(
        factory = {
            TView = TerminalView(it,null)
            initiateSession()
            TView!!.setTextSize(10)
            TView!!.attachSession(TSession)
            TView!!.setTerminalViewClient(TVClient)
            TView!!
        },
        modifier = Modifier.fillMaxSize()
    )
}