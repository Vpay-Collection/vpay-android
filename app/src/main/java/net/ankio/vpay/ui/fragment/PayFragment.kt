package net.ankio.vpay.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import net.ankio.vpay.databinding.FragmentPayBinding

class PayFragment: Fragment()  {
    private lateinit var binding: FragmentPayBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPayBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()


    }
}