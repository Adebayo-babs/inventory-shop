package com.example.inventory.appFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.inventory.databinding.FragmentAddBinding
import com.example.inventory.utils.ProductData
import com.example.inventory.utils.ProductUtil
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson


class AddFragment : DialogFragment() {

    private lateinit var binding: FragmentAddBinding
    private lateinit var listener : DialogNextBtnClickListener
    private var productData: ProductUtil? = null

    fun setListener(listener : DialogNextBtnClickListener){
        this.listener = listener
    }

    companion object {
        const val ARG_PRODUCT_DATA = "productData"

        fun newInstance(productData: ProductUtil): AddFragment {
            val fragment = AddFragment()
            val args = Bundle().apply {
                putParcelable(ARG_PRODUCT_DATA, productData)
            }
            fragment.arguments = args
            return fragment
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NORMAL, R.style.AddFragmentStyle)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments
        if (args != null && args.containsKey(ARG_PRODUCT_DATA)) {
            productData = args.getParcelable(ARG_PRODUCT_DATA)
//            val productDataJson = args.getString(ARG_PRODUCT_DATA)
//            val productData = Gson().fromJson(productDataJson, ProductData::class.java)

            // Retrieve the TextInputEditText views from the binding
            val etProductName = binding.etProductName
            val etProductDescription = binding.etProductDescription
            val etCostPrice = binding.etCostPrice
            val etSellingPrice = binding.etSellingPrice
            val etQuantity = binding.etQuantity

            // Populate the UI fields with the product details
            etProductName.setText(productData?.productName)
            etProductDescription.setText(productData?.productDescription)
            etCostPrice.setText(productData?.costPrice)
            etSellingPrice.setText(productData?.sellingPrice)
            etQuantity.setText(productData?.quantity)
        }
        registerEvents()
    }

    private fun isProductNameExists(productName: String, callback: (Boolean) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("Products")
            .child(auth.currentUser?.uid.toString())
            .orderByChild("productName")
            .equalTo(productName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot.exists())
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }


    @SuppressLint("SetTextI18n")
    private fun registerEvents() {
        binding.addProduct.setOnClickListener {
            val nProductName = binding.etProductName.text.toString()
            val nProductDescription = binding.etProductDescription.text.toString()
            val nCostPrice = binding.etCostPrice.text.toString()
            val nSellingPrice = binding.etSellingPrice.text.toString()
            val nQuantity = binding.etQuantity.text.toString()

            if (nProductName.isNotEmpty() && nProductDescription.isNotEmpty() && nCostPrice.isNotEmpty() && nSellingPrice.isNotEmpty() && nQuantity.isNotEmpty()) {
                if (productData == null) {
                    isProductNameExists(nProductName) { exists ->
                        if (exists) {
                            // Inform the user that the product already exists
                            Toast.makeText(context, "Product already added", Toast.LENGTH_SHORT).show()
                        }else{
                            listener.onSaveProduct(
                                nProductName, binding.etProductName,
                                nProductDescription, binding.etProductDescription,
                                nCostPrice, binding.etCostPrice,
                                nSellingPrice, binding.etSellingPrice,
                                nQuantity, binding.etQuantity
                            )
                        }
                    }
                }else {
                    productData!!.productId
                    productData?.productName = nProductName
                    productData?.productDescription = nProductDescription
                    productData?.costPrice = nCostPrice
                    productData?.sellingPrice = nSellingPrice
                    productData?.quantity = nQuantity

                    listener.onUpdateProduct(
                        productData!!,
                        binding.etProductName,
                        binding.etProductDescription,
                        binding.etCostPrice,
                        binding.etSellingPrice,
                        binding.etQuantity
                    )
                }
            }else {
                Toast.makeText(context, "Fill in empty fields", Toast.LENGTH_SHORT).show()
            }

//            if (nProductName.isNotEmpty() && nProductDescription.isNotEmpty() && nCostPrice.isNotEmpty() && nSellingPrice.isNotEmpty() && nQuantity.isNotEmpty()) {
//                isProductNameExists(nProductName) { exists ->
//                    if (exists) {
//                        // Inform the user that the product already exists
//                        Toast.makeText(context, "Product already added", Toast.LENGTH_SHORT).show()
//                    } else {
//                        if (productData == null) {
//                            listener.onSaveProduct(
//                                nProductName, binding.etProductName,
//                                nProductDescription, binding.etProductDescription,
//                                nCostPrice, binding.etCostPrice,
//                                nSellingPrice, binding.etSellingPrice,
//                                nQuantity, binding.etQuantity
//                            )
//
//                        } else {
//                            productData!!.productId
//                            productData?.productName = nProductName
//                            productData?.productDescription = nProductDescription
//                            productData?.costPrice = nCostPrice
//                            productData?.sellingPrice = nSellingPrice
//                            productData?.quantity = nQuantity
//
//                            listener.onUpdateProduct(
//                                productData!!,
//                                binding.etProductName,
//                                binding.etProductDescription,
//                                binding.etCostPrice,
//                                binding.etSellingPrice,
//                                binding.etQuantity
//                            )
//                        }
//
//                    }
//                }
//            }else {
//                Toast.makeText(context, "Fill in empty fields", Toast.LENGTH_SHORT).show()
//            }
        }
        binding.calculateEquity.setOnClickListener {

            val productName = binding.etProductName.text.toString()
            val costPrice = binding.etCostPrice.text.toString().toFloatOrNull()
            val sellingPrice = binding.etSellingPrice.text.toString().toFloatOrNull()

            if (costPrice != null && sellingPrice != null && productName.isNotEmpty()) {
                val result = (sellingPrice.times(100).div(costPrice)).minus(100)
                val finalResult = String.format("%.2f", result)
                binding.resultText.text = "You are making $finalResult% profit per $productName sold"
            }else{
                Toast.makeText(context, "Fill in empty fields with valid inputs", Toast.LENGTH_SHORT).show()
            }
        }
    }



    interface DialogNextBtnClickListener {
        fun onSaveProduct(
            product: String,
            etProductName: TextInputEditText,
            productDesc: String,
            etProductDescription: TextInputEditText,
            productCost: String,
            etCostPrice: TextInputEditText,
            productSell: String,
            etSellingPrice: TextInputEditText,
            productQuan: String,
            etQuantity: TextInputEditText
        )

        fun onUpdateProduct(
            productData: ProductUtil,
            etProductName: TextInputEditText,
            etProductDescription: TextInputEditText,
            etCostPrice: TextInputEditText,
            etSellingPrice: TextInputEditText,
            etQuantity: TextInputEditText
        )

    }


}