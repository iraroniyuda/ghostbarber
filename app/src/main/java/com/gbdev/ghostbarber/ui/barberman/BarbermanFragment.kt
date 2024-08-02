package com.gbdev.ghostbarber.ui.barberman

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.gbdev.ghostbarber.R
import com.google.firebase.firestore.FirebaseFirestore

class BarbermanFragment : Fragment() {

    private lateinit var barberRecyclerView: RecyclerView
    private val firestore = FirebaseFirestore.getInstance()
    private val barbersCollection = firestore.collection("barbers")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_barberman, container, false)
        barberRecyclerView = view.findViewById(R.id.barberRecyclerView)
        barberRecyclerView.layoutManager = LinearLayoutManager(context)
        loadBarbers()
        setHasOptionsMenu(true) // Enable options menu
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backToHomeButton = view.findViewById<ImageButton>(R.id.backToHomeButton)
        backToHomeButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_home)
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
        barbersCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val barbers = querySnapshot.documents.mapNotNull { document ->
                    val barberId = document.id
                    val name = document.getString("name") ?: ""
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""
                    Barber(barberId, name, profileImageUrl)
                }
                barberRecyclerView.adapter = BarberAdapter(requireContext(), barbers)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load barbers: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    inner class BarberAdapter(context: Context, private val barbers: List<Barber>) :
        RecyclerView.Adapter<BarberAdapter.BarberViewHolder>() {

        inner class BarberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val barberNameTextView: TextView = view.findViewById(R.id.barberNameTextView)
            val profileImageView: ImageView = view.findViewById(R.id.profileImageView)
            val servicesContainer: LinearLayout = view.findViewById(R.id.servicesContainer)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarberViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_barberman, parent, false)
            return BarberViewHolder(view)
        }

        override fun onBindViewHolder(holder: BarberViewHolder, position: Int) {
            val barber = barbers[position]
            holder.barberNameTextView.text = barber.name
            Glide.with(holder.itemView.context).load(barber.profileImageUrl).transform(CircleCrop()).into(holder.profileImageView)

            loadServices(barber.barberId, holder.servicesContainer)
        }

        override fun getItemCount(): Int = barbers.size
    }

    private fun loadServices(barberId: String?, servicesContainer: LinearLayout) {
        if (barberId == null) return

        barbersCollection.document(barberId).collection("services").get()
            .addOnSuccessListener { querySnapshot ->
                servicesContainer.removeAllViews()
                val services = querySnapshot.documents.mapNotNull { document ->
                    val serviceName = document.getString("serviceName")
                    val price = document.getDouble("price")
                    if (serviceName != null && price != null) {
                        "$serviceName - Rp ${String.format("%,.2f", price)}"
                    } else {
                        null
                    }
                }
                services.forEach { service ->
                    val textView = TextView(requireContext()).apply {
                        text = service
                        textSize = 16f
                    }
                    servicesContainer.addView(textView)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load services: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    data class Barber(val barberId: String, val name: String, val profileImageUrl: String)
}
