package net.ankio.vpay.utils

import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PushUtils {
    private val TAG = "PushUtils"
    private var host: String? = null
    private var key: String? = null
    init {
        host = SpUtils.getString("host")
        key = SpUtils.getString("key")
    }
    fun appPush(type: Int, price: Double, context: Context) {
        if (price < 0) {
            Logger.d(TAG, "金额小于0，不推送！", context)
            return
        }

        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(key)) return

        Logger.d(TAG, "推送数据: 类型： $type 金额：$price", context)


        val jsonObject = JSONObject()
        jsonObject.put("t", java.lang.String.valueOf(Date().time/1000))
        jsonObject.put("type", type)
        jsonObject.put("price", price)
        jsonObject.put("sign", md5(jsonObject.toString() + key))

        try {
            val url = "$host/api/app/push?" + jsonObjectToUrlParams(jsonObject)
            Logger.d(TAG, "推送数据地址：$url", context)
            val okHttpClient = OkHttpClient()
            val request: Request =
                Request.Builder().url(url).method("GET", null).build()

            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Logger.d(TAG, "推送失败: ${e.message}", context)
                }

                override fun onResponse(call: Call, response: Response) {
                    val api = Gson().fromJson(response.body?.string(),AnkioApi::class.java)
                    if(api.code==200){
                        Logger.d(TAG, "推送成功: ${response.body?.string()}", context)
                    }else{
                        Logger.d(TAG, "推送失败: ${api.msg}", context)
                    }

                }
            })
        } catch (e: IllegalArgumentException) {
            Logger.d(TAG, "网站响应异常", context)
        }

    }

    fun extractAmount(input: String): Double {
        val regex = Regex("[0-9]+(,[0-9]{3})*(\\.[0-9]{2})?")
        val matchResult = regex.find(input)
        if (matchResult != null) {
            val amountString = matchResult.value.replace(",", "")
            return amountString.toDouble()
        }
        return 0.0
    }


    fun md5(string: String): String {
        if (TextUtils.isEmpty(string)) {
            return ""
        }
        val md5: MessageDigest?
        try {
            md5 = MessageDigest.getInstance("MD5")
            val bytes: ByteArray = md5.digest(string.toByteArray())
            var result = ""
            for (b in bytes) {
                var temp = Integer.toHexString(b.toInt() and 0xff)
                if (temp.length == 1) {
                    temp = "0$temp"
                }
                result += temp
            }
            return result
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }
      fun jsonObjectToUrlParams(jsonObject: JSONObject): String {
        val params = StringBuilder()
        val keys = jsonObject.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.get(key).toString()

            if (params.isNotEmpty()) {
                params.append("&")
            }

            params.append(key)
                .append("=")
                .append(URLEncoder.encode(value, "UTF-8"))
        }

        return params.toString()
    }

    fun convertTimestampToDateTime(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }

}