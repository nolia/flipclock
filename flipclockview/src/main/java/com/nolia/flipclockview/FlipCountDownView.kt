package com.nolia.flipclockview

import android.content.Context
import android.util.AttributeSet
import java.util.*

class FlipCountDownView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defAttrStyle: Int = 0
) : FlipClockView(context, attributeSet, defAttrStyle) {

    var deadlineTime: Long = 0L
        set(value) {
            field = value
            invalidate()
        }

    override fun onUpdateText() {
        val time = System.currentTimeMillis()
        var diff = deadlineTime - time
        if (diff < 0) diff = 0L

        val now = Calendar.getInstance()
        now.time = Date(diff)

        val hourText = "%02d".format(now.get(Calendar.HOUR_OF_DAY))
        val minuteText = "%02d".format(now.get(Calendar.MINUTE))
        val secondText = "%02d".format(now.get(Calendar.SECOND))

        updateText(hourView, hourText)
        updateText(minuteView, minuteText)
        updateText(secondView, secondText)
    }

}
