package mmz.pocketpc.util

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import mmz.pocketpc.AppContext
import mmz.pocketpc.screens.Distro
import mmz.pocketpc.screens.DistroList
import mmz.pocketpc.screens.updateInstalledDistro
import java.io.File

private fun getCfgFile() : File{
    return File(AppContext.getDataDir(), "config.json")
}

@Serializable
data class SaveableCfg(
    var selectedDistro: Int = 0,
    var selectedVersion: Int = 0,
    var prootName: String = ""
)

object GlobalConfig {
    var installedDistro: String = ""
    var installedVersion: String = ""
    val installStatus: MutableState<Int> = mutableStateOf(0)

    var S = SaveableCfg()
    fun save(){
        getCfgFile().writeText(Json.encodeToString(S))
    }
    fun load(){
        if(getCfgFile().exists())
            S = Json.decodeFromString<SaveableCfg>(getCfgFile().readText())
        updateInstalledDistro()
        if(installedDistro != "")
            for(i in DistroList.indices){
                val distro = DistroList[i]
                if(distro.name == installedDistro){
                    S.selectedDistro = i
                    for (k in distro.versions.indices){
                        val ver = distro.versions[k]
                        if(ver == installedVersion){
                            S.selectedVersion = k
                            break
                        }
                    }
                    break
                }
            }
    }
}

/*object Config {

    private var cfgFile = File("")

    fun saveConfig(){ cfgFile.writeText(Json.encodeToString(GlobalConfig)) }
    private fun readConfig(){ GlobalConfig = Json.decodeFromString<globalCfg>(cfgFile.readText()) }
    fun loadConfig(){
        cfgFile = File(AppContext.getDataDir(), "config.json")
        if (cfgFile.exists()){
            readConfig()
        }else{
            cfgFile.createNewFile()
            saveConfig()
        }
    }
}*/