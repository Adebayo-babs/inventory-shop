package com.example.inventory.utils

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.databinding.FragmentEachCatalogBinding

class ProductAdapter( private var list: MutableList<ProductUtil>) :
        RecyclerView.Adapter<ProductAdapter.ProductViewHolder>(){

    private var listener: ProductAdapterClicksInterface? = null
    fun setListener(listener:ProductAdapterClicksInterface) {
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

    inner class ProductViewHolder(val binding: FragmentEachCatalogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = FragmentEachCatalogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val productData = list[position]
        with(holder.binding) {
            productNameTv.text = productData.productName
            quantityTv.text = productData.quantity
            productPriceTv.text = productData.sellingPrice

            eachCatalog.setOnClickListener {
                listener?.onReadProductBtnClicked(productData)
            }

            deleteBtn.setOnClickListener {
                listener?.onDeleteProductBtnClicked(productData)
            }

            editTask.setOnClickListener {
                listener?.onEditProductBtnClicked(productData)
            }
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    interface ProductAdapterClicksInterface{
        fun onDeleteProductBtnClicked(productData: ProductUtil)
        fun onEditProductBtnClicked(productData: ProductUtil)

        fun onReadProductBtnClicked(productData: ProductUtil)
    }
}