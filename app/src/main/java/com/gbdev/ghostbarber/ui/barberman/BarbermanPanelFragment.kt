package com.gbdev.ghostbarber.ui.barberman

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gbdev.ghostbarber.R
import com.gbdev.ghostbarber.databinding.FragmentBarbermanPanelBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.NumberFormat
import java.util.*
import kotlin.random.Random

class BarbermanPanelFragment : Fragment() {

    private var _binding: FragmentBarbermanPanelBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val barbersCollection = firestore.collection("barbers")
    private val storage = FirebaseStorage.getInstance().reference

    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var phoneNumberTextView: TextView
    private lateinit var servicesAdapter: ServicesAdapter
    private var currentProfileImageUrl: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBarbermanPanelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileImageView = binding.profileImageView
        nameTextView = binding.nameTextView
        ratingTextView = binding.ratingTextView
        phoneNumberTextView = binding.phoneNumberTextView

        binding.buttonBackToProfile.setOnClickListener {
            findNavController().navigate(R.id.navigation_profile)
        }

        binding.buttonBackToBooking.setOnClickListener {
            findNavController().navigate(R.id.bookFragment)
        }

        binding.addServiceButton.setOnClickListener {
            showAddServiceDialog()
        }

        setupServicesRecyclerView()
        loadUserData()
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        usersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("username") ?: ""
                    val phoneNumber = document.getString("phoneNumber") ?: ""
                    val profileImageUrl = document.getString("profilePictureUrl") ?: ""

                    nameTextView.text = name
                    phoneNumberTextView.text = phoneNumber
                    if (profileImageUrl.isNotEmpty()) {
                        currentProfileImageUrl = profileImageUrl
                        Glide.with(this)
                            .load(profileImageUrl)
                            .circleCrop()
                            .into(profileImageView)
                    }

