package com.gbdev.ghostbarber.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreData {
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val usersCollection = firestore.collection("users")
    val barbersCollection = firestore.collection("barbers")
    val currentUserId: String? get() = FirebaseAuth.getInstance().currentUser?.uid
}
