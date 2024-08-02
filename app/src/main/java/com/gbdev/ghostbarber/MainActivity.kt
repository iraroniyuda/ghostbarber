package com.gbdev.ghostbarber

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import com.gbdev.ghostbarber.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        setTheme(R.style.Theme_GhostBarber)

        // Inflate layout and setup binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Setup nav controller
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        // Setup AppBarConfiguration with the top-level destinations
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_message, R.id.navigation_reels, R.id.navigation_cart, R.id.navigation_profile
            )
        )

        // Direct users based on their authentication status
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.loginFragment && auth.currentUser == null) {
                // No user is signed in and we are not on the login page, redirect to login
                navController.navigate(R.id.loginFragment)
            } else if (destination.id == R.id.loginFragment && auth.currentUser != null) {
                // User is signed in but we are on the login page, redirect to home
                navController.navigate(R.id.navigation_home)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
