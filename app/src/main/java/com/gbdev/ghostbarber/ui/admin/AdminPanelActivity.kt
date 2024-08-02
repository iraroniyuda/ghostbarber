package com.gbdev.ghostbarber.ui.admin

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gbdev.ghostbarber.databinding.ActivityAdminPanelBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.view.View

class AdminPanelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPanelBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userEmails: MutableList<String>
    private lateinit var userEmailToUidMap: MutableMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        userEmails = mutableListOf()
        userEmailToUidMap = mutableMapOf()

        setupUserEmailSpinner()
        fetchUsers()

        binding.spinnerUserEmails.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedEmail = parent.getItemAtPosition(position).toString()
                if (selectedEmail.isNotEmpty()) {
                    fetchUserRoles(selectedEmail)
                } else {
                    clearAndDisableCheckboxes()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                clearAndDisableCheckboxes()
            }
        }

        binding.buttonUpdateRoles.setOnClickListener {
            val selectedEmail = binding.spinnerUserEmails.selectedItem?.toString() ?: ""
            val userId = userEmailToUidMap[selectedEmail] ?: return@setOnClickListener
            val roles = hashMapOf(
                "barber" to binding.checkboxBarber.isChecked,
                "seller" to binding.checkboxSeller.isChecked,
                "investor" to binding.checkboxInvestor.isChecked,
                "owner" to binding.checkboxOwner.isChecked,
                "creator" to binding.checkboxCreator.isChecked
            )
            updateUserRoles(userId, roles)
        }

        binding.buttonBackToProfile.setOnClickListener {
            finish()
        }
    }

    private fun fetchUsers() {
        firestore.collection("users").get()
            .addOnSuccessListener { result ->
                userEmails.clear()
                userEmailToUidMap.clear()
                userEmails.add("")
                for (document in result) {
                    val email = document.getString("email")
                    val uid = document.id
                    if (email != null) {
                        userEmails.add(email)
                        userEmailToUidMap[email] = uid
                    }
                }
                setupUserEmailSpinner()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch users: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupUserEmailSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userEmails)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUserEmails.adapter = adapter
    }

    private fun fetchUserRoles(email: String) {
        val userId = userEmailToUidMap[email] ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val roles = document.data?.get("roles") as? Map<String, Boolean> ?: return@addOnSuccessListener
                binding.checkboxBarber.isChecked = roles["barber"] ?: false
                binding.checkboxSeller.isChecked = roles["seller"] ?: false
                binding.checkboxInvestor.isChecked = roles["investor"] ?: false
                binding.checkboxOwner.isChecked = roles["owner"] ?: false
                binding.checkboxCreator.isChecked = roles["creator"] ?: false

                roles.forEach { (role, isChecked) ->
                    when (role) {
                        "barber" -> binding.checkboxBarber.isEnabled = !isChecked
                        "seller" -> binding.checkboxSeller.isEnabled = !isChecked
                        "investor" -> binding.checkboxInvestor.isEnabled = !isChecked
                        "owner" -> binding.checkboxOwner.isEnabled = !isChecked
                        "creator" -> binding.checkboxCreator.isEnabled = !isChecked
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch user roles: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserRoles(userId: String, roles: Map<String, Boolean>) {
        val userDoc = firestore.collection("users").document(userId)
        firestore.runTransaction { transaction ->
            val userSnapshot = transaction.get(userDoc)
            if (userSnapshot.exists()) {
                val currentRoles = userSnapshot.get("roles") as Map<String, Boolean>
                val username = userSnapshot.getString("username") ?: return@runTransaction

                val updatedRoles = currentRoles.toMutableMap()
                updatedRoles.putAll(roles)

                val updates = mutableMapOf<String, Any>("roles" to updatedRoles)

                if (roles["barber"] == true && !currentRoles["barber"]!!) {
                    // Update username with the title if barber role is newly granted
                    if (!username.startsWith("Mr. Drac ")) {
                        val updatedUsername = "Mr. Drac $username"
                        updates["username"] = updatedUsername
                    }
                }

                transaction.update(userDoc, updates)
            }
        }.addOnSuccessListener {
            if (roles["barber"] == true) {
                updateBarberRequestStatus(userId, "accepted")
            }
            Toast.makeText(this, "User roles updated successfully", Toast.LENGTH_SHORT).show()
            resetUI()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to update user roles: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBarberRequestStatus(userId: String, status: String) {
        firestore.collection("barber_requests").document(userId)
            .update("status", status)
            .addOnSuccessListener {
                Toast.makeText(this, "Barber request status updated to $status", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update barber request status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearAndDisableCheckboxes() {
        binding.checkboxBarber.isChecked = false
        binding.checkboxSeller.isChecked = false
        binding.checkboxInvestor.isChecked = false
        binding.checkboxOwner.isChecked = false
        binding.checkboxCreator.isChecked = false

        binding.checkboxBarber.isEnabled = false
        binding.checkboxSeller.isEnabled = false
        binding.checkboxInvestor.isEnabled = false
        binding.checkboxOwner.isEnabled = false
        binding.checkboxCreator.isEnabled = false
    }

    private fun resetUI() {
        binding.spinnerUserEmails.setSelection(0)
        clearAndDisableCheckboxes()
    }
}
