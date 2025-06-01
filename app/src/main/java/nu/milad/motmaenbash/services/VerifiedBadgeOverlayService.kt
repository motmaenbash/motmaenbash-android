package nu.milad.motmaenbash.services

import android.animation.ValueAnimator
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import nu.milad.motmaenbash.R

class VerifiedBadgeOverlayService : Service() {
    private var mWindowManager: WindowManager? = null
    private var mFloatingView: View? = null
    private var statusTextView: TextView? = null
    private var handler: Handler? = null
    private var textSwitchRunnable: Runnable? = null
    private var currentTextIndex = 0
    private val statusTexts = arrayOf("درگاه پرداخت معتبر", "مطمئن باش!")

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        mFloatingView =
            LayoutInflater.from(this).inflate(R.layout.overlay_verification_badge, null, false)


        // Calculate screen width and desired margins
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val marginInPx = (12 * displayMetrics.density).toInt()

        // Set up WindowManager LayoutParams
        val params = WindowManager.LayoutParams(
            screenWidth - (marginInPx * 2),
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        // Specify the view position - center horizontally with margins
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.x = 0
        params.y = 0

        // Add the view to the window
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mWindowManager!!.addView(mFloatingView, params)

        // Initialize status text view and start animation
        statusTextView = mFloatingView?.findViewById(R.id.status_text_view)
        startTextAnimation()

        // Set up close button listener
        mFloatingView?.findViewById<ImageView>(R.id.close_button)?.setOnClickListener {
            stopSelf()
        }


        // Set up drag functionality
        mFloatingView?.findViewById<View>(R.id.root_container)?.setOnTouchListener(
            object : View.OnTouchListener {
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            return true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            mWindowManager!!.updateViewLayout(mFloatingView, params)
                            return true
                        }

                        MotionEvent.ACTION_UP -> {
                            return true
                        }
                    }
                    return false
                }
            })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("URL")
        if (url != null) {
            val urlTextView = mFloatingView?.findViewById<TextView>(R.id.url_text_view)
            urlTextView?.text = url
        }
        return START_NOT_STICKY
    }

    private fun startTextAnimation() {
        handler = Handler(Looper.getMainLooper())
        textSwitchRunnable = object : Runnable {
            override fun run() {
                switchTextWithFade()
                handler?.postDelayed(this, 3000) // Switch every 3 seconds
            }
        }
        handler?.postDelayed(textSwitchRunnable!!, 3000)
    }

    private fun switchTextWithFade() {
        statusTextView?.let { textView ->
            // Fade out animation
            val fadeOut = ValueAnimator.ofFloat(1f, 0f)
            fadeOut.duration = 300
            fadeOut.addUpdateListener { animation ->
                textView.alpha = animation.animatedValue as Float
            }

            fadeOut.addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}
                override fun onAnimationCancel(animation: android.animation.Animator) {}
                override fun onAnimationRepeat(animation: android.animation.Animator) {}

                override fun onAnimationEnd(animation: android.animation.Animator) {
                    // Change text
                    currentTextIndex = (currentTextIndex + 1) % statusTexts.size
                    textView.text = statusTexts[currentTextIndex]

                    // Fade in animation
                    val fadeIn = ValueAnimator.ofFloat(0f, 1f)
                    fadeIn.duration = 300
                    fadeIn.addUpdateListener { anim ->
                        textView.alpha = anim.animatedValue as Float
                    }
                    fadeIn.start()
                }
            })

            fadeOut.start()
        }
    }

    private fun stopTextAnimation() {
        textSwitchRunnable?.let { handler?.removeCallbacks(it) }
        handler = null
        textSwitchRunnable = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTextAnimation()
        if (mFloatingView != null) {
            mWindowManager?.removeViewImmediate(mFloatingView)
            mFloatingView = null
        }
    }
}