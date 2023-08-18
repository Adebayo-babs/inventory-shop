package com.example.inventory.appFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.inventory.R
import com.example.inventory.databinding.FragmentSalesBinding


class SalesFragment : Fragment() {

    private lateinit var binding: FragmentSalesBinding

    private lateinit var listener : DialogNextBtnClickListener

    fun setListener(listener : DialogNextBtnClickListener){
        this.listener = listener
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    interface DialogNextBtnClickListener{

    }


}