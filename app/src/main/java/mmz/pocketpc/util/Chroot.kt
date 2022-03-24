package mmz.pocketpc.util

import mmz.pocketpc.AppContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object Chroot {
    private var fakedid = ""
    fun getFaked() : String{
        if(fakedid == ""){
            val cmd = arrayListOf(File(AppContext.libdir,"libfaked.so").path)
            val savefile = File(AppContext.getDataDir(),"faked.db")

            if(savefile.exists()) cmd.addAll(arrayOf("--load",savefile.path))
            cmd.addAll(arrayOf("--save-file",savefile.path))

            val p = Runtime.getRuntime().exec(cmd.toTypedArray())
            val stdInput = BufferedReader(InputStreamReader(p.inputStream))
            var s : String? = null
            while (stdInput.readLine().also{s = it} != null) {
                return s!!.also{fakedid = it}
            }
        }
        return fakedid
    }
    fun getFakerootCmd() : Array<String>{
        val out = ArrayList<String>() //File(AppContext.libdir,"libfakeroot.so").path
        val cmd = arrayListOf(
            "FAKEROOTKEY=${getFaked()}",
            "LD_PRELOAD=${File(AppContext.libdir,"libfakeroot.so").path}")
        return cmd.toTypedArray()
    }
    fun getFakerootCmd(cmdaft : Array<String> = arrayOf<String>()) : String{
        val cmd = arrayListOf<String>()
        cmd.addAll(getFakerootCmd())
        cmd.addAll(cmdaft)
        return cmd.joinToString(" ")
    }
    class Shell {


        init {
            //System.loadLibrary("fakeroot")
            //println("Fakeroot loaded!")
            //System.loadLibrary("fakechroot")
            //println("FakeChroot loaded!")
            //fakechroot_init()
            //println("FakeChroot initiated!")
        }
    }
}