package net.ankio.vpay

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.flurry.android.FlurryAgent
import com.quickersilver.themeengine.ThemeEngine
import net.ankio.vpay.utils.SpUtils
import java.util.*


open class App : Application() {

    companion object {

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
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

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }



}