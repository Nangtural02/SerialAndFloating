package com.example.myserialapp

import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myserialapp.positioningUtill.CoordinatePlane
import com.example.myserialapp.ui.theme.MySerialAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun MainScreen(viewModel: SerialViewModel, innerPadding:PaddingValues){
    var dialog by rememberSaveable { mutableStateOf(false) }
    val connected = viewModel.connected.asStateFlow().collectAsState().value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.TopCenter
    ){
        LazyColumn {
            item {
                ConnectingButton(viewModel = viewModel)
            }

            item {
                NowRangingDataLog(viewModel = viewModel)
            }

            //SerialLog(viewModel)

        }
    }
}
@Composable
fun ConnectingButton(viewModel: SerialViewModel){
    val connectedUSBItem = viewModel.connectedUSBItem.collectAsState().value
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    if (connectedUSBItem == null) {
        Button(
            onClick = {
                coroutineScope.launch {
                    viewModel.connectSerialDevice(context)
                }
            },
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Connect to Serial Device")
        }
    }
    else {
        Row(modifier = Modifier
            .fillMaxWidth()
            ) {
            Card(
                modifier = Modifier.height(80.dp)
            ) {
                androidx.compose.material3.Text(
                    text = "Port:${connectedUSBItem.port.portNumber}\n" +
                            "Name:${connectedUSBItem.device.deviceName}\n" +
                            "${connectedUSBItem.device.deviceId}\n "
                )
            }
            Spacer(modifier = Modifier.weight((1f)))
            Button(
                onClick = {
                    coroutineScope.launch{
                        viewModel.disConnectSerialDevice()
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Magenta),
                modifier = Modifier
                    .width(80.dp)
                    .height(80.dp)
            ){
                Text("Disconnect")
            }
        }
    }

}
@Composable
fun NowRangingDataLog(viewModel: SerialViewModel){
    val data = viewModel.nowRangingData
    Column(modifier = Modifier
        .background(Color.Green)
        .fillMaxWidth()
        .padding(end = 5.dp)){
        Text(text = "Block: ${data.value.blockNum}      lastCoordinateBlock: ${viewModel.lastCoordinateBlock.intValue}")
        Text(text = "Distance: \n")
        data.value.distanceList.forEach{ rangingDistance ->
            Text(text="${rangingDistance.id}: ${rangingDistance.distance}m, PDOA: ${rangingDistance.PDOA}_degree, AOA: ${rangingDistance.AOA}_degree")
        }
        CoordinatePlane(anchorList = viewModel.anchorList,
            pointsList = listOf(
                viewModel.calculated4_3Result,
                viewModel.calculated4_4Result,
                viewModel.calculated2_2Result

            ),
            distanceList = data.value.distanceList.map{it.distance})
        if(data.value.coordinates.isNotEmpty()){
            data.value.coordinates.forEach { point ->
                Text(text = "Coordinate(${"%.2f".format(point.x)},${"%.2f".format(point.y)},${"%.2f".format(point.z)})")
            }
        }else {
            Text(text= "not all anchor connected")

        }
        

    }
}

@Composable
fun SerialLog(viewModel:SerialViewModel){
    LazyColumn() {
        item {
            Text(text = viewModel.lineTexts)
        }
    }
}
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MySerialAppTheme {
        MainScreen(SerialViewModel(Application()),PaddingValues(0.dp))
    }
}