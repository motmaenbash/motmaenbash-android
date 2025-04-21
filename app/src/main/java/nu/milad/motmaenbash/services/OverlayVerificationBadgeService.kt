package nu.milad.motmaenbash.services

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.ui.activities.MainActivity

class OverlayVerificationBadgeService : Service() {
    private var mWindowManager: WindowManager? = null
    private var mFloatingView: View? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        //Inflate the floating view layout we created


        mFloatingView =
            LayoutInflater.from(this).inflate(R.layout.overlay_verification_badge, null)


        // Set up WindowManager LayoutParams
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,

            // Determine the layout flag based on Android version
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


        //Specify the view position
        params.gravity = Gravity.TOP or Gravity.CENTER
        params.x = 0
        params.y = 0

        //Add the view to the window
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mWindowManager!!.addView(mFloatingView, params)

        // Add the view to the window with animation
//        val animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_top)
//        mFloatingView?.startAnimation(animation)

//        mFloatingView?.postDelayed({
//            val animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_top)
//            mFloatingView?.startAnimation(animation)
//        }, 100)

        // Setup views and listeners
        val collapsedView = mFloatingView?.findViewById<View>(R.id.collapse_view)
        val expandedView = mFloatingView?.findViewById<View>(R.id.expanded_container)


        //Set the close button
        val closeButtonCollapsed = mFloatingView?.findViewById<View>(R.id.close_btn) as ImageView
        val closeDark = mFloatingView?.findViewById<View>(R.id.dark) as View


        mFloatingView?.findViewById<ImageView>(R.id.close_btn)?.setOnClickListener {
            //close the service and remove the from from the window
            stopSelf()
        }

        //Set the close button
        mFloatingView?.findViewById<ImageView>(R.id.close_button)?.setOnClickListener {
            collapsedView?.visibility = View.VISIBLE
            expandedView?.visibility = View.GONE
        }

        closeDark.setOnClickListener {
            stopSelf()
        }


        mFloatingView?.findViewById<ImageView>(R.id.help_button)?.setOnClickListener {
            //Open the application  click.
            val intent = Intent(this@OverlayVerificationBadgeService, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //close the service and remove view from the view hierarchy
            stopSelf()
        }

        //Drag and move floating view using user's touch action.
        mFloatingView?.findViewById<View>(R.id.root_container)
            ?.setOnTouchListener(object : View.OnTouchListener {

                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f


                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            //remember the initial position.
                            initialX = params.x
                            initialY = params.y

                            //get the touch location
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            return true
                        }

                        MotionEvent.ACTION_UP -> {
                            val xDiff = (event.rawX - initialTouchX).toInt()
                            val yDiff = (event.rawY - initialTouchY).toInt()


                            //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                            //So that is click event.
                            if (xDiff < 10 && yDiff < 10) {
                                if (isViewCollapsed) {
                                    //When user clicks on the image view of the collapsed layout,
                                    //visibility of the collapsed layout will be changed to "View.GONE"
                                    //and expanded view will become visible.
                                    collapsedView?.visibility = View.VISIBLE
                                    expandedView?.visibility = View.GONE
                                }
                            }
                            return true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            //Calculate the X and Y coordinates of the view.
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()


                            //Update the layout with new X & Y coordinate
                            mWindowManager!!.updateViewLayout(mFloatingView, params)
                            return true
                        }
                    }
                    return false
                }
            })

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Get the URL from the Intent
        val url = intent?.getStringExtra("URL")
        if (url != null) {
            val urlTextView = mFloatingView?.findViewById<TextView>(R.id.url_text_view)
            urlTextView?.text = url
        }
        return START_NOT_STICKY
    }

    private val isViewCollapsed: Boolean
        /**
         * Detect if the floating view is collapsed or expanded.
         *
         * @return true if the floating view is collapsed.
         */
        get() = mFloatingView == null || mFloatingView!!.findViewById<View>(R.id.collapse_view).isVisible


    override fun onDestroy() {
        super.onDestroy()
        if (mFloatingView != null) {
            mWindowManager?.removeViewImmediate(mFloatingView)
            mFloatingView = null
        }
    }


}