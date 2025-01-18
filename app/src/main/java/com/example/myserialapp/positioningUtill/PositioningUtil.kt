package com.example.myserialapp.positioningUtill

import android.util.Half.toFloat
import android.util.Log
import androidx.collection.mutableFloatListOf
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.widget.EdgeEffectCompat.getDistance
import com.example.myserialapp.ui.theme.MySerialAppTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
fun calcMiddleBy4Side(distances: List<Float>, anchorPosition: List<Point>): Point {
    val x = anchorPosition.map{it.x}
    val y = anchorPosition.map{it.y}
    val d = distances.map{it}

    val A1 = arrayOf(
        floatArrayOf(2 * (x[1] - x[0]), 2 * (y[1] - y[0])),
        floatArrayOf(2 * (x[3] - x[2]), 2 * (y[3] - y[2]))
    )
    val B1 = floatArrayOf(
        generateRight(x[1],y[1],d[1]) - generateRight(x[0],y[0],d[0]),
        generateRight(x[3],y[3],d[3]) - generateRight(x[2],y[2],d[2])
    )
    val A2 = arrayOf(
        floatArrayOf(2 * (x[1] - x[2]), 2 * (y[1] - y[2])),
        floatArrayOf(2 * (x[3] - x[0]), 2 * (y[3] - y[0]))
    )
    val B2 = floatArrayOf(
        generateRight(x[1],y[1],d[1]) - generateRight(x[2],y[2],d[2]),
        generateRight(x[3],y[3],d[3]) - generateRight(x[0],y[0],d[0])
    )
    val A3 = arrayOf(
        floatArrayOf(2 * (x[0] - x[2]), 2 * (y[0] - y[2])),
        floatArrayOf(2 * (x[3] - x[1]), 2 * (y[3] - y[1]))
    )
    val B3 = floatArrayOf(
        generateRight(x[0],y[0],d[0]) - generateRight(x[2],y[2],d[2]),
        generateRight(x[3],y[3],d[3]) - generateRight(x[1],y[1],d[1])
    )
    var resultVectorList: List<FloatArray> = mutableListOf(multiplyMatrixVector(invertMatrix(A1),B1), multiplyMatrixVector(invertMatrix(A2),B2), multiplyMatrixVector(invertMatrix(A3),B3))
    var meanPoint: Point = Point()
    resultVectorList.forEach{
        if(isPointInSquare(Point(it[0],it[1]), calcBy4Side(distances,anchorPosition))){
            meanPoint = Point(it[0],it[1])
        }
    }

    val meanArray = FloatArray(4)
    val zArray:MutableList<Float> = mutableListOf()
    for(i in 0..3){
        meanArray[i] = sqrt((meanPoint.x-anchorPosition[i].x).pow(2)+(meanPoint.y-anchorPosition[i].y).pow(2))
    }
    for(i in 0..3){
        if(distances[i].pow(2) >= meanArray[i].pow(2))
            zArray.add(sqrt(distances[i].pow(2) - meanArray[i].pow(2)))
    }

    return Point(meanPoint.x,meanPoint.y,zArray.average().toFloat())
}

fun sign(o: Point, a: Point, b: Point): Float {
    return (o.x - b.x) * (a.y - b.y) - (a.x - b.x) * (o.y - b.y)
}

fun isPointInTriangle(p: Point, p0: Point, p1: Point, p2: Point): Boolean {
    val b1 = sign(p, p0, p1) < 0.0
    val b2 = sign(p, p1, p2) < 0.0
    val b3 = sign(p, p2, p0) < 0.0

    return (b1 == b2) && (b2 == b3)
}

fun isPointInSquare(p: Point, quad: List<Point>): Boolean {
    // 사각형을 두 개의 삼각형으로 분할
    val t1 = listOf(quad[0], quad[1], quad[2])
    val t2 = listOf(quad[0], quad[2], quad[3])

    return isPointInTriangle(p, t1[0], t1[1], t1[2]) || isPointInTriangle(p, t2[0], t2[1], t2[2])
}

