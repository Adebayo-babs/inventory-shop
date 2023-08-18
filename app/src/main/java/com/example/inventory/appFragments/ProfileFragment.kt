package com.example.inventory.appFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.inventory.R
import com.example.inventory.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class ProfileFragment : Fragment(), SalesFragment.DialogNextBtnClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var parentDatabaseReference: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: FragmentProfileBinding



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        bottomNavigation()

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            auth.currentUser?.let {
                binding.shopEmail.text = it.email
            }
        }

        binding.logOut.setOnClickListener {
            logout()

        }

    }

    private fun logout() {
            auth.signOut()
        // Clear the back stack
        val navController = findNavController()
//        navController.popBackStack(R.id.signInFragment, true)

        // Navigate to the SignInFragment
        navController.navigate(R.id.action_profileFragment2_to_signInFragment)
        }


    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
//        databaseReference = FirebaseDatabase.getInstance().reference
//            .child("Products").child(auth.currentUser?.uid.toString())
        parentDatabaseReference = FirebaseDatabase.getInstance().reference
            .child("Products").child(auth.currentUser?.uid.toString())
        databaseReference = parentDatabaseReference.child("Shop Products")

    }

    private fun bottomNavigation() {
        binding.apply {
            homeIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment2_to_dashboardFragment2)
            }

            sellIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment2_to_sellFragment2)
            }

            catalogIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment2_to_catalogFragment2)
            }

            analyticIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment2_to_analyticsFragment2)
            }
        }
    }

}



