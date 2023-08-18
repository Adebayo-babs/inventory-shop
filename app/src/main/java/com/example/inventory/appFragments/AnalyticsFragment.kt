package com.example.inventory.appFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.ContactsContract.Data
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.inventory.R
import com.example.inventory.databinding.FragmentAnalyticsBinding
import com.example.inventory.utils.AnalyticsAdapter
import com.example.inventory.utils.ProductAdapter
import com.example.inventory.utils.ProductData
import com.example.inventory.utils.ProductUtil
import com.example.inventory.utils.SalesData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale


class AnalyticsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentAnalyticsBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var parentDatabaseReference: DatabaseReference
    private lateinit var soldProductRef: DatabaseReference
    private lateinit var mList:MutableList<SalesData>
    private lateinit var adapter: AnalyticsAdapter
    private lateinit var myRecyclerList: MutableList<SalesData>
    private var totalCostOfProductSold : Double = 0.0
    private var totalRevenue : Double = 0.0
    private var grossProfitMargin : Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNavigation()

//        databaseReference = FirebaseDatabase.getInstance().reference
//            .child("Products").child(FirebaseAuth.getInstance().currentUser?.uid.toString())
        auth = FirebaseAuth.getInstance()
//        databaseReference = FirebaseDatabase.getInstance().reference
//            .child("Products").child(FirebaseAuth.getInstance().currentUser?.uid.toString())
        parentDatabaseReference = FirebaseDatabase.getInstance().reference
            .child("Products").child(auth.currentUser?.uid.toString())
        databaseReference = parentDatabaseReference.child("Shop Products")

        soldProductRef = parentDatabaseReference.child("SoldProducts")

        mList = mutableListOf()
        adapter = AnalyticsAdapter(mList)
        binding.analyticsRv.adapter = adapter
        getDataFromFirebase()
        myRecyclerList = arrayListOf()

    }


    private fun getDataFromFirebase() {
        soldProductRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                totalCostOfProductSold = 0.0
                totalRevenue = 0.0
                grossProfitMargin = 0.0
                for (productSnapshot in snapshot.children) {
                    val productName = productSnapshot.child("productName").getValue(String::class.java)
                    val productDescription = productSnapshot.child("productDescription").getValue(String::class.java)
                    val costPrice = productSnapshot.child("costPrice").getValue(String::class.java)
                    val sellingPrice = productSnapshot.child("sellingPrice").getValue(Double::class.java)
                    val quantity = productSnapshot.child("quantitySold").getValue(String::class.java)
                    val soldDate = productSnapshot.child("soldDate").getValue(String::class.java)
                    val productId = productSnapshot.key

                    val productData = SalesData(
                        productId,
                        productName,
                        productDescription,
                        costPrice,
                        sellingPrice,
                        quantity,
                        soldDate
                    )
                    mList.add(productData)
                    totalCostOfProductSold += costPrice?.toDouble() ?: 0.0
                    totalRevenue += (sellingPrice!!.toDouble())
                    grossProfitMargin = ((totalRevenue - totalCostOfProductSold)/totalRevenue) * 100

                }
                adapter.setData(mList)
                binding.apply {
                    viewPageCostPrice.text = totalCostOfProductSold.toString()
                    viewPageSellingPrice.text = totalRevenue.toString()
                    viewPageGrossProfit.text = String.format(Locale.getDefault(), "%.2f%%", grossProfitMargin)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun bottomNavigation() {
        binding.apply {
            homeIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_analyticsFragment2_to_dashboardFragment2)
            }

            sellIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_analyticsFragment2_to_sellFragment2)
            }

            catalogIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_analyticsFragment2_to_catalogFragment2)
            }

            profileIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_analyticsFragment2_to_profileFragment2)
            }
        }
    }


}