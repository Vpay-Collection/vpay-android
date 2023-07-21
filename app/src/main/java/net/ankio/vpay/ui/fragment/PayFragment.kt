package net.ankio.vpay.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.ankio.vpay.databinding.FragmentPayBinding
import net.ankio.vpay.ui.MyAdapter
import net.ankio.vpay.utils.PayUtils


class PayFragment: Fragment()  {
    private lateinit var binding: FragmentPayBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPayBinding.inflate(layoutInflater)

        val list  = PayUtils.list()
        list.reverse()
        binding.recyclerView.adapter = MyAdapter(list)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        return binding.root
    }

}