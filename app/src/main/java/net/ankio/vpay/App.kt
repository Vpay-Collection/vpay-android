package net.ankio.vpay

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils.SimpleStringSplitter
import android.widget.Toast
import com.flurry.android.FlurryAgent
import com.quickersilver.themeengine.ThemeEngine
import net.ankio.vpay.service.HeartbeatManager
import net.ankio.vpay.service.NotificationAccessibilityService
import net.ankio.vpay.utils.SpUtils


open class App : Application() {

    companion object {

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        const val PAY_WECHAT = 3 // 微信收款
        const val PAY_ALIPAY = 4 // 支付宝收款
         fun isAccessibilitySettingsOn(context: Context): Boolean {
            val mStringColonSplitter = SimpleStringSplitter(':')
            val settingValue = Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessibilityService = mStringColonSplitter.next()
                    if (accessibilityService.equals(NotificationAccessibilityService.Companion::class.java)) {
                        return true
                    }
                }
            }
            return false
        }
        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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

        if (!isAccessibilitySettingsOn(this)) {
            Toast.makeText(this,R.string.tips, Toast.LENGTH_LONG).show()
            openAccessibilitySettings(this)
        }
    }

    override fun onTerminate() {
        stopHeartbeat() // 停止心跳
        super.onTerminate()
    }



}