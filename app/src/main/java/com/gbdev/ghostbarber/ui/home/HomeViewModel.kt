package com.gbdev.ghostbarber.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gbdev.ghostbarber.models.Post
import com.google.firebase.firestore.FirebaseFirestore

class HomeViewModel : ViewModel() {

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val firestore = FirebaseFirestore.getInstance()

    fun fetchPosts() {
        firestore.collection("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.map { document ->
                    val post = document.toObject(Post::class.java)
                    post.id = document.id
                    post
                }
                _posts.value = posts
            }
            .addOnFailureListener { e ->
                // Handle the error
            }
    }
}
