package com.gbdev.ghostbarber.models

data class User(
    var username: String = "Unknown",
    var email: String = "Unknown",
    var bio: String = "",
    var isAdmin: Boolean = false,
    var phoneNumber: String = "",
    var profilePictureUrl: String? = null,
    var roles: Roles = Roles()
)

data class Roles(
    var barber: Boolean = false,
    var seller: Boolean = false,
    var investor: Boolean = false,
    var creator: Boolean = false,
    var owner: Boolean = false
)
