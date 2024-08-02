package com.gbdev.ghostbarber.ui.barberman

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gbdev.ghostbarber.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.Locale

class ServicesAdapter(private val onServiceAction: (BarbermanPanelFragment.Service, String) -> Unit) :
    ListAdapter<BarbermanPanelFragment.Service, ServicesAdapter.ServiceViewHolder>(ServiceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = getItem(position)
        holder.bind(service)
    }

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serviceNameTextView: TextView = itemView.findViewById(R.id.serviceNameTextView)
        private val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(service: BarbermanPanelFragment.Service) {
            serviceNameTextView.text = service.serviceName
            priceTextView.text = NumberFormat.getNumberInstance(Locale.US).format(service.price)

            editButton.setOnClickListener {
                showConfirmationDialog(itemView.context, "Edit Service", "Are you sure you want to edit this service?") {
                    onServiceAction(service, "edit")
                }
            }

            deleteButton.setOnClickListener {
                showConfirmationDialog(itemView.context, "Delete Service", "Are you sure you want to delete this service?") {
                    onServiceAction(service, "delete")
                }
            }
        }
    }

    private fun showConfirmationDialog(context: Context, title: String, message: String, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("No", null)
            .show()
    }

    class ServiceDiffCallback : DiffUtil.ItemCallback<BarbermanPanelFragment.Service>() {
        override fun areItemsTheSame(oldItem: BarbermanPanelFragment.Service, newItem: BarbermanPanelFragment.Service): Boolean {
            return oldItem.serviceId == newItem.serviceId
        }

        override fun areContentsTheSame(oldItem: BarbermanPanelFragment.Service, newItem: BarbermanPanelFragment.Service): Boolean {
            return oldItem == newItem
        }
    }
}
