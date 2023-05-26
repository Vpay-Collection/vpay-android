package net.ankio.vpay.ui.fragment


import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import com.google.android.material.color.MaterialColors
import com.king.zxing.CameraScan
import com.king.zxing.CaptureActivity
import com.quickersilver.themeengine.ThemeEngine
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.ankio.vpay.R
import net.ankio.vpay.databinding.FragmentHomeBinding
import net.ankio.vpay.service.NotificationService
import net.ankio.vpay.utils.Logger
import net.ankio.vpay.utils.PushUtils
import net.ankio.vpay.utils.SpUtils
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data
                    val result = CameraScan.parseScanResult(data)
                    try {
                        Logger.d("Main", "二维码数据：$result", requireContext())
                        val jsonObject = JSONObject(result)
                        SpUtils.putString("host", jsonObject.getString("url"))
                        SpUtils.putString("key", jsonObject.getString("key"))
                        Logger.d("Main", "配置成功，重启监听服务...", requireContext())
                        showMsg("配置成功，尝试心跳中...")
                        GlobalScope.launch {
                            delay(2000) // 延时一秒钟
                            refreshStatus()
                        }

                    } catch (e: JSONException) {
                        Logger.d(
                            "Main",
                            "二维码识别结果错误，请扫描VPay4后台识别二维码",
                            requireContext()
                        )
                        showMsg("二维码识别结果错误，请扫描VPay4后台识别二维码");
                    }
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.scan.setOnClickListener {
            startScan(CaptureActivity::class.java, "请扫描后台二维码绑定")

        }
        binding.url.setText(SpUtils.getString("host"))
        binding.key.setText(SpUtils.getString("key"))
        //restartNotification()
        refreshStatus()
    }

    val KEY_TITLE = "key_title"
    val KEY_IS_CONTINUOUS = "key_continuous_scan"

    /**
     * 扫码
     * @param cls
     * @param title
     */
    private fun startScan(cls: Class<*>, title: String) {
        val intent = Intent(requireActivity(), cls)
        intent.putExtra(KEY_TITLE, title)
        intent.putExtra(KEY_IS_CONTINUOUS, false)
        resultLauncher.launch(intent);
    }
    /**
     * 获取主题色
     */
    private fun getThemeAttrColor( @AttrRes attrResId: Int): Int {
        return MaterialColors.getColor(ContextThemeWrapper(requireContext(), ThemeEngine.getInstance(requireContext()).getTheme()), attrResId, Color.WHITE)
    }

    private fun setActive2(text: String, @AttrRes backgroundColor:Int, @AttrRes textColor:Int, @DrawableRes drawable:Int){
        binding.active2.setBackgroundColor(getThemeAttrColor(backgroundColor))
        binding.imageView2.setImageDrawable(
            AppCompatResources.getDrawable(
                requireActivity(),
                drawable
            )
        )
        binding.msgLabel2.text = text
        binding.imageView2.setColorFilter(getThemeAttrColor(textColor))
        binding.msgLabel2.setTextColor(getThemeAttrColor(textColor))
    }
    private fun isMyNotificationListenerServiceRunning(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains(context.packageName) == true
    }


    private fun refreshStatus(){
        val time = SpUtils.getLong("time_heart",0)
        val reason = SpUtils.getString("reason","尚未配置数据")
        if(!isMyNotificationListenerServiceRunning(requireContext())) restartNotification()
        if(SpUtils.getInt("heart",0)==0 || !isMyNotificationListenerServiceRunning(requireContext()) ){
            setActive2(getString(R.string.heart_not_work,reason,PushUtils.convertTimestampToDateTime(time)),com.google.android.material.R.attr.colorErrorContainer,com.google.android.material.R.attr.colorOnErrorContainer, R.drawable.ic_error)
        }else{
            setActive2(getString(R.string.heart_work,PushUtils.convertTimestampToDateTime(time)),com.google.android.material.R.attr.colorPrimary,com.google.android.material.R.attr.colorOnPrimary,R.drawable.ic_success)
        }
    }
    private fun restartNotification() {
        val pm = requireActivity().packageManager
        //先禁用
        pm.setComponentEnabledSetting(
            ComponentName(requireActivity(), NotificationService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        //再启用，重新触发rebind
        pm.setComponentEnabledSetting(
            ComponentName(
                requireActivity(),
                NotificationService::class.java
            ),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }




    /**
     * 显示信息
     */
    private fun showMsg(msg:String) {
        Toast.makeText(requireActivity(), msg, Toast.LENGTH_LONG).show()
    }
}