fun calcBy4Side(distances: List<Float>, anchorPosition: List<Point>): List<Point>{
    var results = emptyList<Point>()
    for (i in distances.indices) {
        results = results.plus(
            calcBy3Side(
                listOf(
                    distances[(i + 1) % distances.size],
                    distances[(i + 2) % distances.size],
                    distances[(i + 3) % distances.size]
                ),
                listOf(
                    anchorPosition[(i + 1) % distances.size],
                    anchorPosition[(i + 2) % distances.size],
                    anchorPosition[(i + 3) % distances.size]
                )
            )
        )
    }
    return results
}
fun calcBy3Side(distances: List<Float>, anchorPosition: List<Point>): Point {

    if(distances.size < 3) return Point(-66.66f,-66.66f,-66.66f)

    val x = anchorPosition.map{it.x}
    val y = anchorPosition.map{it.y}
    val z = anchorPosition.map{it.z}
    val d = distances.map{it}

    val A = arrayOf(
        floatArrayOf(2 * (x[1] - x[0]), 2 * (y[1] - y[0])),
        floatArrayOf(2 * (x[2] - x[0]), 2 * (y[2] - y[0]))
    )
    val B = floatArrayOf(
        generateRight(x[1],y[1],d[1]) - generateRight(x[0],y[0],d[0]),
        generateRight(x[2],y[2],d[2]) - generateRight(x[0],y[0],d[0])
    )
    val Ainv = invertMatrix(A)
    val result = multiplyMatrixVector(Ainv, B)


    return Point(result[0], result[1])
}
/*
fun calcByDoubleAnchor2Distance(anchor1:Int, anchor2: Int,anchorPositions: List<Point>, distances: List<Float>, lastZ:Float = 0f): List<Point>{
    val d1 = distances[anchor1]
    val d2 = distances[anchor2]

    return calcByDoubleAnchor2Distance(d1,d2, anchorPositions[anchor1],anchorPositions[anchor2])
}
fun calcByDoubleAnchor2Distance(d1:Float, d2:Float, p1: Point, p2:Point): List<Point>{
    val A = 4 * (p1.y-p2.y).pow(2)
    val B = -2 * (p1.y-p2.y) * (p2.x.pow(2)-p1.x.pow(2)+p1.y.pow(2)-p2.y.pow(2)+d2.pow(2)-d1.pow(2)+ 2 * p1.x * p2.x)
    val C = (p2.x.pow(2)-p1.x.pow(2)+p1.y.pow(2)-p2.y.pow(2)+d2.pow(2)-d1.pow(2)+ 2 * p1.x * p2.x).pow(2) - 4 * (p1.x - p2.x).pow(2) * d1.pow(2)
    val y1 = (-B+sqrt(B.pow(2)-4*A*C))/(2*A)
    val y2 = (-B-sqrt(B.pow(2)-4*A*C))/(2*A)
    val x1 = (generateRight(p1.x,p1.y,d1)-generateRight(p2.x,p2.y,d2)- 2*y1*(p1.y-p2.y))/(2*(p1.x-p2.x))
    val x2 = (generateRight(p1.x,p1.y,d1)-generateRight(p2.x,p2.y,d2)- 2*y2*(p1.y-p2.y))/(2*(p1.x-p2.x))
    return listOf(Point(x1,y1), Point(x2,y2))
}*/

