package net.ankio.vpay.ui.fragment

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.quickersilver.themeengine.ThemeChooserDialogBuilder
import com.quickersilver.themeengine.ThemeEngine
import com.quickersilver.themeengine.ThemeMode
import net.ankio.vpay.R
import net.ankio.vpay.databinding.FragmentSettingBinding
import net.ankio.vpay.utils.SpUtils
class SettingFragment:Fragment() {
    private lateinit var binding: FragmentSettingBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(layoutInflater)
        initView()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    private fun initView(){
        //匿名分析
        SpUtils.getBoolean("app_center_analyze",true).apply { binding.analyze.isChecked = this }
        binding.AnalyzeView.setOnClickListener {
            binding.analyze.isChecked = !binding.analyze.isChecked
            SpUtils.putBoolean("app_center_analyze",binding.analyze.isChecked )
        }
        binding.analyze.setOnCheckedChangeListener { _, isChecked ->  SpUtils.putBoolean("app_center_analyze",isChecked ) }
        //语言配置


        //主题
        val color = ThemeEngine.getInstance(requireContext()).staticTheme.primaryColor

        binding.colorSwitch.setCardBackgroundColor(requireActivity().getColor(color))

        binding.settingTheme.setOnClickListener {
            ThemeChooserDialogBuilder(requireContext())
                .setTitle(R.string.choose_theme)
                .setPositiveButton(getString(R.string.ok)) { _, theme ->
                    ThemeEngine.getInstance(requireContext()).staticTheme = theme
                    recreateInit()
                }
                .setNegativeButton(getString(R.string.close))
                .setNeutralButton(getString(R.string.default_theme)) { _, _ ->
                    ThemeEngine.getInstance(requireContext()).resetTheme()
                    recreateInit()
                }
                .setIcon(R.drawable.ic_theme)
                .create()
                .show()
        }

        val stringList = arrayListOf(getString(R.string.always_off),getString(R.string.always_on),getString(R.string.lang_follow_system))

        binding.settingDarkTheme.text = when(ThemeEngine.getInstance(requireContext()).themeMode){
            ThemeMode.DARK -> stringList[1]
            ThemeMode.LIGHT -> stringList[0]
            else  -> stringList[2]
        }

        val listPopupThemeWindow = ListPopupWindow(requireContext(), null)

        listPopupThemeWindow.anchorView =  binding.settingDarkTheme

        listPopupThemeWindow.setAdapter(ArrayAdapter(requireContext(), R.layout.list_popup_window_item,  stringList))
        listPopupThemeWindow.width = WindowManager.LayoutParams.WRAP_CONTENT

        listPopupThemeWindow.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            binding.settingDarkTheme.text = stringList[position]
            ThemeEngine.getInstance(requireContext()).themeMode = when(position){
                1 -> ThemeMode.DARK
                0 -> ThemeMode.LIGHT
                else -> ThemeMode.AUTO
            }
            listPopupThemeWindow.dismiss()
        }

        binding.settingDark.setOnClickListener{ listPopupThemeWindow.show() }


        binding.alwaysDark.isChecked = ThemeEngine.getInstance(requireContext()).isTrueBlack
        binding.settingUseDarkTheme.setOnClickListener {
            ThemeEngine.getInstance(requireContext()).isTrueBlack = !binding.alwaysDark.isChecked
            binding.alwaysDark.isChecked = !binding.alwaysDark.isChecked
        }
        binding.alwaysDark.setOnCheckedChangeListener { _, isChecked ->  ThemeEngine.getInstance(requireContext()).isTrueBlack =  isChecked //;recreateInit()
        }
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S){
            binding.settingUseSystemTheme.visibility = View.GONE
        }

        fun isUseSystemColor(systemColor:Boolean){
            if (systemColor){
                binding.settingTheme.visibility = View.GONE
            }else{
                binding.settingTheme.visibility = View.VISIBLE
            }
        }

        binding.systemColor.isChecked = ThemeEngine.getInstance(requireContext()).isDynamicTheme
        isUseSystemColor(binding.systemColor.isChecked)
        binding.settingUseSystemTheme.setOnClickListener {
            ThemeEngine.getInstance(requireContext()).isDynamicTheme = !binding.alwaysDark.isChecked
            binding.alwaysDark.isChecked = !binding.alwaysDark.isChecked
        }
        binding.systemColor.setOnCheckedChangeListener { _, isChecked ->
            isUseSystemColor(isChecked)
            ThemeEngine.getInstance(requireContext()).isDynamicTheme = isChecked
            recreateInit()
        }


    }

    /**
     * 页面重新初始化
     */
    fun recreateInit() {
        activity?.recreate()
    }

}