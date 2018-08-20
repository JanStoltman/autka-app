package com.yggdralisk.autkaapp.common.anim

import android.util.Property
import android.view.View
import kotlin.math.roundToInt


internal class HeightProperty : Property<View, Float>(Float::class.java, "height") {
    override operator fun get(view: View): Float? {
        return view.height.toFloat()
    }

    override operator fun set(view: View, value: Float?) {
        value?.let {
            view.layoutParams.height = value.roundToInt()
        }
        view.layoutParams = view.layoutParams
    }
}