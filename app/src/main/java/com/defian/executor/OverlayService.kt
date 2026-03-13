package com.defian.executor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: FrameLayout
    private var params: WindowManager.LayoutParams? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        showOverlay()
    }

    private fun startForegroundService() {
        val channelId = "executor_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Executor Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Defian Executor Active")
            .setContentText("Tap the floating icon to open menu")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .build()

        startForeground(1, notification)
    }

    private fun showOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        overlayView = FrameLayout(this)
        
        // Floating Icon (Collapsed State)
        val collapsedView = Button(this).apply {
            text = "Δ"
            setBackgroundColor(Color.parseColor("#BB86FC"))
            setTextColor(Color.BLACK)
            textSize = 20f
        }

        // Expanded Menu (Script Runner)
        val expandedView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            setPadding(20, 20, 20, 20)
            visibility = View.GONE
            
            val scriptInput = EditText(this@OverlayService).apply {
                hint = "Enter script here..."
                setTextColor(Color.WHITE)
                setHintTextColor(Color.GRAY)
            }
            
            val runButton = Button(this@OverlayService).apply {
                text = "Execute Dash"
                setOnClickListener {
                    executeDash()
                }
            }

            val closeButton = Button(this@OverlayService).apply {
                text = "Close Menu"
                setOnClickListener {
                    visibility = View.GONE
                    collapsedView.visibility = View.VISIBLE
                }
            }

            addView(scriptInput)
            addView(runButton)
            addView(closeButton)
        }

        collapsedView.setOnClickListener {
            collapsedView.visibility = View.GONE
            expandedView.visibility = View.VISIBLE
        }

        overlayView.addView(collapsedView, FrameLayout.LayoutParams(150, 150))
        overlayView.addView(expandedView, FrameLayout.LayoutParams(600, 800))

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        overlayView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params!!.x
                        initialY = params!!.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params!!.x = initialX + (event.rawX - initialTouchX).toInt()
                        params!!.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(overlayView, params)
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(overlayView, params)
    }

    private fun executeDash() {
        // Simulation of script execution for Bedrock
        // In reality, this would use a native bridge or Accessibility gesture.
        Toast.makeText(this, "Script: Player.dash(5)", Toast.LENGTH_SHORT).show()
        
        // Example logic:
        // 1. Hook into Minecraft's libminecraftpe.so
        // 2. Find LocalPlayer object
        // 3. Call setVelocity(x, y, z)
        
        // Note: Actual injection requires root or a virtual environment (like Parallel Space)
        // because standard Android apps are sandboxed.
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }
}
