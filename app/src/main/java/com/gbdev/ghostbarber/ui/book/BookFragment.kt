package com.gbdev.ghostbarber.ui.book

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.gbdev.ghostbarber.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class BookFragment : Fragment() {

    private lateinit var barberRecyclerView: RecyclerView
    private lateinit var bookingInfoLayout: LinearLayout
    private lateinit var bookingOrderIdTextView: TextView
    private lateinit var bookingBarberNameTextView: TextView
    private lateinit var bookingTimeTextView: TextView
    private lateinit var bookingTotalPriceTextView: TextView
    private lateinit var servicesListContainer: LinearLayout
    private lateinit var toggleServicesButton: Button
    private lateinit var cancelOrderButton: Button
    private lateinit var barbermanPanelButton: ImageButton

    private val firestore = FirebaseFirestore.getInstance()
    private val barbersCollection = firestore.collection("barbers")
    private var selectedServices = mutableListOf<Pair<String, Double>>()
    private var selectedDateTime: String? = null
    private var selectedBarberId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book, container, false)
        barberRecyclerView = view.findViewById(R.id.barberRecyclerView)
        barberRecyclerView.layoutManager = LinearLayoutManager(context)
        loadBarbers()
        setHasOptionsMenu(true) // Enable options menu

        bookingInfoLayout = view.findViewById(R.id.bookingInfoLayout)
        bookingOrderIdTextView = view.findViewById(R.id.bookingOrderIdTextView)
        bookingBarberNameTextView = view.findViewById(R.id.bookingBarberNameTextView)
        bookingTimeTextView = view.findViewById(R.id.bookingTimeTextView)
        bookingTotalPriceTextView = view.findViewById(R.id.bookingTotalPriceTextView)
        servicesListContainer = view.findViewById(R.id.servicesListContainer)
        toggleServicesButton = view.findViewById(R.id.toggleServicesButton)
        cancelOrderButton = view.findViewById(R.id.cancelOrderButton)
        barbermanPanelButton = view.findViewById(R.id.barbermanPanelButton)

        toggleServicesButton.setOnClickListener {
            if (servicesListContainer.visibility == View.VISIBLE) {
                servicesListContainer.visibility = View.GONE
                toggleServicesButton.text = "Show Services"
            } else {
                servicesListContainer.visibility = View.VISIBLE
                toggleServicesButton.text = "Hide Services"
            }
        }

        cancelOrderButton.setOnClickListener {
            showCancelConfirmationDialog()
        }

        checkUserRoleAndShowButton(barbermanPanelButton)
        checkActiveBooking()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigation for top navigation bar buttons
        view.findViewById<ImageButton>(R.id.homeButton).setOnClickListener {
            findNavController().navigate(R.id.navigation_home)
        }
        view.findViewById<ImageButton>(R.id.aiHairstyleButton).setOnClickListener {
            findNavController().navigate(R.id.action_bookFragment_to_hairstyleFragment)
        }
        view.findViewById<ImageButton>(R.id.reelsButton).setOnClickListener {
            findNavController().navigate(R.id.navigation_reels)
        }
        view.findViewById<ImageButton>(R.id.ic_booking).setOnClickListener {
            // No action needed, we are already in BookFragment
        }
        view.findViewById<ImageButton>(R.id.profileButton).setOnClickListener {
            findNavController().navigate(R.id.navigation_profile)
        }
        view.findViewById<ImageButton>(R.id.moreButton).setOnClickListener {
            findNavController().navigate(R.id.action_bookFragment_to_moreFragment)
        }

        val backToHomeButton = view.findViewById<ImageButton>(R.id.backToHomeButton)
        backToHomeButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_home)
        }

        barbermanPanelButton.setOnClickListener {
            findNavController().navigate(R.id.action_bookFragment_to_barbermanPanelFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_book_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_back_to_home -> {
                findNavController().navigate(R.id.navigation_home)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadBarbers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        barbersCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val barbers = mutableListOf<Barber>()

                // Process each barber document
                val barberDocuments = querySnapshot.documents
                var processedBarbers = 0

                barberDocuments.forEach { document ->
                    val barberId = document.id
                    if (barberId != currentUserId) {
                        val name = document.getString("name") ?: ""
                        val profileImageUrl = document.getString("profileImageUrl") ?: ""
                        val phoneNumber = document.getString("phoneNumber") ?: ""
                        val rating = document.getDouble("rating") ?: 0.0
                        val customers = document.getLong("customers") ?: 0

                        // Check if the barber has any services
                        barbersCollection.document(barberId).collection("services").get()
                            .addOnSuccessListener { servicesSnapshot ->
                                if (!servicesSnapshot.isEmpty) {
                                    // Add the barber to the list only if they have services
                                    barbers.add(Barber(barberId, name, profileImageUrl, phoneNumber, rating, customers.toInt()))
                                }
                                processedBarbers++
                                if (processedBarbers == barberDocuments.size) {
                                    // Set the adapter once all barbers have been processed
                                    barberRecyclerView.adapter = BarberAdapter(requireContext(), barbers)
                                }
                            }
                            .addOnFailureListener { e ->
                                processedBarbers++
                                if (processedBarbers == barberDocuments.size) {
                                    // Set the adapter once all barbers have been processed
                                    barberRecyclerView.adapter = BarberAdapter(requireContext(), barbers)
                                }
                                Log.e("BookFragment", "Failed to load services: ${e.message}")
                            }
                    } else {
                        processedBarbers++
                        if (processedBarbers == barberDocuments.size) {
                            // Set the adapter once all barbers have been processed
                            barberRecyclerView.adapter = BarberAdapter(requireContext(), barbers)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load barbers: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUserRoleAndShowButton(button: ImageButton) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val roles = document.get("roles") as? Map<String, Boolean>
                if (roles?.get("barber") == true) {
                    button.visibility = View.VISIBLE
                } else {
                    button.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Log.e("BookFragment", "Failed to fetch user roles: ${e.message}")
            }
    }

    private fun checkActiveBooking() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("booking_orders")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    bookingInfoLayout.visibility = View.GONE
                    barberRecyclerView.visibility = View.VISIBLE
                } else {
                    val booking = documents.first()
                    fetchBarberUsernameAndDisplayBookingInfo(booking.id, booking.data)
                    bookingInfoLayout.visibility = View.VISIBLE
                    barberRecyclerView.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to check active booking: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchBarberUsernameAndDisplayBookingInfo(orderId: String, bookingData: Map<String, Any>) {
        val barberId = bookingData["barberId"] as? String ?: return

        firestore.collection("barbers").document(barberId).get()
            .addOnSuccessListener { document ->
                val barberUsername = document.getString("name") ?: "Unknown Barber"
                displayBookingInfo(orderId, bookingData, barberUsername)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to fetch barber username: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayBookingInfo(orderId: String, bookingData: Map<String, Any>, barberUsername: String) {
        val textColor = ContextCompat.getColor(requireContext(), R.color.booking_info_text_color)

        bookingOrderIdTextView.apply {
            text = "Booking Order ID: $orderId"
            setTextColor(textColor)
        }
        bookingBarberNameTextView.apply {
            text = "Barber Name: $barberUsername"
            setTextColor(textColor)
        }
        bookingTimeTextView.apply {
            text = "Booking Time: ${bookingData["bookingTime"]}"
            setTextColor(textColor)
        }
        bookingTotalPriceTextView.apply {
            text = "Total Price: Rp ${String.format("%,.2f", bookingData["totalPrice"] as Double)}"
            setTextColor(textColor)
        }

        val services = bookingData["services"] as List<String>
        servicesListContainer.removeAllViews()
        services.forEach { service ->
            val serviceTextView = TextView(requireContext()).apply {
                text = service
                setTextColor(textColor)
            }
            servicesListContainer.addView(serviceTextView)
        }
    }

    private fun showCancelConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel Booking")
            .setMessage("Are you sure you want to cancel this booking?")
            .setPositiveButton("Yes") { _, _ ->
                cancelBookingOrder()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelBookingOrder() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("booking_orders")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val bookingId = documents.first().id
                    firestore.collection("booking_orders").document(bookingId).delete()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Booking order canceled successfully", Toast.LENGTH_SHORT).show()
                            checkActiveBooking()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Failed to cancel booking order: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to fetch booking orders: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    inner class BarberAdapter(context: Context, private val barbers: List<Barber>) :
        RecyclerView.Adapter<BarberAdapter.BarberViewHolder>() {

        inner class BarberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val barberNameTextView: TextView = view.findViewById(R.id.barberNameTextView)
            val profileImageView: ImageView = view.findViewById(R.id.profileImageView)
            val phoneNumberTextView: TextView = view.findViewById(R.id.phoneNumberTextView)
            val ratingTextView: TextView = view.findViewById(R.id.ratingTextView)
            val servicesContainer: LinearLayout = view.findViewById(R.id.servicesContainer)
            val totalPriceTextView: TextView = view.findViewById(R.id.totalPriceTextView)
            val bookingTimeEditText: EditText = view.findViewById(R.id.bookingTimeEditText)
            val bookButton: Button = view.findViewById(R.id.bookButton)
            val pickDateButton: Button = view.findViewById(R.id.pickDateButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarberViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_barber, parent, false)
            return BarberViewHolder(view)
        }

        override fun onBindViewHolder(holder: BarberViewHolder, position: Int) {
            val barber = barbers[position]
            holder.barberNameTextView.text = barber.name
            Glide.with(holder.itemView.context).load(barber.profileImageUrl).transform(CircleCrop()).into(holder.profileImageView)
            holder.phoneNumberTextView.text = barber.phoneNumber
            holder.ratingTextView.text = "Rating: %.1f/5 (%d customers)".format(barber.rating, barber.customers)

            holder.itemView.setOnClickListener {
                if (holder.servicesContainer.visibility == View.GONE) {
                    expandServices(barber.barberId, holder.servicesContainer, holder.totalPriceTextView, holder.pickDateButton, holder.bookingTimeEditText, holder.bookButton)
                    selectedBarberId = barber.barberId
                } else {
                    collapseServices(holder.servicesContainer, holder.totalPriceTextView, holder.pickDateButton, holder.bookingTimeEditText, holder.bookButton)
                }
            }
        }

        override fun getItemCount(): Int = barbers.size
    }

    private fun expandServices(
        barberId: String?,
        servicesContainer: LinearLayout,
        totalPriceTextView: TextView,
        pickDateButton: Button,
        bookingTimeEditText: EditText,
        bookButton: Button
    ) {
        servicesContainer.visibility = View.VISIBLE

        // Ensure parent container is visible
        val parentContainer = servicesContainer.parent as View
        parentContainer.visibility = View.VISIBLE

        totalPriceTextView.visibility = View.VISIBLE
        pickDateButton.visibility = View.VISIBLE
        bookingTimeEditText.visibility = View.VISIBLE
        bookButton.visibility = View.VISIBLE

        loadServices(barberId, servicesContainer, totalPriceTextView, pickDateButton, bookingTimeEditText, bookButton)
        Log.d("BookFragment", "Services expanded for barberId: $barberId")
    }

    private fun collapseServices(
        servicesContainer: LinearLayout,
        totalPriceTextView: TextView,
        pickDateButton: Button,
        bookingTimeEditText: EditText,
        bookButton: Button
    ) {
        servicesContainer.visibility = View.GONE
        servicesContainer.removeAllViews()
        totalPriceTextView.visibility = View.GONE
        pickDateButton.visibility = View.GONE
        bookingTimeEditText.visibility = View.GONE
        bookButton.visibility = View.GONE
        selectedServices.clear()
        selectedDateTime = null
        selectedBarberId = null

        Log.d("BookFragment", "Services collapsed")
    }

    private fun loadServices(
        barberId: String?,
        servicesContainer: LinearLayout,
        totalPriceTextView: TextView,
        pickDateButton: Button,
        bookingTimeEditText: EditText,
        bookButton: Button
    ) {
        if (barberId == null) return

        barbersCollection.document(barberId).collection("services").get()
            .addOnSuccessListener { querySnapshot ->
                val services = querySnapshot.documents.mapNotNull { document ->
                    val serviceName = document.getString("serviceName")
                    val price = document.getDouble("price")
                    if (serviceName != null && price != null) {
                        Pair(serviceName, price)
                    } else {
                        null
                    }
                }
                services.forEach { service ->
                    val checkBox = CheckBox(requireContext()).apply {
                        text = "${service.first} (Rp ${String.format("%,.2f", service.second)})"
                        setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                selectedServices.add(service)
                            } else {
                                selectedServices.remove(service)
                            }
                            updateTotalPrice(totalPriceTextView)
                        }
                    }
                    servicesContainer.addView(checkBox)
                }

                totalPriceTextView.visibility = View.VISIBLE
                pickDateButton.visibility = View.VISIBLE
                bookingTimeEditText.visibility = View.VISIBLE
                bookButton.visibility = View.VISIBLE

                Log.d("BookFragment", "Services loaded for barberId: $barberId")
                Log.d("BookFragment", "totalPriceTextView visibility: ${totalPriceTextView.visibility}")
                Log.d("BookFragment", "pickDateButton visibility: ${pickDateButton.visibility}")
                Log.d("BookFragment", "bookingTimeEditText visibility: ${bookingTimeEditText.visibility}")
                Log.d("BookFragment", "bookButton visibility: ${bookButton.visibility}")

                // Ensure layout parameters are correctly set
                totalPriceTextView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                pickDateButton.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                bookingTimeEditText.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                bookButton.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                // Explicitly check and remove from parent if necessary
                totalPriceTextView.parent?.let { (it as ViewGroup).removeView(totalPriceTextView) }
                pickDateButton.parent?.let { (it as ViewGroup).removeView(pickDateButton) }
                bookingTimeEditText.parent?.let { (it as ViewGroup).removeView(bookingTimeEditText) }
                bookButton.parent?.let { (it as ViewGroup).removeView(bookButton) }

                servicesContainer.addView(totalPriceTextView)
                servicesContainer.addView(pickDateButton)
                servicesContainer.addView(bookingTimeEditText)
                servicesContainer.addView(bookButton)

                setupDateTimePicker(pickDateButton, bookingTimeEditText, bookButton)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load services: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("BookFragment", "Failed to load services: ${e.message}")
            }
    }

    private fun updateTotalPrice(totalPriceTextView: TextView) {
        val totalPrice = selectedServices.sumOf { it.second }
        totalPriceTextView.text = String.format("Total Price: Rp %,.2f", totalPrice)
        Log.d("BookFragment", "Total price updated: Rp %,.2f".format(totalPrice))
    }

    private fun setupDateTimePicker(pickDateButton: Button, bookingTimeEditText: EditText, bookButton: Button) {
        pickDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val minCalendar = Calendar.getInstance()
            minCalendar.add(Calendar.HOUR, 1)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val timePickerDialog = TimePickerDialog(
                        requireContext(),
                        { _, hourOfDay, minute ->
                            val selectedDate = "$dayOfMonth/${month + 1}/$year"
                            val selectedTime = String.format("%02d:%02d", hourOfDay, minute)

                            val selectedCalendar = Calendar.getInstance().apply {
                                set(year, month, dayOfMonth, hourOfDay, minute)
                            }

                            if (selectedCalendar.before(minCalendar)) {
                                Toast.makeText(requireContext(), "Please select a time at least 1 hour from now", Toast.LENGTH_SHORT).show()
                            } else {
                                selectedDateTime = "$selectedDate $selectedTime"
                                bookingTimeEditText.setText(selectedDateTime)
                                Log.d("BookFragment", "Date and time selected: $selectedDateTime")
                            }
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    )

                    customizeDialog(timePickerDialog)
                    timePickerDialog.show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.datePicker.minDate = minCalendar.timeInMillis

            customizeDialog(datePickerDialog)
            datePickerDialog.show()
        }

        bookButton.setOnClickListener {
            bookService()
        }
    }

    private fun customizeDialog(dialog: Dialog) {
        dialog.setOnShowListener {
            val window = dialog.window
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.darker_gray) // Change background color
                val decorView = window.decorView
                if (decorView is ViewGroup) {
                    setCustomColors(decorView)
                }
            }
        }
    }

    private fun setCustomColors(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            when (child) {
                is ViewGroup -> setCustomColors(child)
                is TextView -> {
                    child.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_text_color))
                }
                is Button -> {
                    child.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_color))
                }
                is DatePicker -> customizeDatePicker(child)
            }
        }
    }

    private fun customizeDatePicker(datePicker: DatePicker) {
        val calendarView = datePicker.getChildAt(0) as ViewGroup

        // Set background color for calendar view
        calendarView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.calendar_background_color))

        // Customize date text color
        val dayPickerView = calendarView.findViewById<View>(Resources.getSystem().getIdentifier("day", "id", "android"))
        val monthPickerView = calendarView.findViewById<View>(Resources.getSystem().getIdentifier("month", "id", "android"))
        val yearPickerView = calendarView.findViewById<View>(Resources.getSystem().getIdentifier("year", "id", "android"))

        if (dayPickerView is ViewGroup) {
            setDatePickerTextColor(dayPickerView, R.color.date_text_color)
        }
        if (monthPickerView is ViewGroup) {
            setDatePickerTextColor(monthPickerView, R.color.date_text_color)
        }
        if (yearPickerView is ViewGroup) {
            setDatePickerTextColor(yearPickerView, R.color.date_text_color)
        }
    }

    private fun setDatePickerTextColor(viewGroup: ViewGroup, colorResId: Int) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is TextView) {
                child.setTextColor(ContextCompat.getColor(requireContext(), colorResId))
            }
            if (child is ViewGroup) {
                setDatePickerTextColor(child, colorResId)
            }
        }
    }

    private fun bookService() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please log in to book a service", Toast.LENGTH_SHORT).show()
            return
        }

        val totalPrice = selectedServices.sumOf { it.second }

        if (selectedDateTime == null || selectedServices.isEmpty() || selectedBarberId == null) {
            Toast.makeText(requireContext(), "Please select services, pick a date & time, and select a barber", Toast.LENGTH_SHORT).show()
            return
        }

        val bookingData = hashMapOf(
            "userId" to currentUser.uid,
            "barberId" to selectedBarberId, // Store the selected barber ID
            "services" to selectedServices.map { it.first },
            "bookingTime" to selectedDateTime,
            "totalPrice" to totalPrice,
            "status" to "pending"
        )

        firestore.collection("booking_orders").add(bookingData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Service booked successfully", Toast.LENGTH_SHORT).show()
                resetPage()
                checkActiveBooking() // Instantly refresh to show the booking info
                Log.d("BookFragment", "Service booked successfully")
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to book service: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("BookFragment", "Failed to book service: ${e.message}")
            }
    }

    private fun resetPage() {
        selectedServices.clear()
        selectedDateTime = null
        selectedBarberId = null
        loadBarbers()
        barberRecyclerView.adapter = null
        loadBarbers()
    }

    data class Barber(val barberId: String, val name: String, val profileImageUrl: String, val phoneNumber: String, val rating: Double, val customers: Int)
}
