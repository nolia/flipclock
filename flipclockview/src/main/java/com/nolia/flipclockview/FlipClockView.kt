package com.nolia.flipclockview

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import java.util.*

private const val FLAG_SS = 0b1
private const val FLAG_MM = 0b10
private const val FLAG_HH = 0b100
private const val STYLE_HH_MM_SS = FLAG_HH or FLAG_MM or FLAG_SS

class FlipClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr),
    ResourceUtils {

    var flipClockType: Int = STYLE_HH_MM_SS
        set(value) {
            field = value

            val hoursVisible = value and FLAG_HH != 0
            val minutesVisible = value and FLAG_MM != 0
            val secondsVisible = value and FLAG_SS != 0

            hourView.visibility = if (hoursVisible) View.VISIBLE else View.GONE

            dividerHoursMinutes.visibility = if (hoursVisible && minutesVisible) View.VISIBLE else View.GONE
            minuteView.visibility = if (minutesVisible) View.VISIBLE else View.GONE

            dividerMinutesSeconds.visibility = if (minutesVisible && secondsVisible) View.VISIBLE else View.GONE
            secondView.visibility = if (secondsVisible) View.VISIBLE else View.GONE
        }


    private val hourView: FlipTextView
    private val minuteView: FlipTextView
    private val secondView: FlipTextView
    private var dividerHoursMinutes: TextView
    private var dividerMinutesSeconds: TextView

    private var isTicking: Boolean = false

    override val res: Resources
        get() = resources

    init {
        orientation = HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.merge_flip_clock, this, true)

        hourView = findViewById(R.id.flipHourView)
        minuteView = findViewById(R.id.flipMinuteView)
        secondView = findViewById(R.id.flipSecondView)
        dividerHoursMinutes = findViewById(R.id.dividerHM)
        dividerMinutesSeconds = findViewById(R.id.dividerMS)

        parseAttributes(attrs, defStyleAttr)
    }

    private fun parseAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs == null) {
            return
        }

        parseFlipTextAttrs(attrs, defStyleAttr)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.FlipClockView,
            defStyleAttr,
            0
        ).apply {
            try {
                flipClockType = getInt(R.styleable.FlipClockView_flip_type, STYLE_HH_MM_SS)

            } finally {
                recycle()
            }
        }
    }

    private fun parseFlipTextAttrs(attrs: AttributeSet?, defStyleAttr: Int) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.FlipTextView,
            defStyleAttr,
            0
        ).apply {
            try {
                val textSize = getDimension(R.styleable.FlipTextView_flip_textSize, 30.sp)
                val textColor = getColor(R.styleable.FlipTextView_flip_textColor, Color.GRAY)
                val animationDuration = getInt(R.styleable.FlipTextView_flip_animationDuration, 200).toLong()
                val fontId = getResourceId(R.styleable.FlipTextView_flip_textFont, -1)
                var typeface: Typeface? = null
                if (fontId != -1) {
                    typeface = ResourcesCompat.getFont(context, fontId)
                }

                for (v in listOf(hourView, minuteView, secondView)) {
                    v.textSize = textSize
                    v.textColor = textColor
                    v.animationDuration = animationDuration

                    if (typeface != null) v.typeface = typeface
                }
                for (dividerView in listOf(dividerHoursMinutes, dividerMinutesSeconds)) {
                    dividerView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                    dividerView.setTextColor(textColor)

                    if (typeface != null) dividerView.typeface = typeface
                }

            } finally {
                recycle()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isTicking = true
        tick()
    }

    private fun tick() {
        if (!isTicking) {
            return
        }
        val now = Calendar.getInstance()

        val hourText = "%02d".format(now.get(Calendar.HOUR_OF_DAY))
        val minuteText = "%02d".format(now.get(Calendar.MINUTE))
        val secondText = "%02d".format(now.get(Calendar.SECOND))

        updateText(hourView, hourText)
        updateText(minuteView, minuteText)
        updateText(secondView, secondText)

        postDelayed({ tick() }, 1000L)
    }

    private fun updateText(flipTextView: FlipTextView, text: String) {
        if (flipTextView.text != text) flipTextView.text = text
    }

    override fun onDetachedFromWindow() {
        isTicking = false
        super.onDetachedFromWindow()
    }
}
