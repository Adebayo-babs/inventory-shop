package com.example.inventory.appFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.inventory.R
import com.example.inventory.databinding.FragmentDashboardBinding
import com.example.inventory.utils.AnalyticsAdapter
import com.example.inventory.utils.ProductData
import com.example.inventory.utils.ProductUtil
import com.example.inventory.utils.SalesData
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DashboardFragment : Fragment(), AddFragment.DialogNextBtnClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var parentDatabaseReference: DatabaseReference
    private lateinit var soldProductRef: DatabaseReference
    private lateinit var mList:MutableList<SalesData>
    private lateinit var adapter: AnalyticsAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        databaseReference = FirebaseDatabase.getInstance().reference
//            .child("Products").child(FirebaseAuth.getInstance().currentUser?.uid.toString())
        auth = FirebaseAuth.getInstance()

        parentDatabaseReference = FirebaseDatabase.getInstance().reference
            .child("Products").child(auth.currentUser?.uid.toString())
        databaseReference = parentDatabaseReference.child("Shop Products")

        soldProductRef = parentDatabaseReference.child("SoldProducts")
        mList = mutableListOf()
        adapter = AnalyticsAdapter(mList)
        registerEvents()
        getDataFromFirebasePieChart()

        bottomNavigation()
//        binding.pieButton.setOnClickListener {
//            if (!isPieChartDisplayed) {
//                clearBarChart()
//                getDataFromFirebasePieChart() // Display the pie chart
//                isPieChartDisplayed = true
//                binding.pieButton.isEnabled = false
//                binding.barButton.isEnabled = true
//            }
//        }
//        binding.pieButton.setOnClickListener {
//                clearBarChart()
//                getDataFromFirebasePieChart() // Display the pie chart
//        }
//
//        binding.barButton.setOnClickListener {
//                clearPieChart()
//                getDataFromFirebaseBarChart() // Display the bar chart
//        }

//        binding.barButton.setOnClickListener {
//            if (isPieChartDisplayed) {
//                clearPieChart()
//                getDataFromFirebaseBarChart() // Display the bar chart
//                isPieChartDisplayed = false
//                binding.pieButton.isEnabled = true
//                binding.barButton.isEnabled = false
//            }
//        }
    }

