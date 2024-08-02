package com.gbdev.ghostbarber.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gbdev.ghostbarber.R
import com.gbdev.ghostbarber.databinding.FragmentProfileBinding
import com.gbdev.ghostbarber.models.Post
import com.gbdev.ghostbarber.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var profileAdapter: ProfileAdapter
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        _binding = FragmentProfileBinding.inflate(inflater, container, false).apply {
            viewModel = profileViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        setupNavigationButtons()
        setupRecyclerView()
        loadProfile()
    }

    private fun setupNavigationButtons() {
        binding.homeButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_home)
        }
        binding.profileButton.setOnClickListener {
            // Current Fragment, do nothing
        }
        binding.reelsButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_reels)
        }
        binding.icBooking.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_bookFragment)
        }
        binding.aiHairstyleButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_hairstyleFragment)
        }
        binding.moreButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_moreFragment)
        }
    }

    private fun setupRecyclerView() {
        profileAdapter = ProfileAdapter(requireContext(), null, mutableListOf())
        binding.profileRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.profileRecyclerView.adapter = profileAdapter
    }

    private fun loadProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        currentUser = it
                        profileAdapter.updateProfile(it)
                        loadPosts(it)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPosts(user: User) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.map { document ->
                    val post = document.toObject(Post::class.java).apply {
                        id = document.id
                        username = user.username
                        profilePictureUrl = user.profilePictureUrl
                    }
                    fetchCommentsCount(post)
                    post
                }
                profileAdapter.updatePosts(posts)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load posts", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchCommentsCount(post: Post) {
        firestore.collection("posts").document(post.id).collection("comments").get()
            .addOnSuccessListener { querySnapshot ->
                post.commentsCount = querySnapshot.size()
                profileAdapter.updatePost(post)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}