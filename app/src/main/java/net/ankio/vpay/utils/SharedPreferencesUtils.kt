package net.ankio.vpay.utils;

import android.content.Context
import android.content.SharedPreferences
import net.ankio.vpay.Notification

class SharedPreferencesUtils(context: Context) {
private val PREFERENCE_NAME = "my_preference" // 自定义的 SharedPreferences 名称

private  var sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)



        fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
        }

        fun getString(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: ""
        }


}
