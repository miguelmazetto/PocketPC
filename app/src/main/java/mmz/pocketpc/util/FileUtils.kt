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

object FileUtils{
    private const val BUFFERSIZE = 4096

    data class FPermInd(val read: Boolean, val write: Boolean, val exec: Boolean){
        override fun toString(): String {
            return (if(read) "r" else "-") +
                    (if(write) "w" else "-") +
                    (if(exec) "x" else "-")
        }
    }
    class FPerm(val octalstr: String){
        val owner = FPermInd(
            (octalstr[0].code and 4)>0,
            (octalstr[0].code and 2)>0,
            (octalstr[0].code and 1)>0)
        val group = FPermInd(
            (octalstr[1].code and 4)>0,
            (octalstr[1].code and 2)>0,
            (octalstr[1].code and 1)>0)
        val other = FPermInd(
            (octalstr[2].code and 4)>0,
            (octalstr[2].code and 2)>0,
            (octalstr[2].code and 1)>0)
        val anyread = owner.read || group.read || owner.read
        val anywrite = owner.write || group.write || owner.write
        val anyexec = owner.exec || group.exec || other.exec
        override fun toString(): String {
            return owner.toString()+
                    group.toString()+
                    other.toString()
        }
    }

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

    fun createLink(file: String, link: String, symbolic: Boolean = false){
        File(link).parentFile?.mkdirs()
        //val parent : File? = File(link).parentFile?.mkdirs()
        //if (parent != null && !parent.exists()) parent.mkdirs()
        //println("run: ln ${if(symbolic) "-s " else ""}$file $link")
        Runtime.getRuntime().exec("ln ${if(symbolic) "-s " else ""}$file $link")
    }

    /*fun setFilePerms(file: File, oct: Int){
        val perm = FPerm(oct.toString())
        file.setReadable(perm.anyread, !perm.group.read && !perm.other.read)
        file.setWritable(perm.anywrite, !perm.group.write && !perm.other.write)
        file.setExecutable(perm.anyexec, !perm.group.exec && !perm.other.exec)
    }*/
    fun setFilePerms(file: File, oct: Int){
        Runtime.getRuntime().exec("chmod $oct ${file.path}")
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

    fun extractTarGz(
        infile: File,
        outdir: File,
        onStart: (size: Long) -> Unit = {},
        onProgress: (read: Long) -> Unit = {},
        onDone: (size: Long) -> Unit = {}
    ){
        onStart(infile.length())
        AppContext.mtLaunch {
            if(outdir.exists()) outdir.deleteRecursively()
            outdir.mkdirs()

            val bi = BufferedInputStream(infile.inputStream())
            val gzi = GzipCompressorInputStream(bi)
            val o = TarArchiveInputStream(gzi)
            var entry : TarArchiveEntry?

            val buffer = ByteArray(BUFFERSIZE)

            while(o.nextEntry.also{entry = it as TarArchiveEntry?} != null){
                val tentry = entry!!
                if (!o.canReadEntryData(tentry)) {
                    println("[Extract] Cannot read: ${tentry.name}")
                    continue
                }
                val f = File(outdir, tentry.name)
                fun getLink() : File{
                    //println("getLink: ${tentry.linkName} -> ${f.path}")
                    val res =  if(tentry.linkName[0] == '/')
                        File(outdir, tentry.linkName)
                    else
                        File(f.parentFile, tentry.linkName)
                    //println("\t${res.path}")
                    return res
                }
                if (tentry.isDirectory) {
                    if (!f.isDirectory && !f.mkdirs()) {
                        //throw IOException("failed to create directory $f")
                        println("failed to create directory $f")
                    }else{
                        setFilePerms(f, tentry.mode)
                    }
                } else if(tentry.isLink) {
                    createLink(f.path, getLink().path)
                } else if(tentry.isSymbolicLink){
                    createLink(f.path, getLink().path,true)
                } else {
                    val parent = f.parentFile
                    if (parent == null || (!parent.isDirectory && !parent.mkdirs())) {
                        println("failed to create directory $parent")
                        continue
                    }

                    var bytesRead : Int
                    val outputStream = f.outputStream()

                    while (o.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        onProgress(gzi.compressedCount)
                    }

                    outputStream.close()
                    setFilePerms(f, tentry.mode)
                }
            }
            onDone(gzi.bytesRead)
        }
    }
    fun extractTarGzTask(
        infile: File,
        outfolder: File,
        onDone: (size: Long, task: Task) -> Unit = { sz: Long, tsk: Task -> },
        doRemove: Boolean = true,
        task: Task = Task("Extracting ${infile.name}", Color(230,230,100))
    ){
        var size: Long = 0
        if(!task.added)
            AppContext.taskBar.addTask(task)
        else{
            task.messageState.value = "Extracting ${infile.name}"
            task.colorState.value = Color(230,230,100)
            task.progressState.value = 0f
        }

        extractTarGz(infile, outfolder,{ size = it },{
            task.progressState.value = it.toFloat() / size.toFloat()
        },{
            if(doRemove) AppContext.taskBar.TaskList.remove(task)
            onDone(it, task)
        })
    }
}