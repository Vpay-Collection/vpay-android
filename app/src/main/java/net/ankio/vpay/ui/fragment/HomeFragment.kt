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
import net.ankio.vpay.R
import net.ankio.vpay.databinding.FragmentHomeBinding
import net.ankio.vpay.service.NotificationService
import net.ankio.vpay.utils.Logger
import net.ankio.vpay.utils.SpUtils
import org.json.JSONException
import org.json.JSONObject


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
                        showMsg("配置成功，重启监听服务...");
                        viewInit()
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
        viewInit()
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
    fun getThemeAttrColor( @AttrRes attrResId: Int): Int {
        return MaterialColors.getColor(ContextThemeWrapper(requireContext(), ThemeEngine.getInstance(requireContext()).getTheme()), attrResId, Color.WHITE)
    }
    /**
     * 设置插件状态
     */
    private fun setActive(@StringRes text: Int, @AttrRes backgroundColor:Int, @AttrRes textColor:Int, @DrawableRes drawable:Int){
        binding.active.setBackgroundColor(getThemeAttrColor(backgroundColor))
        binding.imageView.setImageDrawable(
            AppCompatResources.getDrawable(
                requireActivity(),
                drawable
            )
        )
        binding.msgLabel.text = getString(text)
        binding.imageView.setColorFilter(getThemeAttrColor(textColor))
        binding.msgLabel.setTextColor(getThemeAttrColor(textColor))
    }
    private fun isMyNotificationListenerServiceRunning(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains(context.packageName) == true
    }

    private fun refreshStatus(){
        if(!isMyNotificationListenerServiceRunning(requireContext())){//判断服务是否运行
            setActive(R.string.not_work,com.google.android.material.R.attr.colorErrorContainer,com.google.android.material.R.attr.colorOnErrorContainer, R.drawable.ic_error)
        }else{
            setActive(R.string.server_working,com.google.android.material.R.attr.colorPrimary,com.google.android.material.R.attr.colorOnPrimary,R.drawable.ic_success)
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


    fun viewInit() {
        binding.url.setText(SpUtils.getString("host"))
        binding.key.setText(SpUtils.getString("key"))
        restartNotification()
        refreshStatus()
    }


    /**
     * 显示信息
     */
    fun showMsg(msg:String) {
        Toast.makeText(requireActivity(), msg, Toast.LENGTH_LONG).show()
    }
}