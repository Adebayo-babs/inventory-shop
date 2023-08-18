@file:Suppress("SameParameterValue")

package com.example.inventory.appFragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventory.HomeActivity
import com.example.inventory.MainActivity
import com.example.inventory.R
import com.example.inventory.databinding.FragmentSellBinding
import com.example.inventory.utils.ProductUtil
import com.example.inventory.utils.SalesData
import com.example.inventory.utils.SellAdapter
import com.example.inventory.utils.SellProductAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION", "NAME_SHADOWING")
class SellFragment : Fragment(), SellAdapter.SellAdapterClicksInterface, SellProductAdapter.SellProductAdapterClicksInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var parentDatabaseReference: DatabaseReference
    private lateinit var soldProductRef: DatabaseReference
    private lateinit var binding: FragmentSellBinding
    private var currentQuery: String? = null
    private lateinit var mList:MutableList<ProductUtil>
    private lateinit var myRecyclerList: MutableList<ProductUtil>
    private var totalPriceOfProductToBeSold : Double = 0.0
    private lateinit var sellPageAdapter: SellProductAdapter
//    private var sellPageAdapter: SellAdapter? = null
    private lateinit var filteredList: MutableList<ProductUtil>
    private lateinit var adapter: SellAdapter

    private fun handleKeyboardVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.GONE else View.VISIBLE
        (activity as MainActivity).setBottomSellNavigationVisibility(visibility)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSellBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getDataFromFirebase()
        registerEvents()
        bottomNavigation()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val backStackCount = childFragmentManager.backStackEntryCount
                if (backStackCount > 0) {
                    childFragmentManager.popBackStack()
                }else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }

        })

        binding.sellPageBtn.setOnClickListener {
            sellAllProducts()
        }

        binding.searchViewProd.setOnClickListener{
            handleKeyboardVisibility(true)
        }


        binding.searchViewProd.setOnQueryTextFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                (activity as MainActivity).setBottomSellNavigationVisibility(View.GONE)
            }else {
                (activity as MainActivity).setBottomSellNavigationVisibility(View.VISIBLE)
            }
        }

        binding.sellPageAddToSell.setOnClickListener {
            addToSellRecyclerView()
        }

    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
