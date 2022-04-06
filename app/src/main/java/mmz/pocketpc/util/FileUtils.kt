package mmz.pocketpc.util

import androidx.compose.ui.graphics.Color
import mmz.pocketpc.AppContext
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.*
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.text.DecimalFormat
import kotlin.math.log10

class DbgStream : OutputStream(){
    override fun write(p0: Int) {}
    override fun write(b: ByteArray?) {
        println("Dbg: ${b.toString()}")
    }
}

object FileUtils{
    private const val BUFFERSIZE = 4096

    fun readableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble()))
            .toString() + " " + units[digitGroups]
    }

    fun tohex(bytes: ByteArray): String{
        val res = StringBuilder()
        for (byte in bytes) res.append(String.format("%02x",byte))
        return res.toString()
    }

    fun link(src: File, dest: File, symbolic: Boolean = false){
        val arr = arrayListOf("ln")
        if(symbolic) arr.add("-s")
        arr.addAll(arrayOf(src.path, dest.path))
        Runtime.getRuntime().exec(arr.toTypedArray())
    }

    fun downloadToString(
        url: URL,
        onDone: (res: String) -> Unit = {},
        headers: List<Pair<String, String>> = emptyList()
    ){
        AppContext.mtLaunch {
            val httpConn = url.openConnection() as HttpURLConnection
            headers.forEach {
                httpConn.setRequestProperty(it.first, it.second)
            }
            if(httpConn.responseCode == HttpURLConnection.HTTP_OK){
                val inputStream = httpConn.inputStream
                val outputStream = ByteArrayOutputStream()

                var bytesRead : Int
                val buffer = ByteArray(BUFFERSIZE)

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                inputStream.close()
                onDone(outputStream.toString())
            }else{
                println("[HTTP] Error downloading "+url.path+" (${httpConn.responseCode}, ${httpConn.responseMessage})")
                throw IOException("[HTTP] Error downloading "+url.path+" (${httpConn.responseCode}, ${httpConn.responseMessage})")
            }
        }
    }

    fun downloadFile(
        url: URL,
        outfile: File,
        onStart: (size: Long) -> Unit = {},
        onProgress: (read: Long) -> Unit = {},
        onDone: (hash: String) -> Unit = {}
    ) {
        AppContext.mtLaunch {
            if(outfile.exists()) outfile.delete()
            outfile.createNewFile()

            val httpConn = url.openConnection() as HttpURLConnection
            if(httpConn.responseCode == HttpURLConnection.HTTP_OK){
                onStart(httpConn.getHeaderField("Content-Length").toLong())

                val inputStream = httpConn.inputStream
                val outputStream = outfile.outputStream()

                val digest = MessageDigest.getInstance("SHA-256")
                val doutputStream = DigestOutputStream(outputStream, digest)

                var bytesRead : Int
                var totalbytes : Long = 0
                val buffer = ByteArray(BUFFERSIZE)

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    doutputStream.write(buffer, 0, bytesRead)
                    totalbytes += bytesRead
                    onProgress(totalbytes)
                }

                outputStream.close()
                inputStream.close()
                val hash = tohex(doutputStream.messageDigest.digest())
                doutputStream.close()

                onDone(hash)
            }else{
                throw IOException("[HTTP] Error downloading "+url.path+" (${httpConn.responseCode}, ${httpConn.responseMessage})")
            }
        }
    }

    fun downloadToFileTask(
        url: URL,
        outfile: File,
        onDone: (size: Long, task: Task, hash: String) -> Unit = { size: Long, tsk: Task, hash: String -> },
        doRemove: Boolean = true,
        task: Task = Task("Downloading ${url.path.substring(url.path.lastIndexOf('/') + 1, url.path.length)}")
    ){
        val fname = url.path.substring(url.path.lastIndexOf('/') + 1, url.path.length)
        var size: Long = 0
        if(!task.added) AppContext.taskBar.addTask(task)

        downloadFile(url, outfile, {
            size = it
            task.messageState.value = "Downloading $fname (${readableFileSize(it)})"
        }, {
            task.progressState.value = it.toFloat() / size.toFloat()
        }, {
            if(doRemove) AppContext.taskBar.TaskList.remove(task)
            onDone(size, task, it)
        })
    }

    data class CmdAdd(val before: Array<String>? = null, val after: Array<String>? = null, val envadd: Array<String>? = null)

    fun extractTarGz(
        infile: File,
        outdir: File,
        onProgress: (read: Long) -> Unit = {},
        onDone: (size: Long) -> Unit = {},
        froot: Boolean = false
    ){
        AppContext.mtLaunch {
            if(outdir.exists()) outdir.deleteRecursively()
            outdir.mkdirs()

            val bi = BufferedInputStream(infile.inputStream())
            val gzi = GzipCompressorInputStream(bi)

            var bytesRead : Int
            val buffer = ByteArray(BUFFERSIZE)

            //val cmd = arrayListOf<String>()
            //cmd.addAll("tar xf - -C ${outdir.path}".split(" "))
            //cmd.addAll(arrayOf("xf","-","-C",outdir.path)) //xf - -C ${outdir.path}

            val taros = Runtime.getRuntime().exec("tar xf - -C ${outdir.path}",
                if(froot) Chroot.getFakerootEnv() else null).outputStream

            while (gzi.read(buffer).also { bytesRead = it } != -1) {
                taros.write(buffer, 0, bytesRead)
                onProgress(gzi.compressedCount)
            }

            taros.close()
            if(froot) Chroot.saveFaked()
            onDone(gzi.bytesRead)
        }
    }

    fun extractTarGzTask(
        infile: File,
        outfolder: File,
        onDone: (size: Long, task: Task) -> Unit = { sz: Long, tsk: Task -> },
        froot: Boolean = false,
        doRemove: Boolean = true,
        task: Task = Task("Extracting ${infile.name}", Color(230,230,100))
    ){
        val size = infile.length()
        if(!task.added)
            AppContext.taskBar.addTask(task)
        else{
            task.messageState.value = "Extracting ${infile.name}"
            task.colorState.value = Color(230,230,100)
            task.progressState.value = 0f
        }

        extractTarGz(infile, outfolder,{
            task.progressState.value = it.toFloat() / size.toFloat()
        },{
            if(doRemove) AppContext.taskBar.TaskList.remove(task)
            onDone(it, task)
        }, froot)
    }
}