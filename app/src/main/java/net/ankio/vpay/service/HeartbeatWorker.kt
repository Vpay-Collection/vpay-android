package net.ankio.vpay.service

import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import net.ankio.vpay.BuildConfig
import net.ankio.vpay.utils.AnkioApi
import net.ankio.vpay.utils.Logger
import net.ankio.vpay.utils.PushUtils
import net.ankio.vpay.utils.SpUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Date

class HeartbeatWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {
    private val TAG = "HeartbeatManager"
    override fun doWork(): Result {
        // 实现心跳逻辑
        val host = SpUtils.getString("host")
        val key = SpUtils.getString("key")
        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(key)) {
            SpUtils.putInt("heart",0)
            SpUtils.putString("reason","尚未配置数据")
            Logger.d(TAG, "请先配置监控地址和密钥！", context)
            return  Result.failure()
        }else{
            val t: String = java.lang.String.valueOf(Date().time/1000)
            val jsonObject = JSONObject()
            jsonObject.put("t", t)
            jsonObject.put("ver", BuildConfig.VERSION_NAME)
            val sign = PushUtils.md5(jsonObject.toString() + key)
            jsonObject.put("sign", sign)

            try {
                val okHttpClient = OkHttpClient()
                val url = "$host/api/app/heart?" + PushUtils.jsonObjectToUrlParams(jsonObject)
                val request: Request =
                    Request.Builder()
                        .url(url)
                        .method("GET", null)
                        .build()
                //  Logger.d(TAG, "心跳地址：${url}", context)

                val response = okHttpClient.newCall(request).execute()

                if(!response.isSuccessful || response.code!=200){
                    Logger.d(TAG, "心跳失败: ${response.message}", context)
                    SpUtils.putInt("heart",0)
                    SpUtils.putString("reason", response.message)
                    return Result.retry()
                }

                try {
                    val api = Gson().fromJson(response.body?.string(), AnkioApi::class.java)
                    return if(api.code == 200){
                        SpUtils.putInt("heart",1)
                        SpUtils.putLong("time_heart",System.currentTimeMillis())
                        Result.success()
                    }else{
                        SpUtils.putInt("heart",0)
                        SpUtils.putString("reason",api.msg)
                        Logger.d(TAG, "心跳异常: ${api.msg}", context)
                        Result.retry()
                    }
                }catch (e : JsonSyntaxException){
                    SpUtils.putInt("heart",0)
                    e.message?.let { SpUtils.putString("reason", it) }
                    Logger.d(TAG, "心跳错误: ${e.message}", context)
                    return Result.retry()
                }

            } catch (e: IllegalArgumentException) {
                SpUtils.putInt("heart",0)
                e.message?.let { SpUtils.putString("reason", it) }
                Logger.d(TAG, "配置错误: ${e.message}", context)
                return Result.retry()
            }

        }
    }
}