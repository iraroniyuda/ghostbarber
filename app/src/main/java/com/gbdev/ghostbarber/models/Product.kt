package com.gbdev.ghostbarber.models

import com.google.firebase.Timestamp

data class Product(
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "",
    val stock: Int = 0,
    val userId: String = "",  // Add this field
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
