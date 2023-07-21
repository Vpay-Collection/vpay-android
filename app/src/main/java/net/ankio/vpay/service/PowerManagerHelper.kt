package net.ankio.vpay.service

import android.content.Context
import android.os.PowerManager
import androidx.core.content.ContextCompat.getSystemService


class PowerManagerHelper(private val context: Context) {
    private var wakeLock: PowerManager.WakeLock? = null

    fun acquireWakeLock() {
        val powerManager = getSystemService(context, PowerManager::class.java)
        wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyApp::MyWakeLockTag"
        )

        wakeLock?.acquire()
    }

    fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }
}