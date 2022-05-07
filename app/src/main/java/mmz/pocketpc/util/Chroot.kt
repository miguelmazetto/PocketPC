package mmz.pocketpc.util

import mmz.pocketpc.AppContext
import mmz.pocketpc.R
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object Chroot {
    private var fakedid = ""
    lateinit var bindir : File
    fun getRootfs() : File{
        return File(AppContext.getDataDir(),"rootfs")
    }
    fun getFakedDB() : File{
        return File(AppContext.getDataDir(),"faked_fs.db")
    }
    fun prepareBin(){
        bindir = File(AppContext.getFilesDir(),"bin")
        bindir.deleteRecursively()
        bindir.mkdirs()

        for (v in arrayOf("faked","getopt","chroot","gdbserver"))
            FileUtils.link(File(AppContext.libdir,"lib$v.so"), File(bindir, v))

        for (v in arrayOf("fakechroot","fakeroot","chrootdbg","sudo")) {
            val f = File(bindir,v)
            f.writeBytes(AppContext.getActivity().assets.open(v).readBytes())
            f.setExecutable(true, false)
        }
    }
    fun getBin(b : String) : File{
        return File(bindir, b)
    }
    fun getLib(b : String) : File{
        return File(AppContext.libdir, b)
    }
    fun getFaked() : String{
        if(fakedid == ""){
            val cmd = arrayListOf(File(AppContext.libdir,"libfaked.so").path)
            val savefile = getFakedDB()

            if(savefile.exists()) cmd.addAll(arrayOf("--load",savefile.path))
            cmd.addAll(arrayOf("--save-file",savefile.path))

            val p = Runtime.getRuntime().exec(cmd.toTypedArray())
            val stdInput = BufferedReader(InputStreamReader(p.inputStream))
            var s : String?
            return stdInput.readLine().also{ println("Fakerootkey: $it") }
            /*while (stdInput.readLine().also{s = it} != null) {

                return s!!.also{fakedid = it}
            }*/
        }
        return fakedid.also{ println("Fakerootkey: $it") }
    }
    fun saveFaked(){
        Runtime.getRuntime().exec("kill -s USR1 ${fakedid.split(":")[1]}")
    }
    fun getPocketPCEnv() : Array<String>{
        return arrayOf(
            "POCKETPC_DIR=${AppContext.getFilesDir()!!.path}",
            "POCKETPC_LIBS=${AppContext.libdir.path}",
            "POCKETPC_BIN=${bindir.path}",
            "PATH=${bindir.path}",
            "FAKECHROOT_DEBUG=1"
        )
    }
    fun getFakerootEnv() : Array<String>{
        val savefile = getFakedDB()
        val fakedopts = "--save-file ${savefile.path}" + if(savefile.exists()) " --load" else ""
        val env = arrayListOf(
            "FAKEROOTKEY=${getFaked()/*.split(":")[0]*/}",
            //"FAKED=\"${getBin("faked").path}\"",
            //"FAKEDOPTS=\"$fakedopts\"",
            "LD_LIBRARY_PATH=${System.getenv("LD_LIBRARY_PATH")}:${AppContext.libdir.path}",
            "LD_PRELOAD=${getLib("libfakeroot.so").path}",
            //"PATH=${System.getenv("PATH")}:$bindir"/*,
            "SHELL=/system/bin/sh")
        //if(savefile.exists()) env.add("PIPEIN=\"<${savefile.path}\"")
        //println("FakedPort: ${env[0]}")
        return env.toTypedArray()
    }
    fun getFakerootArgs() : Array<String>{
        val savefile = getFakedDB()
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
        val savefile = getFakedDB()
        cmd.addAll(arrayOf(
            getBin("fakeroot").path,
                "-s", savefile.path,
                "-l", getLib("libfakeroot.so").path,
                "--faked", getBin("faked").path))
        if(savefile.exists()) cmd.addAll(arrayOf("-i",savefile.path))
        cmd.add("--")
        cmd.addAll(cmdaft)
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