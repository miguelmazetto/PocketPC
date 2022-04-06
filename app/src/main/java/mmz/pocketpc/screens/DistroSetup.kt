package mmz.pocketpc.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mmz.pocketpc.AppContext
import mmz.pocketpc.ui.theme.PocketPCTheme
import mmz.pocketpc.util.*
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.regex.Pattern

class Distro(val name: String, defver: List<String>, con: (t: Distro)->Unit){
    var versions = defver
    var getVer : ()->Unit = {}
    var getURL : ()->URL = {URL("")}
    var getShaURL : ()->URL = {URL("")}
    init { con(this) }
}
//class Distro(val name: String, val shaurl: URL, val getURL: () -> URL, val getVer: ())
/*val DistroList = listOf(
    Distro("Ubuntu 21.04", URL("https://cdimage.ubuntu.com/ubuntu-base/releases/21.04/release/SHA256SUMS")) {
        val osarch = System.getProperty("os.arch")
        val arch = when (osarch) {
            "x86_64" -> "amd64"
            "aarch64" -> "arm64"
            "armv71" -> "armhf"
            else -> {println("Unsupported arch: $osarch"); "invalid"}
        }
        URL("https://cdimage.ubuntu.com/ubuntu-base/releases/21.04/release/ubuntu-base-21.04-base-$arch.tar.gz")
    }
)*/
val DistroList : List<Distro> = listOf(
    Distro("Ubuntu", listOf("21.10","21.04")) {
        it.getVer = {
            FileUtils.downloadToString(URL("https://cdimage.ubuntu.com/ubuntu-base/releases/"),{ str ->
                val m = Pattern.compile("<li><a href=\"[0-9.]*/\">").matcher(str)
                if(!m.find()) return@downloadToString
                val l = ArrayList<String>()
                while(m.find())
                    l.add(m.group().substringAfter("<li><a href=\"").substringBefore("/\">"))
                l.reverse()
                it.versions = l
            })
        }
        it.getURL = {
            val ver = it.versions[GlobalConfig.S.selectedVersion]
            val arch = when (System.getProperty("os.arch")) {
                "x86_64" -> "amd64"
                "aarch64" -> "arm64"
                "armv71" -> "armhf"
                else -> "invalid"}
            URL("https://cdimage.ubuntu.com/ubuntu-base/releases/$ver/release/ubuntu-base-$ver-base-$arch.tar.gz")
        }
        it.getShaURL = {
            val ver = it.versions[GlobalConfig.S.selectedVersion]
            URL("https://cdimage.ubuntu.com/ubuntu-base/releases/$ver/release/SHA256SUMS")
        }
    }
)

fun updateDistroVersions(){
    DistroList.forEach { it.getVer() }
}

private fun getAfterNoSpace(a: String, s: String) : String{
    var st = s.substring(s.lastIndexOf(a)+1)
    while(st[0] == ' ')
        st = st.substring(1)
    return st
}

fun updateInstalledDistro(){
    val lsb = File(AppContext.getDataDir(),"rootfs/etc/lsb-release")
    GlobalConfig.installedDistro = ""
    GlobalConfig.installedVersion = ""
    if(lsb.exists())
        for(s in lsb.readText().split("\n")){
            if(s.startsWith("DISTRIB_ID")){
                GlobalConfig.installedDistro = getAfterNoSpace("=",s)
            }else if(s.startsWith("DISTRIB_RELEASE")){
                GlobalConfig.installedVersion = getAfterNoSpace("=",s)
            }
        }
    println("Detected distro: ${GlobalConfig.installedDistro} ${GlobalConfig.installedVersion}")
}

fun installDistro() {
    val inst = GlobalConfig.installStatus
    if(inst.value != 0) return
    inst.value = 1
    val distro = DistroList[GlobalConfig.S.selectedDistro]
    val url = distro.getURL()
    val fname = url.path.substring(url.path.lastIndexOf('/') + 1, url.path.length)
    val datadir = AppContext.getDataDir()
    try {
        val outfile = File(datadir, "rootfs.tar.gz")
        var nextstep = false
        var hash : String = "_"
        var fhash : String = ""
        val task : Task = Task("Downloading $fname")

        fun extractDistro(){
            val tempdir = File(datadir,"rootfs_temp")
            val outdir = File(datadir,"rootfs")
            FileUtils.extractTarGzTask(outfile, tempdir, { extractedsize, tk ->
                println("Extracted size: ${FileUtils.readableFileSize(extractedsize)}")
                outfile.delete()
                if(outdir.exists()) outdir.deleteRecursively()
                tempdir.renameTo(outdir)
                inst.value = 2
                updateInstalledDistro()
            }, true,true,task)
        }

        var tryDownload : ()->Unit = {}

        fun verifyDownload(){
            println("verifyDownload:\n\t\"hash: $hash\"\n\tfhash: $fhash")
            if(fhash.equals(hash))
                extractDistro()
            else
                tryDownload()
        }

        tryDownload = {
            println("Start trydownload: ${url.toString()}")
            FileUtils.downloadToFileTask(url,outfile,{ size, tsk, hsh ->
                fhash = hsh
                println("FHash: $fhash")
                verifyDownload()
            },false, task)
        }

        FileUtils.downloadToString(distro.getShaURL(),{
            for (s in it.split("\n")) {
                if(s.endsWith(fname)){
                    hash = s.substring(0,64)
                    if(nextstep) verifyDownload()
                    else nextstep = true
                    println("CHash: $s")
                    return@downloadToString
                }
            }
            println("Failed to get sha256 hash")
            throw IOException("Failed to get sha256 hash!")
        })

        tryDownload()

    } catch (e: IOException) {
        inst.value = 0
        println("Download failed!")
    }
}

