package com.vivekroy.navcogknn

import android.bluetooth.le.ScanResult
import java.nio.ByteBuffer

@ExperimentalUnsignedTypes
class Beacon(private val result: ScanResult,
             val rssi : Int = result.rssi) {
    val major : String
    val minor : String

    init {
        val manufacturerData = result.scanRecord?.getManufacturerSpecificData(76)
        if (manufacturerData != null) {
            val majorMinor = ByteBuffer.wrap(manufacturerData, 18, 4).asShortBuffer()
            major = majorMinor[0].toUShort().toString()
            minor = majorMinor[1].toUShort().toString()
        } else {
            major = "-1"
            minor = "-1"
        }
    }

    constructor(beacon: Beacon, rssi: Int) : this(beacon.result, rssi)

    override fun toString(): String {
        return "{major: ${major}, minor: ${minor}, rssi: ${rssi}}"
    }
}