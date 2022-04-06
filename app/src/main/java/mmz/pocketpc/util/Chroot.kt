package mmz.pocketpc.util

import mmz.pocketpc.AppContext
import mmz.pocketpc.R
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object Chroot {
    private var fakedid = ""
    lateinit var bindir : File
    fun prepareBin(){
        bindir = File(AppContext.getFilesDir(),"bin")
        bindir.deleteRecursively()
        bindir.mkdirs()

        for (v in arrayOf("faked","getopt","chroot","gdbserver"))
            FileUtils.link(File(AppContext.libdir,"lib$v.so"), File(bindir, v))

        for (v in arrayOf("fakechroot","fakeroot","chrootdbg")) {
            val f = File(bindir,v)
            f.writeBytes(AppContext.getActivity().assets.open(v).readBytes())
            f.setExecutable(true, false)
        }
    }
    fun getFaked() : String{
        if(fakedid == ""){
            val cmd = arrayListOf(File(AppContext.libdir,"libfaked.so").path)
            val savefile = File(AppContext.getDataDir(),"faked.db")

            if(savefile.exists()) cmd.addAll(arrayOf("--load",savefile.path))
            cmd.addAll(arrayOf("--save-file",savefile.path))

            val p = Runtime.getRuntime().exec(cmd.toTypedArray())
            val stdInput = BufferedReader(InputStreamReader(p.inputStream))
            var s : String?
            while (stdInput.readLine().also{s = it} != null) {
                return s!!.also{fakedid = it}
            }
        }
        return fakedid
    }
    fun saveFaked(){
        Runtime.getRuntime().exec("kill -s USR1 ${fakedid.split(":")[1]}")
    }
    fun getPocketPCEnv() : Array<String>{
        return arrayOf(
            "POCKETPC_DIR=${AppContext.getFilesDir()!!.path}",
            "POCKETPC_LIBS=${AppContext.libdir.path}",
            "POCKETPC_BIN=${bindir.path}",
            "PATH=${bindir.path}"
        )
    }
    fun getFakerootEnv() : Array<String>{
        val savefile = File(AppContext.getDataDir(),"faked.db")
        val fakedopts = "--save-file ${savefile.path}" + if(savefile.exists()) " --load" else ""
        val env = arrayListOf(
            "FAKEROOTKEY=\"${getFaked()}\"",
            "FAKED=${File(AppContext.libdir,"libfaked.so").path}",
            "FAKEDOPTS=\"$fakedopts\"",
            "LD_PRELOAD=${File(AppContext.libdir,"libfakeroot.so").path}")
        if(savefile.exists()) env.add("PIPEIN=\"<${savefile.path}\"")
        return env.toTypedArray()
    }
    fun getFakerootArgs() : Array<String>{
        val savefile = File(AppContext.getDataDir(),"faked.db")
        val args = arrayListOf<String>(
            "--faked",File(AppContext.libdir,"libfaked.so").path,
            "-s",savefile.path,)
        if(savefile.exists()) args.addAll(arrayOf("-i",savefile.path))
        args.add("--")
        return args.toTypedArray()
    }
    fun getChrootEnv() : Array<String>{
        val env = arrayListOf<String>()
        //env.addAll(getFakerootEnv())
        //val preload = env[env.lastIndex] + ":${File(AppContext.libdir,"libfakechroot.so").path}"
        val preload =  "LD_PRELOAD=\$LD_PRELOAD:${File(AppContext.libdir,"libfakechroot.so").path}"
        //env.removeAt(env.lastIndex)
        env.addAll(arrayOf(
            "FAKECHROOT_BASE=${File(AppContext.getDataDir(),"rootfs").path}",
            preload))
        return env.toTypedArray()
    }
    fun getChrootCmd(cmdaft : String = ""): String {
        val cmd = "fakechroot"
        return "$cmd $cmdaft".also{ println("Chroot CMD: $it") }
    }
    fun getFakerootCmd(cmdaft : Array<String> = arrayOf<String>()) : String{
        val cmd = arrayListOf<String>()
        //val savefile = File(AppContext.getDataDir(),"faked.db")
        cmd.addAll(getFakerootEnv())
        //cmd.addAll(getFakerootArgs())
        cmd.addAll(cmdaft)
        //cmd.add(cmdaft[0])
        //cmd.addAll(arrayOf("--faked",File(AppContext.libdir,"libfaked.so").path))
        //if(savefile.exists()) cmd.addAll(arrayOf("-i",savefile.path))
        //cmd.addAll(arrayOf("-s",savefile.path,"--"))
        //cmd.addAll(getFakerootArgs())
        //cmd.addAll(cmdaft.sliceArray(IntRange(1,cmdaft.size-1)))
        return cmd.joinToString(" ").also{ println("Fakeroot CMD: $it") }
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