//    private fun clearBarChart() {
//        binding.barChart.clear()
//    }
//
//    private fun clearPieChart() {
//        binding.pieChart.clear()
//    }

    private fun registerEvents() {
        binding.addProduct.setOnClickListener {
//            val fragmentManager = requireActivity().supportFragmentManager
            val addFragment = AddFragment()
            addFragment.setListener(this)
            addFragment.show(
                childFragmentManager,
                AddFragment.ARG_PRODUCT_DATA
            )
//            fragmentManager.beginTransaction()
//                .replace(R.id.dashboardsFragment, addFragment)
//                .addToBackStack(null)
//                .commit()
        }
    }

    private fun getDataFromFirebasePieChart() {
        soldProductRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                val salesData = mutableListOf<PieEntry>()

                for (productSnapshot in snapshot.children) {
                    val productName = productSnapshot.child("productName").getValue(String::class.java)
                    val quantitySold = productSnapshot.child("quantitySold").getValue(String::class.java)
                    val productId = productSnapshot.key

                    if (productName != null && quantitySold != null) {
                        salesData.add(PieEntry(quantitySold.toFloat(), productName))
                    }
                }
                val dataSet = PieDataSet(salesData, "Sales Distribution")
                dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

                // Create a new PieData object with the dataset
                val pieData = PieData(dataSet)

                // Get a reference to the PieChart view from the layout
                val pieChart = binding.pieChart
                // Set the PieData to the PieChart view
                pieChart.data = pieData
                // Customize the chart appearance
                pieChart.description.isEnabled = false // Disable chart description
                pieChart.setDrawEntryLabels(true) // Show labels on the chart
                pieChart.setEntryLabelTextSize(12f)
                pieChart.legend.isEnabled = true // Show legend

                // Refresh the chart
                pieChart.invalidate()
                adapter.setData(mList)

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getDataFromFirebaseBarChart() {
        soldProductRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                val salesData = mutableListOf<BarEntry>()
                var index = 0f

                for (productSnapshot in snapshot.children) {
                    val productName = productSnapshot.child("productName").getValue(String::class.java)
                    val quantitySold = productSnapshot.child("quantitySold").getValue(String::class.java)
                    val productId = productSnapshot.key

                    if (productName != null && quantitySold != null) {
                        salesData.add(BarEntry(index, quantitySold.toFloat(), productName))
                        index++
                    }
                }
                val dataSet = BarDataSet(salesData, "Sales Distribution")
                dataSet.colors = ColorTemplate.COLORFUL_COLORS.asList()

                // Create a new BarData object with the dataset
                val barData = BarData(dataSet)

                // Get a reference to the BarChart view from the layout
                val barChart = binding.barChart
                // Set the BarData to the BarChart view
                barChart.data = barData
                // Customize the chart appearance
                barChart.description.isEnabled = false // Disable chart description
                barChart.setDrawValueAboveBar(true) // Show values above the bars
                barChart.legend.isEnabled = true // Show legend
                barChart.xAxis.setDrawGridLines(false) // Hide vertical grid lines

                // Refresh the chart
                barChart.invalidate()
                adapter.setData(mList)

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }


    override fun onSaveProduct(
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
    ) {
        val productName = etProductName.text.toString()
        val productDescription = etProductDescription.text.toString()
        val costPrice = etCostPrice.text.toString()
        val sellingPrice = etSellingPrice.text.toString()
        val quantity = etQuantity.text.toString()

        val productData = ProductData(
            productName,
            productDescription,
            costPrice,
            sellingPrice,
            quantity
        )
        val auth = FirebaseAuth.getInstance()
        val databaseReference = FirebaseDatabase.getInstance().reference

        // Generate a new unique key using push()
        val productKey = databaseReference.child("Products")
            .child(auth.currentUser?.uid.toString())
            .child("Shop Products") // Child node within the user's data
            .push().key

        if (productKey != null) {
            databaseReference.child("Products")
                .child(auth.currentUser?.uid.toString())
                .child("Shop Products")
                .child(productKey)
                .setValue(productData)
                .addOnSuccessListener {
                    // Product data saved successfully
                    Toast.makeText(context, "Product added successfully", Toast.LENGTH_SHORT).show()
                    etProductName.text?.clear()
                    etProductDescription.text?.clear()
                    etCostPrice.text?.clear()
                    etSellingPrice.text?.clear()
                    etQuantity.text?.clear()
                }
                .addOnFailureListener {
                    // Failed to save product data
                    Toast.makeText(context, "Failed to add product", Toast.LENGTH_SHORT).show()
                }
        }

    }

    override fun onUpdateProduct(
        productData: ProductUtil,
        etProductName: TextInputEditText,
        etProductDescription: TextInputEditText,
        etCostPrice: TextInputEditText,
        etSellingPrice: TextInputEditText,
        etQuantity: TextInputEditText
    ) {
        val productName = etProductName.text.toString()
        if (productName.isNotEmpty()) {
            val updatedData = mutableMapOf<String, Any?>(
                "productName" to productName,
                "productDescription" to etProductDescription.text.toString(),
                "costPrice" to etCostPrice.text.toString(),
                "sellingPrice" to etSellingPrice.text.toString(),
                "quantity" to etQuantity.text.toString()
            )

            val auth = FirebaseAuth.getInstance()
            val databaseReference = FirebaseDatabase.getInstance().reference

            // Use the product key directly to update the data
            databaseReference.child("Products")
                .child(auth.currentUser?.uid.toString())
                .child(productData.productId ?: "")
                .updateChildren(updatedData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to update product", Toast.LENGTH_SHORT).show()
                    }
                    etProductName.text = null
                    etProductDescription.text = null
                    etCostPrice.text = null
                    etSellingPrice.text = null
                    etQuantity.text = null

                }
        }
    }

    private fun bottomNavigation() {

        binding.apply {
            sellIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_dashboardFragment2_to_sellFragment2)
            }

            catalogIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_dashboardFragment2_to_catalogFragment2)
            }

            analyticIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_dashboardFragment2_to_analyticsFragment2)
            }

            profileIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_dashboardFragment2_to_profileFragment2)
            }
        }

    }


}