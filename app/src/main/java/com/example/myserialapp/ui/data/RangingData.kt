package com.example.myserialapp.ui.data

import android.icu.text.SimpleDateFormat
import androidx.room.PrimaryKey
import com.example.myserialapp.positioningUtill.Point
import java.util.Date
import kotlin.math.pow

/*
@Entity(tableName = "RangingData")*/
data class RangingData(
    @PrimaryKey(autoGenerate = false)
    val blockNum: Int = 0,
    val distanceList: List<RangingDistance> = emptyList(),
    var coordinates: List<Point> = emptyList(),
    val time: String = SimpleDateFormat("dd HH:mm:ss").format(Date())
)

data class RangingDistance(
    val id: Int,
    val distance : Float,
    val PDOA : Float? = null,
    val AOA : Float? = null
)
/*
class RangingDataConverters{
    @TypeConverter
    fun fromList(distanceList: List<RangingDistance>): String{
        val gson= Gson()
        return gson.toJson(distanceList)
    }

    @TypeConverter
    fun toList(distanceListString: String): List<RangingDistance> {
        val gson = Gson()
        val type = object : TypeToken<List<RangingDistance>>() {}.type
        return gson.fromJson(distanceListString,type)
    }
}*/

fun RangingData.calc2Location(AnchorPosition: List<Point>){
    val A = 2* (AnchorPosition[1].x-AnchorPosition[0].x)
    val B = 2* (AnchorPosition[1].y-AnchorPosition[0].y)
    val C = distanceList[0].distance.pow(2) - distanceList[1].distance.pow(2) - AnchorPosition[0].x.pow(2)+AnchorPosition[1].x.pow(2) - AnchorPosition[0].y.pow(2) + AnchorPosition[1].y.pow(2)
    val D = 2* (AnchorPosition[2].x -AnchorPosition[1].x)
    val E = 2* (AnchorPosition[2].y -AnchorPosition[1].y)
    val F = distanceList[1].distance.pow(2) - distanceList[2].distance.pow(2) - AnchorPosition[1].x.pow(2)+AnchorPosition[2].x.pow(2) - AnchorPosition[1].y.pow(2) + AnchorPosition[2].y.pow(2)

    //coordinateX = ( (F * B) - (E * C) ) / ( (B * D) - (E * A))
    //coordinateY = ( (F * A) - (D * C) ) / ( (A * E) - (D * B))
}