package com.vivekroy.navcogknn

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.widget.TextView
import com.github.chrisbanes.photoview.PhotoView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.TimeUnit

data class BleScanner(val uuid: String) {

    private val scanSettings : ScanSettings
    private val scanFilter : ScanFilter
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bleScanner = bluetoothAdapter.bluetoothLeScanner
    private var subject : PublishSubject<ScanResult>? = null

    init {
        // Initialize scanSettings

        val scanSettingsBuilder = ScanSettings.Builder()
        scanSettingsBuilder.setReportDelay(0)
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        scanSettings = scanSettingsBuilder.build()
    }

    init {
        // initialize scanSettings

        val uuid = UUID.fromString(uuid)
        val bb = ByteBuffer.allocate(16)
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)
        val uuidBytes = bb.array()
        val manufacturerData = ByteBuffer.allocate(23)
        val manufacturerDataMask = ByteBuffer.allocate(23)
        manufacturerData.put(0, 2)  // 0x02
        manufacturerData.put(1, 21) // 0x15
        for (i in 2..17) {
            manufacturerData.put(i, uuidBytes[i - 2])
        }
        for (i in 0..17) {
            manufacturerDataMask.put(i, 1)
        }
        val scanFilterBuilder = ScanFilter.Builder()
        scanFilterBuilder.setManufacturerData(
            76,
            manufacturerData.array(),
            manufacturerDataMask.array()
        )
        scanFilter = scanFilterBuilder.build()
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result != null) {
                subject?.onNext(result)
            }
        }
    }

    @ExperimentalUnsignedTypes
    @SuppressLint("CheckResult")
    fun startScanning(knn : KNN, img : PhotoView, bitmap: Bitmap) {
        subject = PublishSubject.create<ScanResult>()
        bleScanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
        subject!!
            .observeOn(Schedulers.computation())
            .buffer(1, TimeUnit.SECONDS)
            .map { scanList ->
                knn.predict(
                    scanList
                        .map { scanResult ->
                            Beacon(scanResult) }
                        .groupBy { beacon -> "${beacon.major}_${beacon.minor}" }
                        .map { groupedBeacon ->
                            val meanRssi = groupedBeacon.value.map { it.rssi }.intMedian()
                            Beacon(groupedBeacon.value[0], meanRssi)
                        }.rssiVector()
                )
            }
            .map {
                val x = it[0]*25.0
                val y = it[1]*-25.0
                val cosT = 0.999708014 // cos pi/130
                val sinT = 0.0241637452 // sin pi/130
                listOf( x*cosT + y*sinT + 218.0, -x*sinT + y*cosT + 700.0 )
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val oldState = Matrix()
                img.attacher.getSuppMatrix(oldState)
                val bitmap = bitmap.copy(null, true)
                val canvas = Canvas(bitmap)
                val paint = Paint()
                paint.color = Color.RED
                canvas.drawCircle(it[0].toFloat(), it[1].toFloat(), 7.5f, paint)
                img.setImageBitmap(bitmap)
                img.attacher.setDisplayMatrix(oldState)
            }
    }

    fun stopScanning() {
        subject?.onComplete()
        bleScanner.stopScan(scanCallback)
    }
}