//        databaseReference = FirebaseDatabase.getInstance().reference
//            .child("Products").child(FirebaseAuth.getInstance().currentUser?.uid.toString())
        parentDatabaseReference = FirebaseDatabase.getInstance().reference
            .child("Products").child(auth.currentUser?.uid.toString())
        databaseReference = parentDatabaseReference.child("Shop Products")

        soldProductRef = parentDatabaseReference.child("SoldProducts")

        binding.newSellRv.setHasFixedSize(true)
        binding.newSellRv.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()
        filteredList = mutableListOf()

        myRecyclerList = arrayListOf()

        // After initializing the adapter
        sellPageAdapter = SellProductAdapter(myRecyclerList)
        sellPageAdapter.setListener(this) // Set the click listener here
        binding.sellPageRecyclerView.adapter = sellPageAdapter

        adapter = SellAdapter(filteredList)
        adapter.setListener(this)
        binding.newSellRv.adapter = adapter

    }

    private fun registerEvents() {
        binding.searchViewProd.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchViewProd.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText?.toLowerCase(Locale.getDefault())
                filterList()
                return false
            }

        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterList() {
        filteredList.clear()
        if (currentQuery!!.isNotEmpty()) {
            binding.newSellRv.visibility = View.VISIBLE
            for (productData in mList) {
                if (productData.productName?.toLowerCase(Locale.getDefault())
                        ?.contains(currentQuery!!) == true
                ) {
                    filteredList.add(productData)
                }
            }
        }

        adapter.setData(filteredList)

    }

    private fun getDataFromFirebase() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (productSnapshot in snapshot.children) {
                    val productName = productSnapshot.child("productName").getValue(String::class.java)
                    val productDescription = productSnapshot.child("productDescription").getValue(String::class.java)
                    val costPrice = productSnapshot.child("costPrice").getValue(String::class.java)
                    val sellingPrice = productSnapshot.child("sellingPrice").getValue(String::class.java)
                    val quantity = productSnapshot.child("quantity").getValue(String::class.java)
                    val productId = productSnapshot.key

                    val productData = ProductUtil(
                        productId,
                        productName,
                        productDescription,
                        costPrice,
                        sellingPrice,
                        quantity
                    )
                    mList.add(productData)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }



    @SuppressLint("NotifyDataSetChanged")
    private fun addToSellRecyclerView(isSelling: Boolean = true) {
        binding.apply {
            if (sellPageProduct.text != null) {
                val selectedProductName = sellPageProduct.text.toString()
                val selectedQuantity = qtyField.text.toString().toInt()

                // Check if the product already exists in the list
                val existingProduct = myRecyclerList.find { it.productName == selectedProductName }

                // Find the original product details
                val originalProduct = mList.find { it.productName == selectedProductName }

                if (originalProduct != null) {
                    val originalSellingPrice = originalProduct.sellingPrice?.toDouble() ?: 0.0

                    if (existingProduct != null) {
                        // Update the quantity of the existing product
                        existingProduct.quantity = (existingProduct.quantity!!.toInt() + selectedQuantity).toString()

                        if (isSelling) {
                            // Update the selling price based on the new quantity
                            existingProduct.sellingPrice = (originalSellingPrice * existingProduct.quantity!!.toDouble()).toString()
                            totalPriceOfProductToBeSold += originalSellingPrice * selectedQuantity
                            Toast.makeText(context, "$selectedQuantity $selectedProductName has been added to the Sell list", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // Add the new product to the list
                        myRecyclerList.add(
                            ProductUtil(
                                originalProduct.productId,
                                originalProduct.productName,
                                originalProduct.productDescription,
                                originalProduct.costPrice,
                                (originalSellingPrice * selectedQuantity).toString(),
                                selectedQuantity.toString()
                            )
                        )

                        if (isSelling) {
                            // Update the total price for selling
                            totalPriceOfProductToBeSold += originalSellingPrice * selectedQuantity
                            Toast.makeText(context, "$selectedQuantity $selectedProductName has been added to the Sell list", Toast.LENGTH_LONG).show()
                        }
                    }

                    // Make sure the sellPageAdapter is initialized before calling notifyDataSetChanged()
                    if (::sellPageAdapter.isInitialized) {
                        sellPageAdapter.notifyDataSetChanged()
                    } else {
                        // Initialize the adapter and set it to the RecyclerView
                        sellPageAdapter = SellProductAdapter(myRecyclerList)
                        binding.sellPageRecyclerView.adapter = sellPageAdapter
                        binding.sellPageRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    }

                    // Update the total price TextView
                    binding.sellPageTotalPrice.text = String.format(Locale.getDefault(), "Total Price: %.2f", totalPriceOfProductToBeSold)

                    // Clear the fields
                    setFieldsToNull()
                }
            } else {
                Toast.makeText(context, "No valid product selected", Toast.LENGTH_LONG).show()
            }
        }
    }


//    private fun addToSellRecyclerView(isSelling: Boolean = true) {
//        binding.apply {
//            if (sellPageProduct.text != null) {
//                for (eachProduct in mList) {
//                    if (sellPageProduct.text.toString() == eachProduct.productName) {
//                        if (qtyField.text.toString().isNotEmpty()) {
//                            if (eachProduct.quantity!!.isNotEmpty() && qtyField.text.toString().toInt() <= eachProduct.quantity!!.toInt()) {
//                                if (myRecyclerList.isEmpty()) {
//                                    val originalQuantity = eachProduct.quantity!!.toInt()
//                                    myRecyclerList.add(
//                                        ProductUtil(
//                                            eachProduct.productId,
//                                            eachProduct.productName,
//                                            eachProduct.productDescription,
//                                            eachProduct.costPrice,
//                                            (qtyField.text.toString().toInt() * eachProduct.sellingPrice?.toDouble()!!).toString(),
//                                            qtyField.text.toString()
//                                        )
//                                    )
//                                    if (isSelling) {
//                                        val newQuantity = (originalQuantity - qtyField.text.toString().toInt()).toString()
//                                        updateProductQuantity(eachProduct.productId!!, newQuantity)
//                                        totalPriceOfProductToBeSold += qtyField.text.toString().toInt() * eachProduct.sellingPrice!!.toDouble()
//                                        Toast.makeText(context, "${qtyField.text.toString().toInt()}  ${ eachProduct.productName} has been added to the Sell list", Toast.LENGTH_LONG).show()
//
//                                    }else{
//                                        val newQuantity = (originalQuantity + qtyField.text.toString().toInt()).toString()
//                                        updateProductQuantity(eachProduct.productId!!, newQuantity)
//                                    }
//
//                                } else {
//                                    var isProductAlreadyAdded = false
//                                    for (listData in myRecyclerList) {
//                                        if (listData.productName == eachProduct.productName) {
//                                            Toast.makeText(context, "${eachProduct.productName} has already been added to the Sell list. Tap to edit ", Toast.LENGTH_LONG).show()
//
////                                            isProductAlreadyAdded = true
////                                            Toast.makeText(context, "${eachProduct.productName} has already been added to the Sell list. Tap to edit ", Toast.LENGTH_LONG).show()
////                                            break
//                                        }
//                                    }
//                                    if (!isProductAlreadyAdded) {
//                                        myRecyclerList.add(
//                                            ProductUtil(
//                                                eachProduct.productId,
//                                                eachProduct.productName,
//                                                eachProduct.productDescription,
//                                                eachProduct.costPrice,
//                                                (qtyField.text.toString().toInt() * eachProduct.sellingPrice!!.toDouble()).toString(), qtyField.text.toString()
//                                            )
//                                        )
//                                        totalPriceOfProductToBeSold += qtyField.text.toString().toInt() * eachProduct.sellingPrice!!.toDouble()
//
//                                        val newQuantity = (eachProduct.quantity!!.toInt() - qtyField.text.toString().toInt()).toString()
//                                        updateProductQuantity(eachProduct.productId!!, newQuantity)
//                                    }
//                                }
//                            } else {
//                                Toast.makeText(context, "You only have ${eachProduct.quantity}  ${eachProduct.productName}  left", Toast.LENGTH_LONG).show()
//                            }
//                        }else {
//                            Toast.makeText(context, "Enter the quantity of ${eachProduct.productName} to sell", Toast.LENGTH_LONG).show()
//                        }
//                        break
//                    }
//                }
//            }else {
//                Toast.makeText(context, "No valid product selected", Toast.LENGTH_LONG).show()
//            }
//        }
//
//        // Make sure the sellPageAdapter is initialized before calling notifyDataSetChanged()
//        if (::sellPageAdapter.isInitialized) {
//            sellPageAdapter.notifyDataSetChanged()
//        } else {
//            // Initialize the adapter and set it to the RecyclerView
//            sellPageAdapter = SellProductAdapter(myRecyclerList)
//            binding.sellPageRecyclerView.adapter = sellPageAdapter
//            binding.sellPageRecyclerView.layoutManager = LinearLayoutManager(this.context)
//        }
//
//        // Update the total price TextView
//        binding.sellPageTotalPrice.text = String.format(Locale.getDefault(), "Total Price: %.2f", totalPriceOfProductToBeSold)
//
//        // Clear the fields
//        setFieldsToNull()
//    }


    private fun setFieldsToNull() {
        binding.apply {
            sellPageProduct.text = null
            qtyField.text = null
            sellPageQty.text = null
        }
    }

    private fun updateProductQuantity(productid: String, newQuantity:String) {
        val productRef = databaseReference.child(productid).child("quantity")
        productRef.setValue(newQuantity)
            .addOnSuccessListener {
                Toast.makeText(context, "Quantity has been updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onReadSellBtnClicked(productData: ProductUtil) {
        binding.newSellRv.visibility = View.GONE
        binding.sellPageProduct.text = productData.productName
        binding.sellPageQty.text = productData.quantity
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDeleteSellProductBtnClicked(productData: ProductUtil, productid: String, newQuantity: String) {

        // Ask for confirmation to delete the product
        val productName = productData.productName
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete $productName?")
            .setPositiveButton("Delete") { _, _ ->
                for (eachProduct in mList) {
                    if (eachProduct.productName == productData.productName) {
//                        val updatedQuantity = (eachProduct.quantity!!.toInt()).toString()
//                        updateProductQuantity(productid, updatedQuantity)
                        break
                    }
                }

                myRecyclerList.remove(productData)
                // Notify the adapter about the changes
                sellPageAdapter.notifyDataSetChanged()

                val updatedTotalPrice = totalPriceOfProductToBeSold - productData.sellingPrice!!.toDouble()
                totalPriceOfProductToBeSold = updatedTotalPrice

                binding.sellPageTotalPrice.text =
                    String.format(Locale.getDefault(), "Total Price: %.2f", totalPriceOfProductToBeSold)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

//    override fun onDeleteSellProductBtnClicked(productData: ProductUtil, productid: String, newQuantity: String) {
////        Log.d("SellFragment", "Delete button clicked for product: ${productData.productName}")
//        // Ask for confirmation to delete the product
//        val productName = productData.productName
//        AlertDialog.Builder(requireContext())
//            .setTitle("Delete Product")
//            .setMessage("Are you sure you want to delete $productName?")
//            .setPositiveButton("Delete") { _, _ ->
//
//                for (eachProduct in mList) {
//                    if (eachProduct.productName == productData.productName) {
////                        Log.d("SellFragment", "You have these number of products left in your database: ${eachProduct.quantity}")
////                        Log.d("SellFragment", "You are to sell these number of products: ${productData.quantity}")
//                        val updatedQuantity = (eachProduct.quantity!!.toInt() + productData.quantity!!.toInt()).toString()
//                        updateProductQuantity(productid, updatedQuantity)
//
//                        // totalPriceOfProductToBeSold -= (productData.sellingPrice!!.toDouble() * productData.quantity!!.toInt())
//
//                    }
//                }
//
//                myRecyclerList.remove(productData)
//                // Notify the adapter about the changes
//                sellPageAdapter.notifyDataSetChanged()
//                val updatedTotalPrice = totalPriceOfProductToBeSold - (productData.sellingPrice!!.toDouble() * productData.quantity!!.toInt())
//                totalPriceOfProductToBeSold = updatedTotalPrice
//
//                binding.sellPageTotalPrice.text =
//                    String.format(Locale.getDefault(), "Total Price: %.2f", totalPriceOfProductToBeSold)
//
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onEditSellProductBtnClicked(productData: ProductUtil, productid: String, newQuantity: String) {
        for (eachProduct in mList) {
            if (eachProduct.productName == productData.productName) {
//                val updatedQuantity = (eachProduct.quantity!!.toInt()).toString()
//                updateProductQuantity(productid, updatedQuantity)

                binding.sellPageProduct.text = eachProduct.productName
                binding.sellPageQty.text = (eachProduct.quantity!!.toInt()).toString()
                binding.qtyField.setText( productData.quantity!!.toInt().toString())


            }
        }

        myRecyclerList.remove(productData)
        // Notify the adapter about the changes
        sellPageAdapter.notifyDataSetChanged()

        val updatedTotalPrice = totalPriceOfProductToBeSold - productData.sellingPrice!!.toDouble()
        totalPriceOfProductToBeSold = updatedTotalPrice

        binding.sellPageTotalPrice.text =
            String.format(Locale.getDefault(), "Total Price: %.2f", totalPriceOfProductToBeSold)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sellAllProducts() {
        if (myRecyclerList.isEmpty()) {
            Toast.makeText(context, "No product to sell", Toast.LENGTH_LONG).show()
            return
        }

        for (eachItem in myRecyclerList) {
            val originalQuantity = mList.find { it.productName == eachItem.productName }?.quantity?.toInt() ?: 0
            val quantityToSell = eachItem.quantity?.toInt() ?: 0
            val soldProduct = SalesData(
                productId = eachItem.productId,
                productName = eachItem.productName,
                productDescription = eachItem.productDescription,
                costPrice = eachItem.costPrice,
                sellingPrice = eachItem.sellingPrice?.toDouble(),
                quantitySold = eachItem.quantity!!.toString(),
                soldDate = getCurrentDateTime()
            )

            val newSoldProductRef = soldProductRef.push()
            newSoldProductRef.setValue(soldProduct)
                .addOnSuccessListener {
                    Toast.makeText(context, "Product sold successfully", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to sell product", Toast.LENGTH_LONG).show()
                }
            val newQuantity = (originalQuantity - quantityToSell).toString()
            updateProductQuantity(eachItem.productId!!, newQuantity)
        }

        myRecyclerList.clear()
        sellPageAdapter.notifyDataSetChanged()
        totalPriceOfProductToBeSold = 0.00
        binding.sellPageTotalPrice.text = String.format(Locale.getDefault(), "Total Price: %.2f", totalPriceOfProductToBeSold)
        setFieldsToNull()
        Toast.makeText(context, "All products sold successfully!", Toast.LENGTH_LONG).show()
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = Date()
        return sdf.format(currentDate)
    }

    private fun bottomNavigation() {
        binding.apply {
            homeIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_sellFragment2_to_dashboardFragment2)
            }

            catalogIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_sellFragment2_to_catalogFragment2)
            }

            analyticIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_sellFragment2_to_analyticsFragment)
            }

            profileIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_sellFragment2_to_profileFragment2)
            }
        }
    }

}


