package mmz.pocketpc.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mmz.pocketpc.ui.theme.PocketPCTheme

class Task(msg: String = "", private val col: Color = Color(0,180,0)){
    val messageState = mutableStateOf(msg)
    val progressState = mutableStateOf(0f)
    val colorState = mutableStateOf(col)
    var added = false
    @Composable
    fun Comp(){
        val messageRem = remember { messageState }
        val progressRem = remember { progressState }
        Box(
            Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(Color.Gray)
                .padding(top = 1.dp)){
            Box(Modifier.fillMaxSize().background(Color.LightGray)){
                Box(Modifier
                    .fillMaxWidth(progressRem.value)
                    .fillMaxHeight()
                    .background(colorState.value)
                )
                Text(text = messageRem.value, fontSize = 10.sp)
            }
        }
    }
}

class TaskBar{

    val TaskList = mutableStateListOf<Task>()

    fun addTask(task: Task){
        if(!task.added){
            task.added = true
            TaskList.add(task)
        }
    }

    @Composable
    fun TaskBar() {
        val tlist = remember { TaskList }
        Column(){
            tlist.forEach{
                it.Comp()
            }
        }
    }
}

@Preview
@Composable
fun TaskPreview() {
    PocketPCTheme {
        val task = Task("Downloading ubuntu-base-21.04-base-arm64.tar.gz (25,7 MB)")
        task.progressState.value = 0.5f
        task.Comp()
    }
}