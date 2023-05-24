package net.ankio.vpay.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import net.ankio.vpay.databinding.FragmentLogBinding
import net.ankio.vpay.utils.Logger

class LogFragment:Fragment() {
    private lateinit var binding: FragmentLogBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val data = Logger.readLog(requireContext())
        binding.logTextView.text = data
    }
}