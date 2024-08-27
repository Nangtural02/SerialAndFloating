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
    resultVectorList.forEach{it ->
        if(isPointInSquare(Point(it[0],it[1]),anchorPosition)){
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
fun calcByDoubleAnchor2AOA(anchor1:Int, anchor2:Int, anchorPositions: List<Point>, lastZ: Float, angles: List<Float>): Point{
    val p1 = anchorPositions[anchor1]; val p2 = anchorPositions[anchor2]
    val distanceByAnchor:Float = p1.getDistance(p2)

    val mA = tan(Math.toRadians(angles[anchor1].toDouble()).toFloat())
    val mB = tan(Math.toRadians(-angles[anchor2].toDouble()).toFloat())
    // 관측자의 위치 계산
    // 두 점을 잇는 선을 기준으로 한 삼각형의 내각 계산
    val thetaC = Math.PI - mA - mB

    // 관측자와 점 A 사이의 거리 (Law of sines)
    val distanceToA = distanceByAnchor * sin(mB) / sin(thetaC)
    // 관측자와 점 B 사이의 거리
    val distanceToB = distanceByAnchor * sin(mA) / sin(thetaC)

    // 점 A를 기준으로 관측자의 위치 계산
    val deltaX = distanceToA * cos(Math.PI - mA)
    val deltaY = distanceToA * sin(Math.PI - mA)

    val xC = p1.x + deltaX
    val yC = p1.y + deltaY

    return Point(xC.toFloat(), yC.toFloat())

}
fun degreesToRadians(degrees:Float):Float{
    return degrees * (PI / 180).toFloat()
}

 */
/*
fun calcByDoubleAnchor2Angle(anchor1:Int, anchor2:Int, anchorPositions: List<Point>, lastZ: Float, angles: List<Float>, distances: List<Float>): Point{

    val p1 = anchorPositions[anchor1]; val p2 = anchorPositions[anchor2]
    val distanceByAnchor:Float = p1.getDistance(p2)
    val angle1 = (angles[anchor1])
    val angle2 = (angles[anchor2])

    val d1 = distanceByAnchor * (sin(degreesToRadians(90-abs(angle2)))) / (sin(degreesToRadians(abs(angle1)+abs(angle2))))
    val d2 = distanceByAnchor * (sin(degreesToRadians(90-abs(angle1)))) / (sin(degreesToRadians(abs(angle1)+abs(angle2))))

    val m : Float = //if(p2.y == p1.y)null
    //else
        -(p1.x-p2.x)/(p1.y-p2.y)
    val mPrime : Float =
        //if(m == null)
            tan(degreesToRadians(90-abs(angle1)))
        //else (m-tan(degreesToRadians(angle1)))/(1+m*tan(degreesToRadians(angle1)))
        //else  (1 - m * tan(degreesToRadians(angle1)))/(m + tan(degreesToRadians(angle1)))
    val A = arrayOf(
        floatArrayOf((p2.x-p1.x)*2,(p2.y-p1.y)*2),
        floatArrayOf(-mPrime, 1f)
    )
    val B = floatArrayOf(
        (generateRight(p2.x,p2.y,d2)- generateRight(p1.x,p1.y,d1)),
        (p1.y- mPrime * p1.x)
    )
    val X = multiplyMatrixVector(invertMatrix(A),B)
    return Point(X[0],X[1])

}

 */
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
}
fun calcByDoubleAnchor(anchor1:Int, anchor2: Int, anchorPositions: List<Point>, distances: List<Float>): Point {
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
    return Point(X[0],X[1])
}

fun generateRight(x:Float, y:Float, d:Float, z: Float= 0f): Float{
    return x*x + y*y + z*z - d*d
}

@Composable
fun CoordinatePlane(
    anchorList: List<Point>,
    pointsList: List<List<Point>>,
    distanceList: List<Float>? = null
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
    val max= anchorList.maxOf{ ceil(max(it.x,it.y)) }
    val min= anchorList.minOf{ floor(min(it.x,it.y)) } -1f
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

                anchorList.forEach{ point ->
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
                    if(distanceList?.size == anchorList.size){ // draw Circle
                        drawCircle(
                            color = Color.LightGray,
                            radius = (distanceList[anchorList.indexOf(point)]*scale),
                            center = Offset(
                                x = scaledX + originX,
                                y = originY - scaledY
                            ),
                            style = Stroke(width = 2.dp.toPx())
                        )


                    }

                }
                // Draw points with different colors for each list
                pointsList.forEachIndexed { index, points ->
                    val color = colors.getOrElse(index) { Color.Black }
                    points.forEach { point ->
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MySerialAppTheme { // truthPoint: 2.5,1.75
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
            listOf(calcMiddleBy4Side(distances,anchors)),
            calcBy4Side(distances,anchors),
            //calcByDoubleAnchor2Distance(anchor1 = 0,anchor2 = 2, anchorPositions = anchors, distances = distances),
            listOf(calcByDoubleAnchor(0,1,anchors,distances))


        )
        Column() {
            CoordinatePlane(
                anchorList = anchors,
                pointsList = pointList,
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
    MySerialAppTheme {
        val anchors = listOf(Point(3.6f,4.5f),Point(0f,4.5f))
        //val distances = listOf(3.418f, 3.27f)
        //val PDOAs = listOf( -34.573f, 30.528f)
        val distances = listOf(4.51f, 5.22f)
        val PDOAs = listOf(-16.42f,31.25f)
        val pointsList = listOf(
            listOf(calcByDoubleAnchor(0,1,anchors,distances))
        )




        Column(){
            CoordinatePlane(
                anchorList = anchors,
                pointsList = pointsList,
                distanceList = distances

            )
            pointsList.forEach{
                Text(text = it.toString())
            }
        }
    }
}


/*
fun calcBy4SideWith3LeastSquareSolution(distances: List<Float>, anchorPosition: List<Point>): List<Point>{
    var results = emptyList<Point>()
    for (i in distances.indices) {
        results = results.plus(
            calcBy3SideWithLeastSquaresSolution(
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
}*/
/*
fun calcBy3SideWithLeastSquaresSolution(distances: List<Float>, anchorPosition: List<Point>): Point {
    if(distances.size < 3) return Point(-66.66f,-66.66f,-66.66f)
    val i = 0
    val j = (i + 1) % 3
    val k = (i + 2) % 3
    val x1 = anchorPosition[i].x
    val y1 = anchorPosition[i].y
    val d1 = distances[i]
    val x2 = anchorPosition[j].x
    val y2 = anchorPosition[j].y
    val d2 = distances[j]
    val x3 = anchorPosition[k].x
    val y3 = anchorPosition[k].y
    val d3 = distances[k]
    val A = arrayOf(
        floatArrayOf(2 * (x2 - x1), 2 * (y2 - y1)),
        floatArrayOf(2 * (x3 - x1), 2 * (y3 - y1))
    )
    val A_T = transitionMatrix(A)
    val B = floatArrayOf(
        d1 * d1 - d2 * d2 + x2 * x2 - x1 * x1 + y2 * y2 - y1 * y1,
        d1 * d1 - d3 * d3 + x3 * x3 - x1 * x1 + y3 * y3 - y1 * y1
    )
    val inv = invertMatrix(multiplyMatrixMatrix(A_T,A))
    val result = multiplyMatrixVector(multiplyMatrixMatrix(inv, A_T), B)
    return Point(result[0], result[1])
}

fun calcBy3Angle(): Point {

    return Point()
}*/

/* 이건 그냥 틀렸음. 모든 노드와 z에 의한 오차 k 가 같을 리가 없음. z만 동일하지
fun calcBy4SideWithErrorAndLSS(distances: List<Float>, anchorPosition: List<Point>): Point{
    if(distances.size<4)
        return Point(-66.66f,-66.66f,-66.66f)

    val xs = anchorPosition.map{it.x}
    val ys = anchorPosition.map{it.y}
    val zs = anchorPosition.map{it.z}

    val a = arrayOf(
        floatArrayOf(xs[0]-xs[2],ys[0]-ys[2],distances[2]-distances[0]),
        floatArrayOf(xs[1]-xs[2],ys[1]-ys[2],distances[2]-distances[1]),
        floatArrayOf(xs[3]-xs[2],ys[3]-ys[2],distances[2]-distances[3])
    )
    val a_t = transitionMatrix(a)
    val b = floatArrayOf(
        (generateRight(xs[0],ys[0],null,distances[0])-generateRight(xs[2],ys[2],null,distances[2]))/2,
        (generateRight(xs[1],ys[1],null,distances[1])-generateRight(xs[2],ys[2],null,distances[2]))/2,
        (generateRight(xs[3],ys[3],null,distances[3])-generateRight(xs[2],ys[2],null,distances[2]))/2
    )
    val inv = invertMatrix(multiplyMatrixMatrix(a_t,a))
    val result = multiplyMatrixVector(multiplyMatrixMatrix(inv,a_t),b)
    val k = abs(result[2])
    Log.d("asdf","k=$k")
    val z = sqrt(k * (k + 2 * distances[0]))
    return Point(result[0],result[1],z)
}*/


/*
fun calcMiddleBy4SideAnotherVersion(distances: List<Float>, anchorPosition: List<Point>): Point {
    val x = anchorPosition.map{it.x}
    val y = anchorPosition.map{it.y}
    val z = anchorPosition.map{it.z}
    val d = distances.map{it}
    val A2 = arrayOf(
        floatArrayOf(2 * (x[1] - x[2]), 2 * (y[1] - y[2])),
        floatArrayOf(2 * (x[3] - x[0]), 2 * (y[3] - y[0]))
    )
    val B2 = floatArrayOf(
        generateRight(x[1],y[1],d[1]) - generateRight(x[2],y[2],d[2]),
        generateRight(x[3],y[3],d[3]) - generateRight(x[0],y[0],d[0])
    )


    val A_inv = invertMatrix(A2)
    val result = multiplyMatrixVector(A_inv,B2)

    val meanPoint = Point(result[0],result[1])
    val meanArray = FloatArray(4)
    val zArray:MutableList<Float> = mutableListOf()
    for(i in 0..3){
        meanArray[i] = sqrt((meanPoint.x-anchorPosition[i].x).pow(2)+(meanPoint.y-anchorPosition[i].y).pow(2))
    }
    for(i in 0..3){
        if(distances[i].pow(2) >= meanArray[i].pow(2))
            zArray.add(sqrt(distances[i].pow(2) - meanArray[i].pow(2)))
    }
    return Point(result[0],result[1],zArray.average().toFloat())
}

fun calcMiddleBy4SideAnotherVersion2(distances: List<Float>, anchorPosition: List<Point>): Point {
    val x = anchorPosition.map{it.x}
    val y = anchorPosition.map{it.y}
    val z = anchorPosition.map{it.z}
    val d = distances.map{it}



    val A_inv = invertMatrix(A)
    val result = multiplyMatrixVector(A_inv,B)

    val meanPoint = Point(result[0],result[1])
    val meanArray = FloatArray(4)
    val zArray:MutableList<Float> = mutableListOf()
    for(i in 0..3){
        meanArray[i] = sqrt((meanPoint.x-anchorPosition[i].x).pow(2)+(meanPoint.y-anchorPosition[i].y).pow(2))
    }
    for(i in 0..3){
        if(distances[i].pow(2) >= meanArray[i].pow(2))
            zArray.add(sqrt(distances[i].pow(2) - meanArray[i].pow(2)))
    }
    return Point(result[0],result[1],zArray.average().toFloat())
}
*/