package com.example.myserialapp.ui.data

import android.icu.text.SimpleDateFormat
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myserialapp.positioningUtill.Point
import java.util.Date
import kotlin.math.pow
@Entity(tableName = "RangingData")
data class RangingData(
    @PrimaryKey(autoGenerate = false)
    val blockNum: Int = 0,
    val distanceList: List<RangingDistance> = emptyList(),
    var coordinates: Point = Point(),
    val time: String = SimpleDateFormat("dd HH:mm:ss").format(Date())
)
fun RangingData.toPoint():Point{
    return this.coordinates
}
data class RangingDistance(
    val id: Int,
    val distance : Float,
    val PDOA : Float? = null,
    val AOA : Float? = null
)
