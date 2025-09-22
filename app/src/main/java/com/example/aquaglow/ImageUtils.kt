package com.example.aquaglow

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

/**
 * ImageUtils provides optimized image loading and caching
 * Reduces memory usage and improves performance
 */
object ImageUtils {
    
    private val drawableCache = mutableMapOf<Int, Drawable>()
    
    /**
     * Gets a cached drawable or loads it from resources
     */
    fun getCachedDrawable(context: Context, drawableRes: Int): Drawable? {
        return drawableCache[drawableRes] ?: run {
            val drawable = ContextCompat.getDrawable(context, drawableRes)
            drawable?.let { drawableCache[drawableRes] = it }
            drawable
        }
    }
    
    /**
     * Converts vector drawable to bitmap with specified size
     */
    fun vectorDrawableToBitmap(drawable: VectorDrawable, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
    
    /**
     * Tints a drawable with the specified color
     */
    fun tintDrawable(drawable: Drawable, color: Int): Drawable {
        val wrappedDrawable = DrawableCompat.wrap(drawable.mutate())
        DrawableCompat.setTint(wrappedDrawable, color)
        return wrappedDrawable
    }
    
    /**
     * Clears the drawable cache to free memory
     */
    fun clearCache() {
        drawableCache.clear()
    }
    
    /**
     * Gets memory usage of the drawable cache
     */
    fun getCacheSize(): Int = drawableCache.size
}

