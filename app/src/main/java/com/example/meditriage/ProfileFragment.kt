package com.example.meditriage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.meditriage.databinding.FragmentProfileBinding

// AI-assisted: ViewBinding wiring pattern
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fullName = "${TriageSession.firstName} ${TriageSession.lastName}".trim()
        val empty = getString(R.string.label_empty_field)

        binding.profileNameText.text  = fullName
        binding.profileEmailText.text = TriageSession.email

        binding.profileInfoCard.text =
            "${getString(R.string.label_personal_info)}\n\n" +
            "${getString(R.string.label_profile_full_name, fullName)}\n" +
            "${getString(R.string.label_profile_phone, TriageSession.phone.ifEmpty { empty })}\n" +
            "${getString(R.string.label_profile_blood, TriageSession.bloodType.ifEmpty { empty })}\n" +
            getString(R.string.label_profile_dob, TriageSession.dateOfBirth.ifEmpty { empty })

        binding.homeTab.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
        binding.historyTab.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_history)
        }
        binding.logoutBtn.setOnClickListener {
            TriageSession.clear()
            findNavController().navigate(R.id.action_profile_to_splash)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
