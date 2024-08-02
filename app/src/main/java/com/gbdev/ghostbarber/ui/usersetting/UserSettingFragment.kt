package com.gbdev.ghostbarber.ui.usersetting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gbdev.ghostbarber.R
import com.gbdev.ghostbarber.databinding.FragmentUserSettingBinding
import com.gbdev.ghostbarber.models.User
import com.gbdev.ghostbarber.ui.admin.AdminPanelActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


class UserSettingFragment : Fragment() {

    private var _binding: FragmentUserSettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var buttonAdminPanel: Button
    private lateinit var buttonSellerPanel: Button
    private lateinit var buttonLogout: ImageButton
    private lateinit var buttonToBarbermanPanel: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        buttonAdminPanel = binding.buttonAdminPanel
        buttonSellerPanel = binding.buttonSellerPanel
        buttonLogout = binding.buttonLogout
        buttonToBarbermanPanel = binding.buttonToBarbermanPanel

        buttonAdminPanel.visibility = View.GONE
        buttonSellerPanel.visibility = View.GONE
        buttonToBarbermanPanel.visibility = View.GONE

        val userId = auth.currentUser?.uid ?: return
        checkUserRoles(userId)

        buttonAdminPanel.setOnClickListener {
            val intent = Intent(requireContext(), AdminPanelActivity::class.java)
            startActivity(intent)
        }

        buttonLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Logout") { _, _ -> performLogout() }
                .show()
        }

        binding.backToHomeButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun performLogout() {
        auth.signOut()
        findNavController().navigate(R.id.loginFragment)
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    private fun checkUserRoles(userId: String) {
        val userDoc = firestore.collection("users").document(userId)
        userDoc.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val isAdmin = document.getBoolean("isAdmin") ?: false
                Log.d("UserSettingFragment", "User is admin: $isAdmin")

                buttonAdminPanel.visibility = if (isAdmin) View.VISIBLE else View.GONE
            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Failed to check roles: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("UserSettingFragment", "Error checking roles", e)
        }
    }
}