fun calcByDoubleAnchor(distances: List<Float>,anchorPositions: List<Point>,actionSquare: List<Point>): Point {
    return calcByDoubleAnchor(0,1,distances, anchorPositions,actionSquare)
}
fun calcByDoubleAnchor(anchor1:Int, anchor2: Int, distances: List<Float>, anchorPositions: List<Point>, actionSquare: List<Point>): Point {
    val p1 = anchorPositions[anchor1]; val p2 = anchorPositions[anchor2]
    val distanceByAnchor:Float = p1.getDistance(p2)
    val cosTheta: Float = (distances[anchor1].pow(2)+distanceByAnchor.pow(2)-distances[anchor2].pow(2))/(2*distances[anchor1]*distanceByAnchor)
    val tanTheta: Float = sqrt(1-cosTheta.pow(2))/cosTheta
    val m = (p2.y-p1.y)/(p2.x-p1.x)
    val mPrime = (m+tanTheta)/(1-m*tanTheta)
    val A = arrayOf(
        floatArrayOf((p2.x-p1.x)*2,(p2.y-p1.y)*2),
        floatArrayOf(-mPrime, 1f)
    )
    val B = floatArrayOf(
        (generateRight(p2.x,p2.y,distances[anchor2])- generateRight(p1.x,p1.y,distances[anchor1])),
        (p1.y- mPrime * p1.x)
    )
    val X = multiplyMatrixVector(invertMatrix(A),B)

    if((actionSquare.size>=4 && isPointInSquare(Point(X[0],X[1]),actionSquare)) || actionSquare.size>=3 && isPointInTriangle(Point(X[0],X[1]),actionSquare[0],actionSquare[1],actionSquare[2]) ){
        return Point(X[0], X[1])
    }else{
        //val cosTheta2: Float = (distances[anchor2].pow(2)+distanceByAnchor.pow(2)-distances[anchor1].pow(2))/(2*distances[anchor2]*distanceByAnchor)
        //val tanTheta2: Float = sqrt(1-cosTheta2.pow(2))/cosTheta2
        val mPrime2 = (m-tanTheta)/(1+m*tanTheta)
        val A2 = arrayOf(
            floatArrayOf((p2.x-p1.x)*2,(p2.y-p1.y)*2),
            floatArrayOf(-mPrime2, 1f)
        )
        val B2 = floatArrayOf(
            (generateRight(p2.x,p2.y,distances[anchor2])- generateRight(p1.x,p1.y,distances[anchor1])),
            (p1.y- mPrime2 * p1.x)
        )
        val X2 = multiplyMatrixVector(invertMatrix(A2),B2)
        return Point(X2[0], X2[1])
    }
}

fun generateRight(x:Float, y:Float, d:Float, z: Float= 0f): Float{
    return x*x + y*y + z*z - d*d
}
/*
@Composable
fun CoordinatePlane(
    anchorList: List<Point>,
    pointsList: List<Point>,
    distanceList: List<Float>? = null,
    displayDistanceCircle: Boolean = false
) {
    val colors = listOf(
        Color.Magenta,
        Color.Green,
        Color.Blue,
        Color.DarkGray,
        Color.Cyan,
        Color.Gray,
        Color.LightGray
    )
    var max = 10f
    var min = -1f

    if(anchorList.isNotEmpty()){
        max = anchorList.maxOf { ceil(max(it.x, it.y)) }
        min = anchorList.minOf { floor(min(it.x, it.y)) } - 1f
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Set the height to match the width
            .padding(6.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.padding(5.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val localMax:Float = if(min<0) max-min else max
                val localMin:Float = if(min<0) 0f else min
                val localAxis:Float = if(min<0) -min else 0f
                val scale = width / (max - min)
                val originX = (localMin+localAxis) * scale
                val originY = (localMax-localAxis) * scale
                val step = (max-min).toInt()
                for(i in 0..step){
                    val x:Float = (localMin + i) * scale
                    val y:Float = (localMax - i) * scale
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(x, 0f),
                        end = Offset(x, width),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                drawLine( //X-axis
                    color = Color.Gray,
                    start = Offset(0f, originY),
                    end = Offset(width, originY),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine( //Y-axis
                    color = Color.Gray,
                    start = Offset(originX, 0f),
                    end = Offset(originX, width),
                    strokeWidth = 2.dp.toPx()
                )
                if(anchorList.isNotEmpty()) {
                    anchorList.forEach { point ->
                        val scaledX = (point.x) * scale
                        val scaledY = (point.y) * scale
                        drawCircle(
                            color = Color.Red,
                            radius = 3.dp.toPx(),
                            center = Offset(
                                x = scaledX + originX,
                                y = originY - scaledY
                            )
                        )
                        if (distanceList != null && distanceList.size <= anchorList.size && displayDistanceCircle) { // draw Circle
                            drawCircle(
                                color = Color.LightGray,
                                radius = (distanceList[anchorList.indexOf(point)] * scale),
                                center = Offset(
                                    x = scaledX + originX,
                                    y = originY - scaledY
                                ),
                                style = Stroke(width = 2.dp.toPx())
                            )


                        }

                    }

                    // Draw points with different colors for each list
                    pointsList.forEachIndexed { index, point ->
                        val color = colors.getOrElse(index) { Color.Black }

                        val scaledX = (point.x) * scale
                        val scaledY = (point.y) * scale
                        drawCircle(
                            color = color,
                            radius = 2.dp.toPx(),
                            center = Offset(
                                x = scaledX + originX,
                                y = originY - scaledY
                            )
                        )

                    }
                }
            }
        }
    }
}
*/

