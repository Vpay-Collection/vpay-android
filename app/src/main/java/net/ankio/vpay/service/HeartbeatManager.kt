package net.ankio.vpay.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import net.ankio.vpay.BuildConfig
import net.ankio.vpay.utils.AnkioApi
import net.ankio.vpay.utils.Logger
import net.ankio.vpay.utils.PushUtils
import net.ankio.vpay.utils.SpUtils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Date

private val TAG = "HeartbeatManager"
class HeartbeatManager(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private val heartbeatInterval = 600000L // 心跳间隔，单位为毫秒（10分钟）
    private var isHeartbeatRunning = false

    private val heartbeatRunnable = object : Runnable {
        override fun run() {
            // 在这里执行心跳操作
            val host = SpUtils.getString("host")
            val key = SpUtils.getString("key")
            if (TextUtils.isEmpty(host) || TextUtils.isEmpty(key)) {
                SpUtils.putInt("heart",0)
                SpUtils.putString("reason","尚未配置数据")
                Logger.d(TAG, "请先配置监控地址和密钥！", context)
                Toast.makeText(context,"请先配置监控地址和密钥！", Toast.LENGTH_LONG).show()
            }else{
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
                    Logger.d(TAG, "心跳地址：${url}", context)

                    okHttpClient.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Logger.d(TAG, "心跳失败: ${e.message}", context)
                            SpUtils.putInt("heart",0)
                            e.message?.let { SpUtils.putString("reason", it) }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            try {
                                val api = Gson().fromJson(response.body?.string(), AnkioApi::class.java)
                                if(api.code == 200){
                                    SpUtils.putInt("heart",1)
                                    SpUtils.putLong("time_heart",System.currentTimeMillis())
                                    Logger.d(TAG, "心跳成功: ${api.msg}", context)
                                }else{
                                    SpUtils.putInt("heart",0)
                                    SpUtils.putString("reason",api.msg)
                                    Logger.d(TAG, "心跳异常: ${api.msg}", context)
                                }
                            }catch (e : JsonSyntaxException){
                                SpUtils.putInt("heart",0)
                                e.message?.let { SpUtils.putString("reason", it) }
                                Logger.d(TAG, "心跳错误: ${e.message}", context)
                            }


                        }
                    })
                } catch (e: IllegalArgumentException) {
                    SpUtils.putInt("heart",0)
                    e.message?.let { SpUtils.putString("reason", it) }
                    Logger.d(TAG, "配置错误: ${e.message}", context)
                }
            }

            // 安排下一次心跳
            if (isHeartbeatRunning) {
                handler.postDelayed(this, heartbeatInterval)
            }
        }
    }

    fun startHeartbeat() {
        if (!isHeartbeatRunning) {
            isHeartbeatRunning = true
            handler.post(heartbeatRunnable)
        }
    }

    fun stopHeartbeat() {
        if (isHeartbeatRunning) {
            isHeartbeatRunning = false
            handler.removeCallbacks(heartbeatRunnable)
        }
    }

}