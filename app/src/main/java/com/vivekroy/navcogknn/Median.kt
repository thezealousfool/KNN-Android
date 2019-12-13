package com.vivekroy.navcogknn

import kotlin.math.roundToInt

fun List<Int>.intMedian() : Int {
    return if (this.size % 2 == 0)
        ((this[this.size/2] + this[this.size/2 - 1]) / 2.0).roundToInt()
    else this[this.size/2]
}