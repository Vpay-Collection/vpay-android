package net.ankio.vpay

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import net.ankio.vpay.databinding.ActivityMainBinding
import net.ankio.vpay.utils.Logger
import net.ankio.vpay.utils.SharedPreferencesUtils
import org.json.JSONException
import org.json.JSONObject


open class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var thread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (!isNotificationListenersEnabled()) {
            gotoNotificationAccessSetting()
        }
        restartNotification()
        Logger.empty(this)
        binding.host.setText(SharedPreferencesUtils(this).getString("host"))
        binding.key.setText(SharedPreferencesUtils(this).getString("key"))
        binding.save.setOnClickListener {
            SharedPreferencesUtils(this).saveString("host", binding.host.text.toString())
            SharedPreferencesUtils(this).saveString("key", binding.key.text.toString())
            Logger.d("Main", "保存成功，重启监听服务...", this)
            restartNotification()
        }
        binding.scan.setOnClickListener {
            val integrator = IntentIntegrator(this)
            integrator.setPrompt("请扫描微支付后台的配置二维码")
            integrator.captureActivity = MyCaptureActivity::class.java
            // 启动扫描器
            integrator.initiateScan()

        }
    }

    // 在Activity或Fragment中添加onActivityResult方法
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 通过IntentIntegrator获取扫描结果
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                // 如果扫描结果为空
                Logger.d("Main", "二维码扫描结果为空", this)
            } else {

               try{
                   val jsonObject = JSONObject((result.contents))

                   SharedPreferencesUtils(this).saveString("host", jsonObject.getString("host"))
                   SharedPreferencesUtils(this).saveString("key", jsonObject.getString("key"))
                   Logger.d("Main", "配置成功，重启监听服务...", this)
                   restartNotification()
               }catch (e: JSONException){
                   Logger.d("Main", "二维码识别结果错误，请扫描VPay4后台识别二维码", this)
               }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    override fun onResume() {
        super.onResume()
        thread = Thread {
            while (!Thread.currentThread().isInterrupted) {
                val data = Logger.readLog(this)
                Handler(Looper.getMainLooper()).post {
                    binding.log.text = data
                }
                try {
                    Thread.sleep(10)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }
        thread.start()
    }

    override fun onPause() {
        super.onPause()
        thread.interrupt()
    }

    private fun restartNotification() {
        val pm = packageManager
        //先禁用
        pm.setComponentEnabledSetting(
            ComponentName(this, Notification::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        //再启用，重新触发rebind
        pm.setComponentEnabledSetting(
            ComponentName(
                this,
                Notification::class.java
            ),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }


    private fun isNotificationListenersEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun gotoNotificationAccessSetting(): Boolean {
        return try {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) { //普通情况下找不到的时候需要再特殊处理找一次
            try {
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val cn = ComponentName(
                    "com.android.settings",
                    "com.android.settings.Settings\$NotificationAccessSettingsActivity"
                )
                intent.component = cn
                intent.putExtra(":settings:show_fragment", "NotificationAccessSettings")
                startActivity(intent)
                return true
            } catch (e1: Exception) {
                e1.printStackTrace()
            }
            Toast.makeText(this, "对不起，您的手机暂不支持", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            false
        }
    }



}