package net.ankio.vpay.service

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import net.ankio.vpay.App
import net.ankio.vpay.R
import net.ankio.vpay.ui.MainActivity
import net.ankio.vpay.utils.Logger
import net.ankio.vpay.utils.PushUtils
import net.ankio.vpay.utils.context


class NotificationAccessibilityService : AccessibilityService() {

    private val TAG = "Notification"
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            // 这里处理通知的逻辑，可以根据通知的内容、标题等信息进行相应的操作
            val packageName = event.packageName.toString()

            val notify = event.parcelableData as Notification?
            if (notify != null) {
                val title = notify.extras.getString(Notification.EXTRA_TITLE)?:""
                val content = notify.extras.getString(Notification.EXTRA_TEXT)?:""
                Logger.d(TAG, "\n============================\n" +
                        "包名："+packageName+"\n" +
                        "标题："+title+"\n" +
                        "内容："+content+"\n" +
                        "============================", this)
                when (packageName) {
                    "com.eg.android.AlipayGphone" -> if (content != "") {
                        if (
                            content.contains("通过扫码向你付款")
                            || content.contains("成功收款")
                            || title.contains("你已成功收款")
                        ) {
                            var money = PushUtils.extractAmount(content)
                            if (money <= 0) money = PushUtils.extractAmount(title)
                            Logger.d(TAG, "匹配成功： 支付宝到账 $money", this)
                            PushUtils.appPush(App.PAY_ALIPAY, money,this)
                        }
                    }
                    "com.tencent.mm" -> if (content != "") {
                        if (title == "微信支付" || title == "微信收款助手" || title == "微信收款商业版") {
                            if(content.contains("已支付"))return
                            var money = PushUtils.extractAmount(content)
                            if (money <= 0) money = PushUtils.extractAmount(title)
                            Logger.d(TAG, "匹配成功： 微信到账 $money", this)
                            PushUtils.appPush(App.PAY_WECHAT, money,this)

                        }
                    }



                }
            }
        }
    }


    override fun onInterrupt() {
        // 当服务中断时调用，不需要处理
    }
    companion object {
        const val CHANNEL_ID = "notificationListener"
        const val NOTIFICATION_ID = 1
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification)
            val descriptionText = context.getString(R.string.notification)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    override fun onServiceConnected() {
        super.onServiceConnected()
        // 配置服务参数，设置对应的事件类型
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo = info

        createNotificationChannel()

        // 创建通知
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification))
           // .setContentText("点击管理服务设置")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()


        // 启动前台服务
        startForeground(NOTIFICATION_ID, notification)

        //自动切换回app
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

}

