package com.gbdev.ghostbarber.ui.shop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.gbdev.ghostbarber.databinding.FragmentShopBinding
import com.gbdev.ghostbarber.models.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.gbdev.ghostbarber.ui.common.ProductAdapter

class ShopFragment : Fragment() {

    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        setupRecyclerView()
        setupBackButton()
        fetchProducts()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter()
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2) // Set 2 columns per row
            adapter = productAdapter
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun fetchProducts() {
        firestore.collection("products").get()
            .addOnSuccessListener { result ->
                val productList = result.toObjects(Product::class.java)
                Log.d("ShopFragment", "Fetched ${productList.size} products")
                productAdapter.submitList(productList)
            }
            .addOnFailureListener { e ->
                Log.e("ShopFragment", "Error fetching products", e)
                Toast.makeText(context, "Error fetching products: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
