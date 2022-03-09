package mmz.pocketpc.util

import mmz.pocketpc.AppContext
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.net.URL

fun getProot(){
    fun downloadProot(url: URL, tag: String){
        val tempfile = File(AppContext.getDataDir(),"files/proot-$tag-temp")
        FileUtils.downloadToFileTask(url, tempfile,{ _, _, _ ->
            for (f in File(AppContext.getDataDir(), "files/").listFiles()!!)
                if(f.name.startsWith("proot-") && !f.name.startsWith("proot-$tag"))
                    f.delete()
            tempfile.renameTo(File(AppContext.getDataDir(),"files/proot-$tag"))
            GlobalConfig.S.prootName = "proot-$tag"
            GlobalConfig.save()
        })
    }
    FileUtils.downloadToString(URL("https://api.github.com/repos/proot-me/proot/releases/latest"),{ jsontext ->
        val json = JSONTokener(jsontext).nextValue() as JSONObject
        val tag = json.getString("tag_name")
        if(File(AppContext.getDataDir(),"files/proot-$tag").exists()){
            if(GlobalConfig.S.prootName != "proot-$tag"){
                GlobalConfig.S.prootName = "proot-$tag"
                GlobalConfig.save()
            }
            return@downloadToString
        }
        val assets = json.getJSONArray("assets")
        for (i in 0 until assets.length()){
            val asset = assets[i] as JSONObject
            val name = asset.getString("name")
            if(name.indexOf(
                    when(AppContext.arch){
                        "armv71" -> "arm"
                        else -> AppContext.arch }) != -1){
                downloadProot(URL(asset.getString("browser_download_url")), tag)
            }
        }
    })
}