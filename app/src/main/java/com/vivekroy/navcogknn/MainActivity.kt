package com.vivekroy.navcogknn

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.chrisbanes.photoview.PhotoView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private val knnData = mutableMapOf<List<Int>, List<Double>>()
    private lateinit var knn: KNN
    private val bleScanner = BleScanner("F7826DA6-4FA2-4E98-8024-BC5B71E0893E")

    private fun loadKnnData(){
        resources.openRawResource(R.raw.knn).bufferedReader().lineSequence().forEach { line ->
            val keyVal = line.split(':')
            val key = keyVal[0].split(',').dropLast(1).map { it.toInt() }
            val value = keyVal[1].split(',').map { it.toDouble() }
            knnData[key] = value
        }
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadKnnData()
        knn = KNN(knnData)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),0)
    }

    override fun onResume() {
        super.onResume()
        bleScanner.startScanning(knn, findViewById<PhotoView>(R.id.map), (resources.getDrawable(R.drawable.nsh2ndf) as BitmapDrawable).bitmap)
    }

    override fun onPause() {
        super.onPause()
        bleScanner.stopScanning()
    }
}