                    loadBarberData(userId, name, phoneNumber, profileImageUrl)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadBarberData(userId: String, name: String, phoneNumber: String, profileImageUrl: String) {
        barbersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                val rating = document.getDouble("rating") ?: generateAndSaveRating(userId)
                val customers = document.getLong("customers")?.toInt() ?: generateAndSaveCustomers(userId)

                ratingTextView.text = "Rating: %.1f/5 (%d customers)".format(rating, customers)
                saveBarberData(userId, name, phoneNumber, profileImageUrl, rating, customers)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load barber data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateAndSaveRating(userId: String): Double {
        val rating = Random.nextDouble(4.0, 5.0) // Generates a rating between 4.0 and 5.0
        barbersCollection.document(userId).update("rating", rating)
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save rating: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        return rating
    }

    private fun generateAndSaveCustomers(userId: String): Int {
        val customers = Random.nextInt(600, 1001) // Generates a customer count between 600 and 1000
        barbersCollection.document(userId).update("customers", customers)
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save customer count: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        return customers
    }

    private fun saveBarberData(userId: String, name: String, phoneNumber: String, profileImageUrl: String, rating: Double, customers: Int) {
        val barberData = hashMapOf(
            "name" to name,
            "phoneNumber" to phoneNumber,
            "profileImageUrl" to profileImageUrl,
            "rating" to rating,
            "customers" to customers
        )

        barbersCollection.document(userId).set(barberData)
            .addOnSuccessListener {
                loadServices(userId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save barber info: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupServicesRecyclerView() {
        servicesAdapter = ServicesAdapter { service, action ->
            when (action) {
                "edit" -> showEditServiceDialog(service)
                "delete" -> deleteService(service)
            }
        }
        binding.servicesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.servicesRecyclerView.adapter = servicesAdapter
    }

    private fun loadServices(userId: String) {
        barbersCollection.document(userId).collection("services").get()
            .addOnSuccessListener { querySnapshot ->
                val services = querySnapshot.documents.map { document ->
                    val category = document.getString("category") ?: ""
                    val serviceName = document.getString("serviceName") ?: ""
                    val price = document.getDouble("price") ?: 0.0
                    val serviceId = document.id

                    Service(category, serviceName, price, serviceId)
                }
                servicesAdapter.submitList(services)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load services: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddServiceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_service, null)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val serviceNameEditText = dialogView.findViewById<EditText>(R.id.serviceNameEditText)
        val priceEditText = dialogView.findViewById<EditText>(R.id.priceEditText)

        val categories = resources.getStringArray(R.array.service_categories).toList()
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.custom_spinner_item, // Use custom spinner item layout
            categories
        )
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item) // Use custom dropdown item layout
        categorySpinner.adapter = adapter

        priceEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                priceEditText.removeTextChangedListener(this)
                try {
                    val value = s.toString().replace(",", "")
                    val formattedValue = NumberFormat.getNumberInstance(Locale.US).format(value.toDouble())
                    priceEditText.setText(formattedValue)
                    priceEditText.setSelection(formattedValue.length)
                } catch (e: NumberFormatException) {
                    // handle error
                }
                priceEditText.addTextChangedListener(this)
            }
        })

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Service")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val category = if (categorySpinner.selectedItemPosition == 0) "" else categorySpinner.selectedItem.toString()
                val serviceName = serviceNameEditText.text.toString()
                val price = priceEditText.text.toString().replace("[,]".toRegex(), "").toDoubleOrNull() ?: 0.0
                if (category.isNotEmpty()) {
                    addService(category, serviceName, price)
                } else {
                    Toast.makeText(requireContext(), "Please choose a service category", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addService(category: String, serviceName: String, price: Double) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val serviceData = hashMapOf(
            "category" to category,
            "serviceName" to serviceName,
            "price" to price
        )

        barbersCollection.document(userId).collection("services").add(serviceData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Service added", Toast.LENGTH_SHORT).show()
                loadServices(userId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to add service: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditServiceDialog(service: Service) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_service, null)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val serviceNameEditText = dialogView.findViewById<EditText>(R.id.serviceNameEditText)
        val priceEditText = dialogView.findViewById<EditText>(R.id.priceEditText)

        // Populate fields with existing service data
        serviceNameEditText.setText(service.serviceName)
        priceEditText.setText(NumberFormat.getNumberInstance(Locale.US).format(service.price))

        val categories = resources.getStringArray(R.array.service_categories).toList()
        val adapter = HintAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
        categorySpinner.setSelection(categories.indexOf(service.category))

        priceEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                priceEditText.removeTextChangedListener(this)
                try {
                    val value = s.toString().replace(",", "")
                    val formattedValue = NumberFormat.getNumberInstance(Locale.US).format(value.toDouble())
                    priceEditText.setText(formattedValue)
                    priceEditText.setSelection(formattedValue.length)
                } catch (e: NumberFormatException) {
                    // handle error
                }
                priceEditText.addTextChangedListener(this)
            }
        })

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Update Service")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val category = if (categorySpinner.selectedItemPosition == 0) "" else categorySpinner.selectedItem.toString()
                val serviceName = serviceNameEditText.text.toString()
                val price = priceEditText.text.toString().replace("[,]".toRegex(), "").toDoubleOrNull() ?: 0.0
                if (category.isNotEmpty()) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton
                    barbersCollection.document(userId).collection("services").document(service.serviceId)
                        .update(mapOf(
                            "category" to category,
                            "serviceName" to serviceName,
                            "price" to price
                        ))
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Service updated", Toast.LENGTH_SHORT).show()
                            loadServices(userId)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Failed to update service: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Please choose a service category", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteService(service: Service) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        barbersCollection.document(userId).collection("services").document(service.serviceId).delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Service deleted", Toast.LENGTH_SHORT).show()
                loadServices(userId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to delete service: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    data class Service(val category: String, val serviceName: String, val price: Double, val serviceId: String)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // HintAdapter class to handle spinner hint item
    class HintAdapter(context: Context, textViewResourceId: Int, private val objects: List<String>) :
        ArrayAdapter<String>(context, textViewResourceId, objects) {

        override fun isEnabled(position: Int): Boolean {
            // Disable the first item from Spinner
            // First item will be used for hint
            return position != 0
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent) as TextView
            if (position == 0) {
                // Set the hint text color
                view.setTextColor(Color.GRAY)
            } else {
                view.setTextColor(Color.BLACK)
            }
            return view
        }
    }
}
