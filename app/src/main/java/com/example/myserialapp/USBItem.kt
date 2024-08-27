package com.example.myserialapp

import android.hardware.usb.UsbDevice
import com.hoho.android.usbserial.driver.*

data class USBItem(
    val device: UsbDevice,
    val port: UsbSerialPort,
    val driver: UsbSerialDriver = CdcAcmSerialDriver(device)
)
