package com.example.aquaglow

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

/**
 * GlowAnimationUtils provides ReactBits-like glowing button animations
 * with gradient borders, movement effects, and color transitions
 */
object GlowAnimationUtils {

    /**
     * Creates a horizontal glow movement animation (left-right sway)
     */
    fun createGlowMovement(view: View, duration: Long = 5000L) {
        val animator = ObjectAnimator.ofFloat(view, "translationX", -20f, 20f)
        animator.duration = duration
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.start()
    }

    /**
     * Creates a vertical glow movement animation (up-down float)
     */
    fun createFloatMovement(view: View, duration: Long = 4000L) {
        val animator = ObjectAnimator.ofFloat(view, "translationY", -15f, 15f)
        animator.duration = duration
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.start()
    }

    /**
     * Creates a pulsing scale animation for emphasis
     */
    fun createPulseEffect(view: View, duration: Long = 2000L) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.05f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.05f, 1.0f)
        
        scaleX.duration = duration
        scaleY.duration = duration
        scaleX.repeatCount = ObjectAnimator.INFINITE
        scaleY.repeatCount = ObjectAnimator.INFINITE
        
        scaleX.start()
        scaleY.start()
    }

    /**
     * Creates a gradient color animation that cycles through colors
     */
    fun createGradientAnimation(button: Button, duration: Long = 4000L) {
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(Color.CYAN, Color.MAGENTA, Color.BLUE)
        )
        gradient.cornerRadius = 20f
        button.background = gradient

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = duration
        animator.repeatCount = ValueAnimator.INFINITE
        animator.addUpdateListener { animation ->
            val fraction = animation.animatedFraction
            val hue = (360 * fraction) % 360
            val color1 = Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
            val color2 = Color.HSVToColor(floatArrayOf((hue + 60) % 360, 1f, 1f))
            
            gradient.colors = intArrayOf(color1, color2)
        }
        animator.start()
    }

    /**
     * Creates a combined glow effect with movement and pulse
     */
    fun createFullGlowEffect(view: View, movementDuration: Long = 5000L, pulseDuration: Long = 2000L) {
        createGlowMovement(view, movementDuration)
        createPulseEffect(view, pulseDuration)
    }

    /**
     * Creates a subtle breathing effect for secondary buttons
     */
    fun createBreathingEffect(view: View, duration: Long = 3000L) {
        val alpha = ObjectAnimator.ofFloat(view, "alpha", 0.7f, 1.0f, 0.7f)
        alpha.duration = duration
        alpha.repeatCount = ObjectAnimator.INFINITE
        alpha.repeatMode = ObjectAnimator.REVERSE
        alpha.start()
    }

    /**
     * Applies glow effect to MaterialButton with elevation and ripple
     */
    fun applyMaterialGlow(button: MaterialButton, glowColor: Int = Color.CYAN) {
        button.elevation = 8f
        button.setRippleColorResource(R.color.secondary_light)
        button.cornerRadius = 20
    }

    /**
     * Creates a star-like twinkling effect using rotation
     */
    fun createTwinkleEffect(view: View, duration: Long = 3000L) {
        val rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f)
        rotation.duration = duration
        rotation.repeatCount = ObjectAnimator.INFINITE
        rotation.start()
    }
}
