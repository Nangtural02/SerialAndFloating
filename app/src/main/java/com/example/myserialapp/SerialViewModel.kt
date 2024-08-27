package com.example.myserialapp

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myserialapp.positioningUtill.Point
import com.example.myserialapp.positioningUtill.calcBy4Side
import com.example.myserialapp.positioningUtill.calcByDoubleAnchor
import com.example.myserialapp.positioningUtill.calcMiddleBy4Side
import com.example.myserialapp.ui.data.RangingData
import com.example.myserialapp.ui.data.RangingDistance
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.hoho.android.usbserial.BuildConfig
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date


class SerialViewModel(application: Application): AndroidViewModel(application), SerialInputOutputManager.Listener {
    var connected : MutableStateFlow<Boolean> = MutableStateFlow(false)
    val foldername: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/SerialLog" //Save Path
    var rawfilename: String = "temp.txt"
    var filename: String = "temp.csv"

    var connectedUSBItem = MutableStateFlow<USBItem?>(null)
    //val anchorList = listOf(Point(0f,0.58f), Point(6.44f,0f), Point(4.95f,2.32f), Point(1.16f,4.06f))
    val anchorList = listOf(Point(0f,0f), Point(15f,0f), Point(30f,0f), Point(45f,0f))
    private val INTENT_ACTION_GRANT_USB: String = BuildConfig.LIBRARY_PACKAGE_NAME + ".GRANT_USB"
    private var usbIOManager: SerialInputOutputManager? = null
    private val usbPermissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (INTENT_ACTION_GRANT_USB == intent.action) {
                usbPermission = if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    USBPermission.Granted
                } else {
                    USBPermission.Denied
                }
                connectSerialDevice(context)
            }
        }
    }
    init {
        val filter = IntentFilter(INTENT_ACTION_GRANT_USB)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 이상일 경우, 명시적으로 플래그를 지정
            getApplication<Application>().registerReceiver(usbPermissionReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            // Android 12 미만일 경우, 기존 방식으로 등록
            getApplication<Application>().registerReceiver(usbPermissionReceiver, filter)
        }
    }

    fun connectSerialDevice(context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            Log.d("asdf", "${connectedUSBItem.value == null}")
            while (connectedUSBItem.value == null) {
                Log.d("button", "try to Connect")
                for (device in usbManager.deviceList.values) {
                    val driver = CdcAcmSerialDriver(device)
                    if (driver.ports.size == 1) {
                        connectedUSBItem.update {
                            USBItem(device, driver.ports[0], driver)
                        }
                        Log.d("asdf", "device Connected")
                    }
                }
                delay(1000L) //by 1 sec
            }
            val device: UsbDevice = connectedUSBItem.value!!.device

            Log.d("asdf", "usb connection try")
            var usbConnection: UsbDeviceConnection? = null
            if (usbPermission == USBPermission.UnKnown && !usbManager.hasPermission(device)) {
                usbPermission = USBPermission.Requested
                val intent: Intent = Intent(INTENT_ACTION_GRANT_USB)
                intent.setPackage(getApplication<Application>().packageName)
                Log.d("asdf", "request Permission")
                usbManager.requestPermission(
                    device,
                    PendingIntent.getBroadcast(
                        getApplication(),
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
                return@launch
            }
            delay(1000L)

            rawfilename = "rawSerial_" + SimpleDateFormat("yyyy_MM_dd HH_mm_ss").format(Date()) + ".txt"
            filename = SimpleDateFormat("yyyy_MM_dd HH_mm_ss").format(Date()) + ".csv"
            try {
                Log.d("asdf", "Port open try")
                usbConnection = usbManager.openDevice(device)
                connectedUSBItem.value!!.port.open(usbConnection)
            }catch(e:IllegalArgumentException){
                connectedUSBItem.update{null}
                return@launch
            }
            Log.d("asdf", "Port open")
            connectedUSBItem.value!!.port.setParameters(baudRate, 8, 1, UsbSerialPort.PARITY_NONE)
            usbIOManager = SerialInputOutputManager(connectedUSBItem.value!!.port, this@SerialViewModel)

            usbIOManager!!.start()
            connected.value = true
            Log.d("qwrwe","dtr On")
            connectedUSBItem.value?.port?.dtr = true
        }
    }
    fun disConnectSerialDevice(){
        connected.value = false
        usbIOManager!!.listener = null
        usbIOManager!!.stop()
        connectedUSBItem.value?.port?.close()
        connectedUSBItem.update{ null }
        usbPermission = USBPermission.UnKnown
    }



    private var _buffer = mutableStateOf("")
    var lineTexts by mutableStateOf("")
        private set

    var nowRangingData : MutableState<RangingData> = mutableStateOf(RangingData())
    var lastCoordinateBlock = mutableIntStateOf(0)
    var calculated4_3Result: List<Point> = mutableListOf()
    var calculated4_4Result: List<Point> = mutableListOf()
    var calculated2_2AngleResult:MutableList<Point> = mutableListOf()
    var calculated2_2Result:MutableList<Point> = mutableListOf()

    fun blockHandler(blockString: String){
        val gson = Gson()
        viewModelScope.launch{
            writeTextFile(blockString + "\n")
        }
        try {
            val data = gson.fromJson(blockString, Data::class.java)
            Log.d("zvcx", "1Block: ${data.block}")

            var distanceList = emptyList<RangingDistance>()
            data.results.forEach { result ->
                distanceList = distanceList.plus(
                    RangingDistance(
                        id = if(result.addr == "Err") -1 else result.addr.substring(3).toInt(),
                        distance = result.dCm.toFloat() / 100,
                        PDOA = if (result.lPDoADeg != 0.0f) result.lPDoADeg else null,
                        AOA = if (result.lAoADeg != 0.0f) result.lAoADeg else null
                    )
                )
            }
            val blockData = RangingData(
                blockNum = data.block,
                distanceList = distanceList,
                time = SimpleDateFormat("dd HH:mm:ss").format(
                    Date()
                )
            )
            var tempErrFlag: Boolean = false
            for (result in data.results) {
                if (result.status != "Ok") {
                    //Log.d("Err", result.addr)
                    tempErrFlag = true
                    break
                }
            }
            if(!tempErrFlag) {
                lastCoordinateBlock.intValue = data.block

                val distances = blockData.distanceList.map{it.distance}
                var results:List<Point> = emptyList()


                results = listOf(calcMiddleBy4Side(distances, anchorList))
                //results += calcBy4SideWithError(distances,anchorList)
                //results += calcAllBy4SideWithError(distances,anchorList)
                blockData.coordinates = results
                calculated4_4Result += listOf(calcMiddleBy4Side(distances,anchorList))


                calculated4_3Result += calcBy4Side(distances,anchorList)

                calculated2_2Result.add(calcByDoubleAnchor(0,1,anchorList,distances))
                calculated2_2Result.add(calcByDoubleAnchor(0,2,anchorList,distances))
                calculated2_2Result.add(calcByDoubleAnchor(0,3,anchorList,distances))
                calculated2_2Result.add(calcByDoubleAnchor(1,2,anchorList,distances))
                calculated2_2Result.add(calcByDoubleAnchor(1,3,anchorList,distances))
                calculated2_2Result.add(calcByDoubleAnchor(2,3,anchorList,distances))

            }
            nowRangingData.value = blockData
            viewModelScope.launch{
                writeCSVFile(blockData)
            }

        }catch(e: JsonSyntaxException ){
            Log.d("error", "signal error")
        }catch(e: NullPointerException){
            Log.e("error", "nullPointer -")
        }
    }



    private enum class USBPermission {UnKnown, Requested, Granted, Denied}
    private val READ_WAIT_MILLIS: Int = 2000
    var baudRate = 115200
    private var usbPermission: USBPermission = USBPermission.UnKnown


    override fun onNewData(data: ByteArray?) {
        viewModelScope.launch{
            receive(data)
        }
    }
    override fun onRunError(e: Exception) {
        viewModelScope.launch() {
            Log.d("SerialDevice", "Disconnected")
            disConnectSerialDevice()
        }
    }

    private fun send(str: String){

    }
    private fun receive(data: ByteArray?){
        //Log.d("good", "receive data")
        if(data != null) {
            if (data.isNotEmpty()) {
                //Log.d("condition", "buffer.value.isNotEmpty():${_buffer.value.isNotEmpty()}")
                val result : String = getLineString(data)
                //Log.d("fdsareqw",result)
                if (_buffer.value.isEmpty()) {
                    _buffer.value += result
                }else{
                    if(result.length >=3 && result.substring(0,3) == "{\"B"){ //메시지를 받다말고 새로운 메시지가 들어옴
                        //appendLineText("Error")
                        _buffer.value = result
                    }else if(result.length >=3 && result.substring(result.length - 3).equals("}  ")){ //메시지의 끝
                        _buffer.value += result.substring(0,result.length-2)
                        //Log.d("qwre",_buffer.value)
                        blockHandler(_buffer.value)
                        _buffer.value = ""
                    }else{
                        _buffer.value += result
                    }
                }
            }
        }
    }


    private fun getLineString(array: ByteArray): String {
        return getLineString(array, 0, array.size)
    }

    private fun getLineString(array: ByteArray, offset: Int, length: Int): String {
        val result = StringBuilder()

        val line = ByteArray(8)
        var lineIndex = 0
        var resultString = ""
        for (i in offset until offset + length) {
            if (lineIndex == line.size) {
                for (j in line.indices) {
                    if (line[j] > ' '.code.toByte() && line[j] < '~'.code.toByte()) {
                        result.append(String(line, j, 1))
                    } else {
                        result.append(" ")
                    }
                }
                lineIndex = 0
            }
            val b = array[i]
            line[lineIndex++] = b
        }
        for (i in 0 until lineIndex) {
            if (line[i] > ' '.code.toByte() && line[i] < '~'.code.toByte()) {
                result.append(String(line, i, 1))
            } else {
                result.append(" ")
            }
        }
        resultString = result.toString()

        return resultString
    }
    private suspend fun writeTextFile(contents: String) {
        withContext(Dispatchers.IO) {
            try {
                val dir: File = File(foldername)
                if (!dir.exists()) {
                    dir.mkdir()
                }
                val fos = FileOutputStream(foldername + "/" + rawfilename, true)
                fos.write(contents.encodeToByteArray())
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    private suspend fun writeCSVFile(data:RangingData) {
        withContext(Dispatchers.IO) {
            try {
                var contents = ""
                contents += "${data.blockNum},"
                data.distanceList.forEach{ rangingDistance ->
                    contents +="${rangingDistance.id},${rangingDistance.distance},${rangingDistance.PDOA},${rangingDistance.AOA},"
                }
                data.coordinates.forEach{ point ->
                    contents +="/,${"%.2f".format(point.x)},${"%.2f".format(point.y)},${"%.2f".format(point.z)},"

                }
                contents += "\n"


                val dir: File = File(foldername)
                if (!dir.exists()) {
                    dir.mkdir()
                }
                val fos = FileOutputStream(foldername + "/" + filename, true)
                fos.write(contents.encodeToByteArray())
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

data class Result(
    @SerializedName("Addr") val addr: String,
    @SerializedName("Status") val status: String,
    @SerializedName("D_cm") val dCm: Int,
    @SerializedName("LPDoA_deg") val lPDoADeg: Float,
    @SerializedName("LAoA_deg") val lAoADeg: Float,
    @SerializedName("LFoM") val lfom: Int,
    @SerializedName("RAoA_deg") val raDoADeg: Float,
    @SerializedName("CFO_100ppm") val cfo100ppm: Int
)

data class Data(
    @SerializedName("Block") val block: Int,
    @SerializedName("results") val results: List<Result>
)