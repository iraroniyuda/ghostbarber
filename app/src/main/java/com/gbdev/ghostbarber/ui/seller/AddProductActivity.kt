package com.gbdev.ghostbarber.ui.seller

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.gbdev.ghostbarber.databinding.ActivityAddProductBinding
import com.gbdev.ghostbarber.models.Product
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.productImageUrl.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        binding.saveButton.setOnClickListener {
            val name = binding.productName.text.toString()
            val description = binding.productDescription.text.toString()
            val priceString = binding.productPrice.text.toString()
            val price = priceString.toDoubleOrNull() ?: 0.0
            val category = binding.productCategory.text.toString()
            val stock = binding.productStock.text.toString().toIntOrNull() ?: 0
            val timestamp = Timestamp(Date())
            val userId = auth.currentUser?.uid ?: return@setOnClickListener

            if (imageUri != null) {
                uploadImageToFirebaseStorage(imageUri!!) { imageUrl ->
                    val product = Product(
                        name = name,
                        description = description,
                        price = price,
                        imageUrl = imageUrl,
                        category = category,
                        stock = stock,
                        userId = userId,  // Set userId
                        createdAt = timestamp,
                        updatedAt = timestamp
                    )

                    firestore.collection("products").add(product)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to add product: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            imageUri = result.data!!.data
            binding.productImageUrl.setText(imageUri.toString())
            binding.productImageView.visibility = ImageView.VISIBLE
            Glide.with(this).load(imageUri).into(binding.productImageView)
        }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri, callback: (String) -> Unit) {
        val storageRef = storage.reference.child("product_images/${UUID.randomUUID()}.jpg")
        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString())
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to get image URL: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
