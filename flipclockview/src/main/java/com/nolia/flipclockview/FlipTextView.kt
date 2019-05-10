package com.nolia.flipclockview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Keep
import androidx.core.content.res.ResourcesCompat
import kotlin.math.roundToInt

class FlipTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr),
    ResourceUtils {

    private val camera = Camera()
    private val cameraMatrix = Matrix()

    override val res: Resources
        get() = resources

    var textColor: Int
        get() = textPaint.color
        set(value) {
            textPaint.color = value
            invalidate()
        }

    var textSize: Float
        get() = textPaint.textSize
        set(value) {
            textPaint.textSize = value
            invalidate()
        }

    var typeface: Typeface
        get() = textPaint.typeface
        set(value) {
            textPaint.typeface = value
            invalidate()
        }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
    }

    private val dividerWidth = 2.5f.dp
    private val topBackground: Drawable =
        ResourcesCompat.getDrawable(resources, R.drawable.bg_flip_top, null)!!

    private val bottomBackground: Drawable =
        ResourcesCompat.getDrawable(resources, R.drawable.bg_flip_bottom, null)!!

    private var currentText: String = ""
    private var nextText: String = ""

    var text: String
        get() = currentText
        set(value) {
            if (currentText != "") {
                nextText = value
                startFipAnimation()
            } else {
                currentText = value
                invalidate()
            }
        }

    private fun startFipAnimation() {
        if (isInEditMode) {
            return
        }
        if (flipAnimator.isRunning) {
            flipAnimator.cancel()
        }
        flipAnimator.start()
    }

    @get:Keep
    @set:Keep
    var degrees: Float = 0f
        set(value) {
            field = value % 360f
            invalidate()
        }

    var animationDuration = 200L
        set(value) {
            field = value
            initAnimator()
        }
    private lateinit var flipAnimator: Animator

    init {
        textSize = 30.sp
        initAnimator()

        if (isInEditMode && currentText == "") {
            currentText = "59"
        }

        parseAttrs(attrs, defStyleAttr)
    }

    private fun initAnimator() {
        flipAnimator = ObjectAnimator.ofFloat(this, "degrees", 0f, 180f)
            .apply {
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        currentText = nextText
                        degrees = 0f
                    }
                })

                duration = animationDuration
            }
    }

    private fun parseAttrs(attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs == null) {
            return
        }

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.FlipTextView,
            defStyleAttr,
            0
        ).apply {
            try {
                currentText = getString(R.styleable.FlipTextView_flip_text) ?: ""
                textSize = getDimension(R.styleable.FlipTextView_flip_textSize, 30.sp)
                textColor = getColor(R.styleable.FlipTextView_flip_textColor, Color.GRAY)

                animationDuration = getInt(R.styleable.FlipTextView_flip_animationDuration, 200).toLong()

                val fontId = getResourceId(R.styleable.FlipTextView_flip_textFont, -1)
                if (fontId != -1 && !isInEditMode) {
                    textPaint.typeface = ResourcesCompat.getFont(context, fontId)
                }
            } finally {
                recycle()
            }
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = Math.max(suggestedMinimumHeight, (textSize / 0.55f).roundToInt())
        var desiredWidth = suggestedMinimumWidth
        if (currentText.isNotEmpty()) {
            var textAndPaddingWidth = Math.max(
                textPaint.measureText(currentText).roundToInt(),
                desiredHeight
            )
            textAndPaddingWidth += paddingLeft + paddingRight

            desiredWidth = Math.max(textAndPaddingWidth, desiredWidth)
        }

        val width = getMeasuredSize(desiredWidth, widthMeasureSpec)
        val height = getMeasuredSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    private fun getMeasuredSize(desired: Int, measureSpec: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)

        return when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> Math.min(size, desired)
            else -> desired
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null) {
            return
        }

        // Draw the underlying, next part.
        if (degrees > 0f) drawTopSide(canvas, 0f, nextText)

        // If the current side is still visible?
        if (degrees < 90) drawTopSide(canvas, -degrees, currentText)

        // If the current bottom part is visible?
        if (degrees < 180f) drawBottomSide(canvas, 0f, currentText)

        // If the next text bottom part is visible?
        if (degrees >= 90) drawBottomSide(canvas, 180f - degrees, nextText)

    }

    override fun onDetachedFromWindow() {
        if (flipAnimator.isRunning) {
            flipAnimator.cancel()
        }
        super.onDetachedFromWindow()
    }

    private fun drawTopSide(canvas: Canvas, rotation: Float, text: String) {
        drawTextSide(canvas, rotation, text, -1f)
    }

    private fun drawBottomSide(canvas: Canvas, rotation: Float, text: String) {
        drawTextSide(canvas, rotation, text, 1f)
    }

    private fun drawTextSide(canvas: Canvas, rotation: Float, text: String, sideMultiplier: Float) {
        val checkPoint = canvas.save()

        // Set top clip rect.
        val regionWidth = 1f * width
        val regionHeight = height / 2f - dividerWidth / 2f

        if (sideMultiplier > 0f) {
            canvas.translate(0f, regionHeight + dividerWidth / 2f)
        }
        canvas.clipRect(0f, 0f, regionWidth, regionHeight)

        camera.save()
        camera.rotateX(rotation)
        camera.getMatrix(cameraMatrix)

        cameraMatrix.preTranslate(-regionWidth / 2f, sideMultiplier * regionHeight)
        cameraMatrix.postTranslate(regionWidth / 2f, -sideMultiplier * regionHeight)

        canvas.concat(cameraMatrix)

        // Measure text.
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)

        val dyTop = height / 2f + textBounds.height() / 2f
        val dyBottom = dyTop - regionHeight - dividerWidth / 2f

        val dy = if (sideMultiplier < 0f) dyTop else dyBottom

        var dx = paddingLeft * 1f + (width - paddingLeft - paddingRight) / 2f - textBounds.width() / 2
        if (dx < 0) dx = 0f

        // Draw background.
        val background = if (sideMultiplier < 0f) topBackground else bottomBackground
        val drawableWidth = (regionWidth - (paddingLeft + paddingRight)).roundToInt()
        var drawableHeight = regionHeight.toInt()

        val drawableTranslateY = if (sideMultiplier == -1f) {
            drawableHeight -= paddingTop
            paddingTop.toFloat()
        } else {
            drawableHeight -= paddingBottom
            0f
        }
        canvas.save()
        canvas.translate(paddingLeft.toFloat(), drawableTranslateY)
        background.setBounds(0, 0, drawableWidth, drawableHeight)
        background.draw(canvas)
        canvas.restore()

        canvas.drawText(text, dx, dy, textPaint)
        camera.restore()

        canvas.restoreToCount(checkPoint)
    }

}
