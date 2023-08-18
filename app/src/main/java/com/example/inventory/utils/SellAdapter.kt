package com.example.inventory.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.databinding.FragmentEachCatalogBinding
import com.example.inventory.databinding.SellNewBinding

class SellAdapter(private var list: MutableList<ProductUtil>):
    RecyclerView.Adapter<SellAdapter.SellViewHolder>(){

    private var listener: SellAdapterClicksInterface? = null
    fun setListener(listener: SellAdapterClicksInterface) {
        this.listener = listener
    }
    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<ProductUtil>) {
        //list.clear()
//        list = data.toMutableList()
//        list.addAll(data)
//        Log.d("ProductAdapter", "Data set. Size: ${list.size}")
        list = data.toMutableList()
        notifyDataSetChanged()
    }

    inner class SellViewHolder(val binding: SellNewBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellAdapter.SellViewHolder {
        val binding = SellNewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SellViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SellAdapter.SellViewHolder, position: Int) {
        val productData = list[position]
        with(holder.binding) {
            productNameTv.text = productData.productName
            quantityTv.text = productData.quantity
            productPriceTv.text = productData.sellingPrice

            forEachCatalog.setOnClickListener {
                listener?.onReadSellBtnClicked(productData)
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface SellAdapterClicksInterface{
        fun onReadSellBtnClicked(productData: ProductUtil)
    }

}