package com.example.myserialapp.positioningUtill

data class Anchor(
    var id : Int = 0,
    var coordinateX: Float = 0f,
    var coordinateY: Float = 0f,
    var name : String = ""
)
fun Anchor.getPoint(): Point {
    return Point(this.coordinateX,this.coordinateY)
}