fun uninstallDistro(){
    val inst = GlobalConfig.installStatus
    inst.value = 3
    val dir = File(AppContext.getDataDir(),"rootfs")

    if(dir.exists()) {
        AppContext.mtLaunch {
            dir.deleteRecursively()
            inst.value = 0
        }
    }else{
        inst.value = 0
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DDMenu(label: String, sel: String, getVals: ()->List<String>, modifier: Modifier, onClick: (i: Int)->Unit){
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            readOnly = true,
            value = sel,
            onValueChange = { },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            val vals = getVals()
            for (i in vals.indices){
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onClick(i)
                    }
                ) {Text(text = vals[i])}
            }
        }
    }
}

private fun getInstalledDistroStr() : String{
    return "${GlobalConfig.installedDistro} ${GlobalConfig.installedVersion}"
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DistroSetup() {
    val inst = remember { GlobalConfig.installStatus }
    val openUninstallDialog = remember { mutableStateOf(false) }
    val seldistro = remember { mutableStateOf( DistroList[GlobalConfig.S.selectedDistro] ) }
    val selversion = remember { mutableStateOf( seldistro.value.versions[GlobalConfig.S.selectedVersion] ) }
    Column(modifier = Modifier
        .fillMaxSize()) {

        Row {
            DDMenu(
                label = "Distro",
                sel = seldistro.value.name,
                getVals = {
                    MutableList<String>(DistroList.size) {
                        DistroList[it].name
                    }.toList()
                },
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                GlobalConfig.S.selectedDistro = it
                seldistro.value = DistroList[it]
                GlobalConfig.save()
            }
            DDMenu(
                label = "Version",
                sel = selversion.value,
                getVals = { seldistro.value.versions },
                modifier = Modifier.fillMaxWidth()
            ) {
                GlobalConfig.S.selectedVersion = it
                selversion.value = seldistro.value.versions[it]
                GlobalConfig.save()
            }
        }
        /*var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            TextField(
                readOnly = true,
                value = DistroList[dcfg.selectedDistro].name,
                onValueChange = { },
                label = { Text("Distro") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.fillMaxWidth(0.5f)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                DistroList.forEach { distro ->
                    DropdownMenuItem(
                        onClick = {
                            dcfg.selectedDistro = DistroList.indexOf(distro)
                            Config.saveConfig()
                            expanded = false
                        }
                    ) {Text(text = distro.name)}
                }
            }
        }*/
        Row(verticalAlignment = Alignment.CenterVertically){
            Button(onClick = {
                when(inst.value){
                0 -> installDistro()
                2 -> { openUninstallDialog.value = true }
            }

            }, enabled = when(inst.value){
                1 -> false
                3 -> false
                else -> true
            }, modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(horizontal = 5.dp)) {
                Text(when(inst.value){
                    0 -> "Install"
                    1 -> "Installing"
                    2 -> "Uninstall"
                    3 -> "Uninstalling"
                    else -> "Unknown"
                })
            }
            Text(
                text = "Distro Status: "+when(inst.value){
                    0 -> "Not installed"
                    1 -> "Installing ${seldistro.value.name} ${selversion.value}"
                    2 -> "Installed ${getInstalledDistroStr()}"
                    3 -> "Uninstalling ${getInstalledDistroStr()}"
                    else -> "${inst.value} (Unknown)"
                }
            )
        }
    }
    if(openUninstallDialog.value){
        val invertedColors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.background,
            contentColor = MaterialTheme.colors.primary)

        AlertDialog(
            onDismissRequest = { openUninstallDialog.value = false },
            title = { Text("Uninstall distro") },
            text = {
                Text("Are you sure you want to uninstall the distro ${getInstalledDistroStr()}?")
            },
            buttons = {
                Row(
                    modifier = Modifier
                        .padding(bottom = 8.dp, end = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { openUninstallDialog.value = false },
                        colors = invertedColors, elevation = null
                    ) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        openUninstallDialog.value = false
                        uninstallDistro()
                    }, colors = invertedColors, elevation = null) {
                        Text("Uninstall")
                    }
                }
            }
        )
    }
}

@Preview
@Composable
fun DistroSetupPreview() {
    DistroList[0].versions = listOf("21.10","21.04")
    PocketPCTheme {
        DistroSetup()
    }
}