package com.gbdev.ghostbarber.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gbdev.ghostbarber.R
import com.gbdev.ghostbarber.databinding.FragmentHomeBinding
import com.gbdev.ghostbarber.models.Post
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var homeAdapter: HomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false).apply {
            viewModel = homeViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        setupNavigationButtons()
        setupRecyclerView()
        loadPosts()
        setupSearchButton()
        setupDimOverlay()
    }

    private fun setupNavigationButtons() {
        binding.homeButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_home)
        }
        binding.profileButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_profile)
        }
        binding.reelsButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_reels)
        }
        binding.icBooking.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_bookFragment)
        }
        binding.aiHairstyleButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_hairstyleFragment)
        }
        binding.moreButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_moreFragment)
        }
        binding.walletIcon.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_walletFragment)
        }
        binding.plusButton.setOnClickListener {
            checkUserRoleAndNavigate()
        }
    }

    private fun setupRecyclerView() {
        homeAdapter = HomeAdapter(requireContext(), mutableListOf())
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = homeAdapter
    }

    private fun loadPosts() {
        firestore.collection("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.map { document ->
                    val post = document.toObject(Post::class.java).apply { id = document.id }
                    fetchUserDetails(post)
                    post
                }
                homeAdapter.updatePosts(posts)
            }
    }

    private fun fetchUserDetails(post: Post) {
        val userId = post.userId
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    post.username = document.getString("username") ?: "Unknown"
                    post.profilePictureUrl = document.getString("profilePictureUrl")
                    fetchCommentsCount(post)
                }
            }
    }

    private fun fetchCommentsCount(post: Post) {
        val postId = post.id
        firestore.collection("posts").document(postId).collection("comments").get()
            .addOnSuccessListener { comments ->
                post.commentsCount = comments.size()
                homeAdapter.updatePost(post)
            }
    }

    private fun checkUserRoleAndNavigate() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val isCreator = document.get("roles.creator") as Boolean? ?: false
                if (isCreator) {
                    findNavController().navigate(R.id.action_homeFragment_to_createPostFragment)
                } else {
                    showNotAllowedAlert()
                }
            }
    }

    private fun showNotAllowedAlert() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Access Denied")
            .setMessage("POST CONTENT IS ONLY FOR REGISTERED BARBER")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupSearchButton() {
        binding.searchIcon.setOnClickListener {
            showFloatingSearchBar()
        }
    }

    private fun setupDimOverlay() {
        binding.dimOverlay.setOnClickListener {
            hideFloatingSearchBar()
        }
    }

    private fun showFloatingSearchBar() {
        binding.floatingSearchBar.root.visibility = View.VISIBLE
        binding.dimOverlay.visibility = View.VISIBLE
        binding.dimOverlay.isClickable = true
    }

    private fun hideFloatingSearchBar() {
        binding.floatingSearchBar.root.visibility = View.GONE
        binding.dimOverlay.visibility = View.GONE
        binding.dimOverlay.isClickable = false
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
