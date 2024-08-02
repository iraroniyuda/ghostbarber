package com.gbdev.ghostbarber.ui.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gbdev.ghostbarber.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MessageFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var notificationsContainer: LinearLayout
    private lateinit var adminMessage: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        notificationsContainer = view.findViewById(R.id.notificationsContainer)
        adminMessage = view.findViewById(R.id.adminMessage)
        checkAdminStatus()
        return view
    }

    private fun checkAdminStatus() {
        val userId = auth.currentUser?.uid ?: return
        val userDoc = firestore.collection("users").document(userId)
        userDoc.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val isAdmin = document.getBoolean("isAdmin") ?: false
                if (isAdmin) {
                    loadNotifications()
                } else {
                    adminMessage.visibility = View.VISIBLE
                }
            } else {
                adminMessage.visibility = View.VISIBLE
            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Failed to check admin status: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadNotifications() {
        firestore.collection("barber_requests")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.forEach { document ->
                    val userId = document.getString("userId") ?: return@forEach
                    val email = document.getString("email") ?: "unknown"

                    val requestView = LayoutInflater.from(context).inflate(R.layout.item_barber_request, null) as LinearLayout
                    val emailTextView = requestView.findViewById<TextView>(R.id.emailTextView)
                    val approveButton = requestView.findViewById<Button>(R.id.approveButton)
                    val rejectButton = requestView.findViewById<Button>(R.id.rejectButton)

                    emailTextView.text = "$email just registered as a barber and needs confirmation."

                    approveButton.setOnClickListener {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Confirm Approval")
                            .setMessage("Are you sure you want to approve this request?")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Yes") { _, _ -> updateBarberRequest(userId, true) }
                            .show()
                    }

                    rejectButton.setOnClickListener {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Confirm Rejection")
                            .setMessage("Are you sure you want to reject this request?")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Yes") { _, _ -> updateBarberRequest(userId, false) }
                            .show()
                    }

                    notificationsContainer.addView(requestView)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to load notifications: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateBarberRequest(userId: String, isApproved: Boolean) {
        val status = if (isApproved) "accepted" else "rejected"

        firestore.collection("barber_requests").document(userId).update("status", status)
            .addOnSuccessListener {
                if (isApproved) {
                    firestore.collection("users").document(userId).update("roles.barber", true)
                        .addOnSuccessListener {
                            Toast.makeText(context, "User has been approved as a barber.", Toast.LENGTH_SHORT).show()
                            notificationsContainer.removeAllViews()
                            loadNotifications()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to update user role: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "User request has been rejected.", Toast.LENGTH_SHORT).show()
                    notificationsContainer.removeAllViews()
                    loadNotifications()
                    firestore.collection("barber_requests").document(userId).delete()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to update request: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
