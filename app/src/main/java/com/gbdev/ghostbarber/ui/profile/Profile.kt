package com.gbdev.ghostbarber.ui.profile

data class Profile(
    val username: String = "Unknown",
    val email: String = "Unknown",
    val bio: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String? = null
)