package com.gbdev.ghostbarber.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.gbdev.ghostbarber.R
import com.gbdev.ghostbarber.databinding.FragmentMoreBinding

class MoreFragment : Fragment() {

    private lateinit var moreViewModel: MoreViewModel
    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        moreViewModel = ViewModelProvider(this)[MoreViewModel::class.java]
        _binding = FragmentMoreBinding.inflate(inflater, container, false).apply {
            viewModel = moreViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigation for top navigation bar buttons
        binding.homeButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_home)
        }
        binding.profileButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_profile)
        }
        binding.reelsButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_reels)
        }

        // Navigation for icon menu group buttons
        binding.aiHairstyleButton.setOnClickListener {
            findNavController().navigate(R.id.action_moreFragment_to_hairstyleFragment)
        }

        binding.icBooking.setOnClickListener {
            findNavController().navigate(R.id.action_moreFragment_to_bookFragment)
        }

        // Navigation for specific fragments
        binding.icBarbershop.setOnClickListener {
            findNavController().navigate(R.id.action_moreFragment_to_barbershopFragment)
        }

        binding.icAcademy.setOnClickListener {
            findNavController().navigate(R.id.action_moreFragment_to_academyFragment)
        }

        binding.icTips.setOnClickListener {
            findNavController().navigate(R.id.action_moreFragment_to_tipsFragment)
        }

        // Navigation for User Settings button
        binding.userSettingButton.setOnClickListener {
            findNavController().navigate(R.id.action_moreFragment_to_userSettingFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
