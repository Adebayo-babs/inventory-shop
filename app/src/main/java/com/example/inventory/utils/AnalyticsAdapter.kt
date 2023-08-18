package com.example.inventory.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.databinding.FragmentSalesCatalogBinding
import com.example.inventory.databinding.SellNewBinding

class AnalyticsAdapter(private var list: MutableList<SalesData>):
    RecyclerView.Adapter<AnalyticsAdapter.AnalyticsViewHolder>(){


    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<SalesData>) {
        list = data.toMutableList()
        notifyDataSetChanged()
    }

    inner class AnalyticsViewHolder(val binding: FragmentSalesCatalogBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalyticsAdapter.AnalyticsViewHolder {
        val binding = FragmentSalesCatalogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnalyticsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnalyticsAdapter.AnalyticsViewHolder, position: Int) {
        val productData = list[position]
        with(holder.binding) {
            productDateTv.text = productData.soldDate
            nameTv.text = productData.productName
            quantityTv.text = productData.quantitySold
            productPriceTv.text = productData.sellingPrice.toString()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}