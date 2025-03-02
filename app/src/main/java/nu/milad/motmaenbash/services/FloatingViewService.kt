package nu.milad.motmaenbash.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.View
import android.view.WindowManager

class FloatingViewService : Service() {
    private var mWindowManager: WindowManager? = null
    private var mFloatingView: View? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        //Inflate the floating view layout we created


        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mFloatingView != null) {
            mWindowManager?.removeViewImmediate(mFloatingView)
            mFloatingView = null
        }
    }


}