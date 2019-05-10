package com.nolia.flipclockview

import android.content.res.Resources
import android.util.TypedValue

internal interface ResourceHelper {

    val res: Resources

    val Number.dp: Float get() = toPx()
    val Number.sp: Float get() = toPx(units = TypedValue.COMPLEX_UNIT_SP)

    fun Number.toPx(units: Int = TypedValue.COMPLEX_UNIT_DIP): Float = TypedValue.applyDimension(
        units,
        this.toFloat(),
        res.displayMetrics
    )

}
