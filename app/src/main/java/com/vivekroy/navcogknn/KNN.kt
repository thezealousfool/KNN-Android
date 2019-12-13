package com.vivekroy.navcogknn

import android.util.Log
import java.lang.Math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class KNN (val data : MutableMap<List<Int>, List<Double>>) {

    companion object {
        fun getDistance(oneHot1 : List<Int>, oneHot2: List<Int>) : Double =
            sqrt(oneHot1.zip(oneHot2) {y2,y1 ->
                (abs(y2-y1)).toDouble().pow(2)
            }.sum())
    }

    fun predict(oneHot: List<Int>, k : Int = 5) : List<Double> {
        val topK = data.map {
            listOf(getDistance(oneHot, it.key), it.value[0], it.value[1])
        }.sortedBy { it[0] }.take(k).toList()
        val sumDistTmp = topK.map { it[0] }.sum()
        val sumDist = if (sumDistTmp > 0) sumDistTmp else 1.0
        val topKCorrected = if (sumDistTmp > 0) topK else topK.map { listOf(1.0, it[1], it[2]) }
        return topKCorrected.map {
            listOf(it[1]*it[0]/sumDist, it[2]*it[0]/sumDist)
        }.reduce { sum, element ->
            listOf(sum[0]+element[0], sum[1]+element[1])
        }
    }
}

fun List<Beacon>.rssiVector() : List<Int> {
    val order = listOf(1,2,3,4,5,6,7,8,11,9,87,12,10,13,14,15,
                        16,17,18,19,20,21,41,42,43,44,45,46,47,
                        48,49,50,51,52,53,54,188,190,191,194,
                        195,196,199,200,201,202,253,89)
    val oneHotRep = IntArray(order.size)
    this.forEach { beacon ->
        val idx = order.indexOf(beacon.minor.toInt())
        if (beacon.major == "65535" && idx != -1)
            oneHotRep[idx] = -beacon.rssi
    }
    return oneHotRep.toList()
}