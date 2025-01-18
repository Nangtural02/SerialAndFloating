package com.example.myserialapp

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter

@Composable
fun insertImageButton() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    var rotationAngle by remember { mutableStateOf(0f) }
    var translationX by remember { mutableStateOf(0f) }
    var translationY by remember { mutableStateOf(0f) }

    Column(modifier = Modifier.fillMaxSize()) {
        // 이미지 선택 버튼
        Button(onClick = { launcher.launch("image/*") }) {
            Text(text = "이미지 선택")
        }

        if (imageUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()

            ) {
                // 이미지 그리기
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Blueprint",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            translationX = translationX,
                            translationY = translationY,
                            rotationZ = rotationAngle
                        ),
                    contentScale = ContentScale.FillBounds
                )

                // Canvas를 이미지 위에 덮기
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // X축 그리기
                    drawLine(
                        color = Color.Blue,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 5f
                    )
                    // Y축 그리기
                    drawLine(
                        color = Color.Red,
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 5f
                    )
                }
            }

            // 슬라이더 UI (이미지와 좌표축 아래에 배치)
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(text = "회전 각도")
                Slider(value = rotationAngle, onValueChange = { rotationAngle = it }, valueRange = -180f..180f)

                Text(text = "X축 이동")
                Slider(value = translationX, onValueChange = { translationX = it }, valueRange = -500f..500f)

                Text(text = "Y축 이동")
                Slider(value = translationY, onValueChange = { translationY = it }, valueRange = -500f..500f)
            }
        }
    }
}
