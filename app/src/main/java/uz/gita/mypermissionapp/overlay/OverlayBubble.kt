package uz.gita.mypermissionapp.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

/**
 * A small bubble drawn on top of every app, like a chat head.
 * This only works after the user grants SYSTEM_ALERT_WINDOW ("Display over other apps").
 */
object OverlayBubble {
    // Built with the application context, so holding it here doesn't leak an Activity
    @SuppressLint("StaticFieldLeak")
    private var bubble: TextView? = null

    val isShowing: Boolean get() = bubble != null

    fun show(context: Context): Boolean {
        if (bubble != null) return true
        if (!Settings.canDrawOverlays(context)) return false

        val appContext = context.applicationContext
        val windowManager = appContext.getSystemService(WindowManager::class.java)

        val view = TextView(appContext).apply {
            text = "💬 I'm drawn over other apps. Tap to close"
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(40, 28, 40, 28)
            background = GradientDrawable().apply {
                cornerRadius = 60f
                setColor(0xE6512DA8.toInt())
            }
            setOnClickListener { hide(appContext) }
        }

        @Suppress("DEPRECATION")
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 200
        }

        windowManager.addView(view, params)
        bubble = view
        return true
    }

    fun hide(context: Context) {
        val view = bubble ?: return
        context.applicationContext.getSystemService(WindowManager::class.java).removeView(view)
        bubble = null
    }
}
