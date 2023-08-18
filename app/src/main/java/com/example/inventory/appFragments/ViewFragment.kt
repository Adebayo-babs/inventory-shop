package com.example.inventory.appFragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.example.inventory.R
import com.example.inventory.databinding.FragmentViewBinding
import com.example.inventory.utils.ProductData
import com.example.inventory.utils.ProductUtil
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale


class ViewFragment : Fragment() {
    private lateinit var binding: FragmentViewBinding
    private lateinit var databaseReference: DatabaseReference

    companion object {
        private const val ARG_PRODUCT_DATA = "productData"

        fun newInstance(productData: ProductUtil): ViewFragment {
            val fragment = ViewFragment()
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
        binding = FragmentViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("CommitTransaction", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("Products")
            .child(auth.currentUser?.uid.toString())

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val backStackCount = childFragmentManager.backStackEntryCount
                    if (backStackCount > 0) {
                        childFragmentManager.popBackStack()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }

            })

        val args = arguments
        if (args != null && args.containsKey(ARG_PRODUCT_DATA)) {
            val productData = args.getParcelable<ProductUtil>(ARG_PRODUCT_DATA)

            if (productData != null) {
                getProductDataFromFirebase(productData.productName ?: "") {
                    binding.viewPageProductName.text = productData.productName
                    binding.viewPageDescription.text = productData.productDescription
                    binding.viewPageCP.text = productData.costPrice
                    binding.viewPageSP.text = productData.sellingPrice
                    binding.viewPageQty.text = productData.quantity
                    binding.viewPageProfit.text = (productData.sellingPrice!!.toInt() - productData.costPrice!!.toInt()).toString()
//                    val result = (sellingPrice.times(100).div(costPrice)).minus(100)
//                    val finalResult = String.format("%.2f", result)
//                    binding.resultText.text = "You are making $finalResult% profit per $productName sold"
                    val sp = (productData.sellingPrice!!.toInt() * 100)
                    val cp = (productData.costPrice!!.toInt())
                    val total = sp/cp
                    val result = (total - 100).toString()
                    binding.viewPagePercentProfit.text = "$result%"

//                    binding.viewPagePercentProfit.text = "$finalResult%"
                }
            }
        }

    }

    private fun getProductDataFromFirebase(productName: String, callback: (ProductUtil?) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val databaseReference = FirebaseDatabase.getInstance().reference
        val query = databaseReference.child("Products")
            .child(auth.currentUser?.uid.toString())
            .orderByChild("productName")
            .equalTo(productName)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (productSnapshot in snapshot.children) {
                        val productData = productSnapshot.getValue(ProductUtil::class.java)
                        callback(productData)
                        return
                    }
                }
                callback(null)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                callback(null)
            }
        })
    }
}