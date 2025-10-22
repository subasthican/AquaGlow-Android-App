package com.example.aquaglow

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

/**
 * Custom view that displays a circular water level indicator with animated waves
 */
class WaterLevelCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var waterLevel = 0f // 0.0 to 1.0
    private var waveOffset = 0f
    
    private val waveAnimator = ValueAnimator.ofFloat(0f, 2 * PI.toFloat())
    
    init {
        setupPaints()
        setupWaveAnimation()
    }
    
    private fun setupPaints() {
        // Circle background
        paint.color = Color.parseColor("#E3F2FD")
        paint.style = Paint.Style.FILL
        
        // Wave paint
        wavePaint.color = Color.parseColor("#2196F3")
        wavePaint.style = Paint.Style.FILL
        wavePaint.alpha = 200
        
        // Border paint
        borderPaint.color = Color.parseColor("#1976D2")
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 8f
        
        // Text paint
        textPaint.color = Color.parseColor("#1976D2")
        textPaint.textSize = 48f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
    }
    
    private fun setupWaveAnimation() {
        waveAnimator.duration = 2000
        waveAnimator.repeatCount = ValueAnimator.INFINITE
        waveAnimator.addUpdateListener { animation ->
            waveOffset = animation.animatedValue as Float
            invalidate()
        }
        waveAnimator.start()
    }
    
    fun setWaterLevel(level: Float) {
        waterLevel = level.coerceIn(0f, 1f)
        invalidate()
    }
    
    fun getWaterLevel(): Float = waterLevel
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = minOf(w, h) / 2f - 20f // 20dp padding
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, paint)
        
        // Draw water level with waves
        drawWaterLevel(canvas)
        
        // Draw border
        canvas.drawCircle(centerX, centerY, radius, borderPaint)
        
        // Draw water level text
        val waterPercentage = (waterLevel * 100).toInt()
        val textY = centerY + textPaint.textSize / 3
        canvas.drawText("$waterPercentage%", centerX, textY, textPaint)
    }
    
    private fun drawWaterLevel(canvas: Canvas) {
        if (waterLevel <= 0f) return
        
        val waterHeight = radius * 2 * waterLevel
        val waterTop = centerY + radius - waterHeight
        
        // Create clipping path for water
        val clipPath = Path()
        clipPath.addCircle(centerX, centerY, radius, Path.Direction.CW)
        canvas.save()
        canvas.clipPath(clipPath)
        
        // Draw water with wave effect
        val wavePath = Path()
        val waveAmplitude = 15f
        val waveFrequency = 0.02f
        
        wavePath.moveTo(centerX - radius, waterTop)
        
        for (x in (centerX - radius).toInt()..(centerX + radius).toInt()) {
            val waveY = waterTop + waveAmplitude * sin(waveFrequency * x + waveOffset).toFloat()
            wavePath.lineTo(x.toFloat(), waveY)
        }
        
        wavePath.lineTo(centerX + radius, centerY + radius)
        wavePath.lineTo(centerX - radius, centerY + radius)
        wavePath.close()
        
        canvas.drawPath(wavePath, wavePaint)
        
        // Add second wave for more realistic effect
        val wave2Path = Path()
        val wave2Amplitude = 8f
        val wave2Frequency = 0.03f
        
        wave2Path.moveTo(centerX - radius, waterTop)
        
        for (x in (centerX - radius).toInt()..(centerX + radius).toInt()) {
            val waveY = waterTop + wave2Amplitude * sin(wave2Frequency * x + waveOffset * 1.5f).toFloat()
            wave2Path.lineTo(x.toFloat(), waveY)
        }
        
        wave2Path.lineTo(centerX + radius, centerY + radius)
        wave2Path.lineTo(centerX - radius, centerY + radius)
        wave2Path.close()
        
        wavePaint.alpha = 100
        canvas.drawPath(wave2Path, wavePaint)
        wavePaint.alpha = 200
        
        canvas.restore()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        waveAnimator.cancel()
    }
}


