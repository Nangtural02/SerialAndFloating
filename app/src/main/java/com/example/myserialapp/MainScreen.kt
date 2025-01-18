package com.example.myserialapp

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.remember
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
import com.example.myserialapp.positioningUtill.Anchor
import com.example.myserialapp.positioningUtill.CoordinatePlane
import com.example.myserialapp.positioningUtill.Point
import com.example.myserialapp.positioningUtill.getPoint
import com.example.myserialapp.ui.theme.MySerialAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun MainScreen(viewModel: SerialViewModel, innerPadding:PaddingValues){
    var dialog by rememberSaveable { mutableStateOf(false) }




    viewModel.anchorList = listOf(Anchor(coordinateX = 10f, coordinateY = 10f), Anchor(coordinateX = 0f, coordinateY = 0f))

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

            item{

            }

            item {
                NowRangingDataLog(viewModel = viewModel)
            }
            item{
                insertImageButton()
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
fun NowRangingDataLog(viewModel: SerialViewModel, imageUri: Uri? = null){
    val data = viewModel.nowRangingData
    Column(modifier = Modifier
        .background(Color.Green)
        .fillMaxWidth()
        .padding(end = 5.dp)){
        Text(text = "Block: ${data.value.blockNum}")
        Text(text = "Distance: \n")
        data.value.distanceList.forEach{ rangingDistance ->
            Text(text="${rangingDistance.id}: ${rangingDistance.distance}m, PDOA: ${rangingDistance.PDOA}_degree, AOA: ${rangingDistance.AOA}_degree")
        }
        CoordinatePlane(anchorList = viewModel.anchorList.map{it.getPoint()},
            viewModel.nowRangingData.value.coordinates,
            distanceList = data.value.distanceList.map{it.distance})
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MySerialAppTheme {
        MainScreen(SerialViewModel(Application()),PaddingValues(0.dp))
    }
}