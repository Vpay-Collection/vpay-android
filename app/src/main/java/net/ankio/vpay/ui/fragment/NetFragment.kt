package net.ankio.vpay.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import net.ankio.vpay.R
import net.ankio.vpay.databinding.FragmentNetBinding
import net.ankio.vpay.utils.Logger
import net.ankio.vpay.utils.SpUtils

class NetFragment:Fragment() {

    private lateinit var binding: FragmentNetBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNetBinding.inflate(layoutInflater)
        val web = binding.webview
        val webSettings = web.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        val host = SpUtils.getString("host","https://pay.ankio.net")


        // 启用Cookie支持
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(web, true)




        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
//                Logger.d("WebView", "Error: ${error?.errorCode} ${error?.description}",requireActivity())
            }
        }
        web.loadUrl("$host/admin")
        return binding.root
    }
    // 保存Cookie到本地

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<ScrollView>(R.id.scrollView)?.let { scrollView ->
            view.post {
                val layoutParams = binding.webview.layoutParams
                layoutParams.height = scrollView.height
                binding.webview.layoutParams = layoutParams
            }
        }


    }

}