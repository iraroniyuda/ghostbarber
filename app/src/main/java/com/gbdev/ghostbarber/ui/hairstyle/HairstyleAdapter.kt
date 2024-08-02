package com.gbdev.ghostbarber.ui.hairstyle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gbdev.ghostbarber.R

class HairstyleAdapter(
    private val hairstyles: List<Hairstyle>,
    private val onItemClick: (Hairstyle) -> Unit
) : RecyclerView.Adapter<HairstyleAdapter.HairstyleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HairstyleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hairstyle, parent, false)
        return HairstyleViewHolder(view)
    }

    override fun onBindViewHolder(holder: HairstyleViewHolder, position: Int) {
        holder.bind(hairstyles[position])
    }

    override fun getItemCount() = hairstyles.size

    inner class HairstyleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.hairstyleImageView)
        private val textView: TextView = itemView.findViewById(R.id.hairstyleTextView)

        fun bind(hairstyle: Hairstyle) {
            Log.d("HairstyleAdapter", "Binding hairstyle: ${hairstyle.name} with image resource ID: ${hairstyle.imageResId}")

            // Clear the ImageView to prevent old images from showing
            imageView.setImageDrawable(null)

            // Load the new image
            Glide.with(itemView.context)
                .load(hairstyle.imageResId)
                .apply(RequestOptions().skipMemoryCache(true).diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE))
                .centerCrop()
                .into(imageView)

            // Set the text
            textView.text = hairstyle.name

            // Set the click listener
            itemView.setOnClickListener { onItemClick(hairstyle) }
        }
    }
}