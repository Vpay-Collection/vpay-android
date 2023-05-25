package net.ankio.vpay.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import net.ankio.vpay.BuildConfig
import net.ankio.vpay.utils.AnkioApi
import net.ankio.vpay.utils.Logger
import net.ankio.vpay.utils.NAME
import net.ankio.vpay.utils.PayUtils
import net.ankio.vpay.utils.PushUtils
import net.ankio.vpay.utils.SpUtils
import net.ankio.vpay.utils.context
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*


class NotificationService : NotificationListenerService() , SharedPreferences.OnSharedPreferenceChangeListener {
    private val TAG = "Notification"
    private var host: String? = null
    private var key: String? = null
    private var newThread: Thread? = null
    private var mWakeLock: WakeLock? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()


    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        host = SpUtils.getString("host")
        key = SpUtils.getString("key")
        // 注册监听器
        // 获取SharedPreferences实例
        sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

        // 注册监听器
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }


    //申请设备电源锁
    @SuppressLint("InvalidWakeLockTag")
    fun acquireWakeLock(context: Context) {
        if (null == mWakeLock) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            mWakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
                "WakeLock"
            )
            if (null != mWakeLock) {
                mWakeLock!!.acquire(10 * 60 * 1000L /*10 minutes*/)
            }
        }
    }

    //释放设备电源锁
    private fun releaseWakeLock() {
        if (null != mWakeLock) {
            mWakeLock!!.release()
            mWakeLock = null
        }
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    //心跳进程
    private fun initAppHeart() {

        if (newThread != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
            return
        }
        acquireWakeLock(this)

        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(key)) {
            SpUtils.putInt("time",0)
            SpUtils.putString("reason","尚未配置数据")
            Logger.d(TAG, "请先配置监控地址和密钥！", this)
            Toast.makeText(applicationContext,"请先配置监控地址和密钥！",Toast.LENGTH_LONG).show()
            return
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        newThread = Thread {
            Logger.d(TAG, "心跳线程启动！", this)
            try {
                while (!Thread.currentThread().isInterrupted) {
                    //这里写入子线程需要做的工作
                    val t: String = java.lang.String.valueOf(Date().time/1000)
                    val jsonObject = JSONObject()
                    jsonObject.put("t", t)
                    jsonObject.put("ver", BuildConfig.VERSION_NAME)
                    val sign = PushUtils.md5(jsonObject.toString() + key)
                    jsonObject.put("sign", sign)

                    val  that = this
                    try {
                        val okHttpClient = OkHttpClient()
                        val url = "$host/api/app/heart?" + PushUtils.jsonObjectToUrlParams(jsonObject)
                        val request: Request =
                            Request.Builder()
                                .url(url)
                                .method("GET", null)
                                .build()
                        Logger.d(TAG, "心跳地址：${url}", this)

                        okHttpClient.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Logger.d(TAG, "心跳失败: ${e.message}", that)
                                SpUtils.putInt("heart",0)
                                e.message?.let { SpUtils.putString("reason", it) }
                            }

                            override fun onResponse(call: Call, response: Response) {
                                val api = Gson().fromJson(response.body?.string(),AnkioApi::class.java)
                                if(api.code == 200){
                                    SpUtils.putInt("heart",1)
                                    SpUtils.putLong("time_heart",System.currentTimeMillis())
                                    Logger.d(TAG, "心跳成功: ${api.msg}", that)
                                }else{
                                    SpUtils.putInt("heart",0)
                                    SpUtils.putString("reason",api.msg)
                                    Logger.d(TAG, "心跳异常: ${api.msg}", that)
                                }

                            }
                        })
                    } catch (e: IllegalArgumentException) {
                        Thread.currentThread().interrupt()
                        SpUtils.putInt("heart",0)
                        e.message?.let { SpUtils.putString("reason", it) }
                        Logger.d(TAG, "配置错误: ${e.message}", that)
                    }

                    Thread.sleep((60 * 1000 * 10).toLong())
                }
            } catch (_: InterruptedException) {

            }
        }
        newThread!!.start() //启动线程
    }

    private fun shutdownHeart() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        if (newThread == null) {
            return
        }
        newThread!!.interrupt();

    }

    //当收到一条消息的时候回调，sbn是收到的消息
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification: Notification? = sbn.notification
        val pkg = sbn.packageName
        if (notification != null) {
            val extras: Bundle = notification.extras
            val title = extras.getString(NotificationCompat.EXTRA_TITLE, "")
            val content = extras.getString(NotificationCompat.EXTRA_TEXT, "")
            if(pkg!="com.eg.android.AlipayGphone" && pkg!="com.tencent.mm") {

                Log.w(TAG, "**********通知到达************")
                Log.w(TAG, "包名:$pkg")
                Log.w(TAG, "标题:$title")
                Log.w(TAG, "内容:$content")
                Log.w(TAG, "**********通知结束************")
                return
            }
            Logger.d(TAG, "**********通知到达************", this)
            Logger.d(TAG, "包名:$pkg", this)
            Logger.d(TAG, "标题:$title", this)
            Logger.d(TAG, "内容:$content", this)
            Logger.d(TAG, "**********通知结束************", this)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(sbn.id) // 使用通知的ID移除单个通知
            when (pkg) {
                "com.eg.android.AlipayGphone" -> if (content != null && content != "") {
                    if (
                        content.contains("通过扫码向你付款")
                        || content.contains("成功收款")
                        || title.contains("你已成功收款")
                    ) {
                        var money = PushUtils.extractAmount(content)
                        if (money < 0) money = PushUtils.extractAmount(title)
                        Logger.d(TAG, "匹配成功： 支付宝 到账 $money", this)
                        PushUtils.appPush(1, money,this)
                        PayUtils.add(PushUtils.convertTimestampToDateTime(System.currentTimeMillis()),1,money)
                    }
                }
                "com.tencent.mm" -> if (content != null && content != "") {
                    if (title == "微信支付" || title == "微信收款助手" || title == "微信收款商业版") {
                        if(content.contains("已支付"))return
                        var money = PushUtils.extractAmount(content)
                        if (money < 0) money = PushUtils.extractAmount(title)
                        Logger.d(TAG, "匹配成功： 微信到账 $money", this)
                        PushUtils.appPush(2, money,this)
                        PayUtils.add(PushUtils.convertTimestampToDateTime(System.currentTimeMillis()),2,money)
                    }
                }


            }
        }
    }

    //当移除一条消息的时候回调，sbn是被移除的消息
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {}

    //当连接成功时调用，一般在开启监听后会回调一次该方法
    override fun onListenerConnected() {
        //开启心跳线程
        initAppHeart()
        Logger.d(TAG, "监听服务开启！", this)
    }

    override fun onListenerDisconnected() {
        releaseWakeLock()
        shutdownHeart()

        Logger.d(TAG, "监听服务关闭！", this)
    }


    //解析金额信息

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "host"||key == "key") {
            host = SpUtils.getString("host")
            this.key = SpUtils.getString("key")
            if(TextUtils.isEmpty(host) || TextUtils.isEmpty(key))return
            shutdownHeart()
            initAppHeart()
        }
    }
}