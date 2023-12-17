package net.ankio.vpay.ui


import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zackratos.ultimatebarx.ultimatebarx.addNavigationBarBottomPadding
import net.ankio.vpay.App
import net.ankio.vpay.R
import net.ankio.vpay.databinding.AboutDialogBinding
import net.ankio.vpay.databinding.ActivityMainBinding
import net.ankio.vpay.utils.Logger
import rikka.html.text.toHtml


class MainActivity : BaseActivity() {

    //视图绑定
    private lateinit var binding: ActivityMainBinding

    private lateinit var fragmentContainerView: FragmentContainerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "MainActivity"
     //   Logger.empty(this)
        Logger.d(tag, "--------- beginning of session", this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

       fragmentContainerView = binding.navHostFragment
        bottomNavigationView = binding.bottomNavigation

        toolbarLayout = binding.toolbarLayout
        toolbar = binding.toolbar
        bottomNavigationView.addNavigationBarBottomPadding()
        scrollView = binding.scrollView
        navHostFragment = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?)!!

        NavigationUI.setupWithNavController(bottomNavigationView, navHostFragment.navController)

        // 添加 Navigate 跳转监听，如果参数不带 ShowAppBar 将不显示底部导航栏
        navHostFragment.navController.addOnDestinationChangedListener { _, navDestination, _ ->
            when (navDestination.id) {
                R.id.homeFragment -> {
                  toolbar.title = getString(R.string.title_home)
                }
                R.id.logFragment -> {
                    toolbar.title = getString(R.string.title_log)
                }
                R.id.settingFragment -> {
                    toolbar.title = getString(R.string.title_setting)
                }
                R.id.netFragment -> {
                    toolbar.title = getString(R.string.title_net)
                }

                R.id.payFragment->{
                    toolbar.title = getString(R.string.title_pay)
                }

            }
        }

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.more -> {
                    val binding = AboutDialogBinding.inflate(LayoutInflater.from(this), null, false)
                    binding.sourceCode.movementMethod = LinkMovementMethod.getInstance()
                    binding.sourceCode.text = getString(
                        R.string.about_view_source_code,
                        "<b><a href=\"https://github.com/Vpay-Collection/vpay-android/\n\">GitHub</a></b>"
                    ).toHtml()

                    binding.versionName.text = packageManager.getPackageInfo(packageName, 0).versionName
                    MaterialAlertDialogBuilder(this)
                        .setView(binding.root)
                        .show()
                    true
                }
                else -> false
            }

        }
        onViewCreated()
        if (!App.isNotificationAccessibilityServiceEnabled(this)) {
            Toast.makeText(this,R.string.tips,Toast.LENGTH_LONG).show()
            App.openAccessibilitySettings(this)
        }
    }

    private fun getNavController(): NavController {
        return navHostFragment.navController
    }


    override fun onDestroy() {

        Logger.d(tag, "--------- ending of session", this)
        super.onDestroy()

    }

  }