@Composable
fun CoordinatePlane(
    anchorList: List<Point>,
    newPoint: Point?,  // 실시간으로 들어오는 점 (nullable로 처리)
    distanceList: List<Float>? = null,
    togglePointHistory: Boolean = false,
    toggleDistanceCircle: Boolean = false,
    scale: Float = 1f,  // 줌 값 추가
    offsetX: Float = 0f,  // X축 오프셋 추가
    offsetY: Float = 0f   // Y축 오프셋 추가
) {
    val colors = listOf(
        Color.Magenta,
        Color.Green,
        Color.Blue,
        Color.DarkGray,
        Color.Cyan,
        Color.Gray,
        Color.LightGray
    )
    var max = 10f
    var min = -1f

    if (anchorList.isNotEmpty()) {
        max = anchorList.maxOf { ceil(max(it.x, it.y)) }
        min = anchorList.minOf { floor(min(it.x, it.y)) } - 1f
    }
    val maxPoints = 100
    val pointsList = remember { mutableStateListOf<Point>() }

    LaunchedEffect(newPoint) {
        newPoint?.let {
            if (pointsList.size >= maxPoints) {
                pointsList.removeAt(0)  // 오래된 점 제거
            }
            pointsList.add(it)  // 새로운 점 추가
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)  // Set the height to match the width
            .padding(6.dp),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        Box(modifier = Modifier.padding(5.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val localMax: Float = if (min < 0) max - min else max
                val localMin: Float = if (min < 0) 0f else min
                val localAxis: Float = if (min < 0) -min else 0f
                val baseScale = width / (max - min)
                val finalScale = baseScale * scale  // scale 적용
                val originX = (localMin + localAxis) * finalScale + offsetX  // offset 적용
                val originY = (localMax - localAxis) * finalScale + offsetY  // offset 적용
                val step = (max - min).toInt()

                for (i in 0..step) {
                    val x: Float = (localMin + i) * finalScale + offsetX
                    val y: Float = (localMax - i) * finalScale + offsetY
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(x, 0f),
                        end = Offset(x, width),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }


                drawLine( // draw X-axis
                    color = Color.Gray,
                    start = Offset(0f, originY),
                    end = Offset(width, originY),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine( // draw Y-axis
                    color = Color.Gray,
                    start = Offset(originX, 0f),
                    end = Offset(originX, width),
                    strokeWidth = 2.dp.toPx()
                )

                if (anchorList.isNotEmpty()) {
                    anchorList.forEach { point ->
                        val scaledX = point.x * finalScale + offsetX  // scale과 offset 적용
                        val scaledY = point.y * finalScale + offsetY  // scale과 offset 적용
                        drawCircle(
                            color = Color.Red,
                            radius = 3.dp.toPx(),
                            center = Offset(
                                x = scaledX + originX,
                                y = originY - scaledY
                            )
                        )

                        // Draw distance circles if needed
                        if (distanceList != null && distanceList.size <= anchorList.size && toggleDistanceCircle) {
                            drawCircle(
                                color = Color.LightGray,
                                radius = (distanceList[anchorList.indexOf(point)] * finalScale),
                                center = Offset(
                                    x = scaledX + originX,
                                    y = originY - scaledY
                                ),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                    if(newPoint != null) {
                        if (!togglePointHistory) {

                            val color = Color.Blue
                            val scaledX = newPoint.x * finalScale + offsetX  // scale과 offset 적용
                            val scaledY = newPoint.y * finalScale + offsetY  // scale과 offset 적용
                            drawCircle(
                                color = color,
                                radius = 2.dp.toPx(),
                                center = Offset(
                                    x = scaledX + originX,
                                    y = originY - scaledY
                                )
                            )
                        }

                        // Draw points(more older, more blurred)
                        pointsList.forEachIndexed { index, point ->
                            val alpha = 1f - (index.toFloat() / maxPoints.toFloat())  // 오래된 점일수록 투명도 증가
                            val scaledX = point.x * finalScale + offsetX  // scale과 offset 적용
                            val scaledY = point.y * finalScale + offsetY  // scale과 offset 적용
                            drawCircle(
                                color = Color.Blue.copy(alpha = alpha),
                                radius = 2.dp.toPx(),
                                center = Offset(
                                    x = scaledX + originX,
                                    y = originY - scaledY
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MySerialAppTheme() { // truthPoint: 2.5,1.75
        val anchors = listOf(Point(0f,0.58f), Point(6.44f,0f), Point(4.95f,2.32f), Point(1.16f,4.06f))
        //val distances = listOf(2.65f,4.19f,2.53f,3.05f)
        //val distances = listOf(2.9465f, 4.0272f, 2.8434f, 3.7243f)
        //val PDOAs = listOf(36.1468f, -52.0954f, -34.317f, 28.13633f)

        val distances = listOf(2.76f,4.31f,2.515f,2.67f)
        val PDOAs = listOf(-64.9f,66.05f,-76.903f,30.12f)

        val pointList = listOf(
            //listOf(Point(1f, 1f), Point(2f, 2f), Point(3f, 1f)),
            //listOf(Point(0f,1f), Point(1f,0f)),
            //listOf(Point(0.5f,1.33f), Point(1.67f,2.22f)),
            //listOf(calcMiddleBy4Side(distances,anchors),calcMiddleBy4SideAnotherVersion(distances,anchors),calcMiddleBy4SideAnotherVersion2(distances,anchors)),
            (calcMiddleBy4Side(distances,anchors)),

            //calcByDoubleAnchor2Distance(anchor1 = 0,anchor2 = 2, anchorPositions = anchors, distances = distances),
            (calcByDoubleAnchor(0,1,distances,anchors,anchors))


        )
        Column() {
            CoordinatePlane(
                anchorList = anchors,
                newPoint = null,
                distanceList = distances
            )

            pointList.forEach{
                Text(text = it.toString(), modifier = Modifier.padding(5.dp))
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun TwoAnchorPreview(){
    MySerialAppTheme() {
        val anchors = listOf(Point(3.6f,4.5f),Point(0f,4.5f))
        //val distances = listOf(3.418f, 3.27f)
        //val PDOAs = listOf( -34.573f, 30.528f)
        val distances = listOf(4.51f, 5.22f)
        val PDOAs = listOf(-16.42f,31.25f)
        val pointsList = listOf(
            (calcByDoubleAnchor(0,1,distances,anchors,anchors))
        )




        Column(){
            CoordinatePlane(
                anchorList = anchors,
                newPoint = null,
                distanceList = distances

            )
            pointsList.forEach{
                Text(text = it.toString())
            }
        }
    }
}