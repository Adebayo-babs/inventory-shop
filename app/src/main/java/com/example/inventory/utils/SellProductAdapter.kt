package com.example.inventory.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.databinding.FragmentSellPageBinding

class SellProductAdapter(private var list: MutableList<ProductUtil>):
    RecyclerView.Adapter<SellProductAdapter.SellProductViewHolder>() {


    private var listener: SellProductAdapterClicksInterface? = null
    fun setListener(listener: SellProductAdapterClicksInterface) {
        this.listener = listener
    }


    inner class SellProductViewHolder(val binding: FragmentSellPageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellProductAdapter.SellProductViewHolder {
        val binding = FragmentSellPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SellProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SellProductAdapter.SellProductViewHolder, position: Int) {
        val productData = list[position]
        with(holder.binding) {
            sellPageSellListName.text = productData.productName
            sellPageSellListQty.text = productData.quantity
            sellPageSellListPrice.text = productData.sellingPrice

            deleteSellBtn.setOnClickListener {
                listener?.onDeleteSellProductBtnClicked(productData, productData.productId!!, productData.quantity!!)
            }

            sellEditCatalog.setOnClickListener {
                listener?.onEditSellProductBtnClicked(productData, productData.productId!!, productData.quantity!!)
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface SellProductAdapterClicksInterface{
        fun onDeleteSellProductBtnClicked(productData: ProductUtil,productid: String, newQuantity:String)
        fun onEditSellProductBtnClicked(productData: ProductUtil,productid: String, newQuantity:String)

    }


}