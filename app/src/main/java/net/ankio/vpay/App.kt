package net.ankio.vpay

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import com.flurry.android.FlurryAgent
import com.quickersilver.themeengine.ThemeEngine
import net.ankio.vpay.service.HeartbeatManager
import net.ankio.vpay.service.NotificationAccessibilityService
import net.ankio.vpay.utils.SpUtils
import java.util.*


open class App : Application() {

    companion object {

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        const val PAY_WECHAT = 1 // 微信收款
        const val PAY_ALIPAY = 2 // 支付宝收款

        fun isNotificationAccessibilityServiceEnabled(context: Context): Boolean {
            var isAccessibilityEnabled = false
            (context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager).apply {
                installedAccessibilityServiceList.forEach { installedService ->
                    installedService.resolveInfo.serviceInfo.apply {
                        if (getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK).any {
                            it.resolveInfo.serviceInfo.packageName == packageName &&
                                    it.resolveInfo.serviceInfo.name == name  })
                            isAccessibilityEnabled = true
                    }
                }
            }
            return isAccessibilityEnabled
        }
        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context.startActivity(intent)
        }
        @SuppressLint("StaticFieldLeak")
        private lateinit var heartbeatManager: HeartbeatManager

        fun startHeartbeat() {
            heartbeatManager.startHeartbeat()
        }

        fun stopHeartbeat() {
            heartbeatManager.stopHeartbeat()
        }
    }



    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        ThemeEngine.applyToActivities(this)

        //匿名统计
        if(SpUtils.getBoolean("app_center_analyze",true)){
            FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, "N6BV5FC9PSGZ29FNDD36")
        }
        SpUtils.putBoolean("noticeServer",false)
        heartbeatManager = HeartbeatManager(applicationContext)
        startHeartbeat()
    }

    override fun onTerminate() {
        stopHeartbeat() // 停止心跳
        super.onTerminate()
    }



}