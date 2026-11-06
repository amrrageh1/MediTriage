package com.example.meditriage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.meditriage.databinding.FragmentRegisterBinding

// AI-assisted: ViewBinding wiring pattern
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()
    private var selectedGender = "M"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backBtn.setOnClickListener { findNavController().popBackStack() }

        binding.maleBtn.setOnClickListener {
            selectedGender = "M"
            binding.maleBtn.setBackgroundResource(R.drawable.bg_option_selected)
            binding.femaleBtn.setBackgroundResource(R.drawable.bg_input)
        }
        binding.femaleBtn.setOnClickListener {
            selectedGender = "F"
            binding.femaleBtn.setBackgroundResource(R.drawable.bg_option_selected)
            binding.maleBtn.setBackgroundResource(R.drawable.bg_input)
        }

        binding.createAccountBtn.setOnClickListener {
            val firstName = binding.firstNameInput.text.toString().trim()
            val lastName  = binding.lastNameInput.text.toString().trim()
            val email     = binding.emailInput.text.toString().trim()
            val phone     = binding.phoneInput.text.toString().trim()
            val blood     = binding.bloodInput.text.toString().trim()
            val dob       = binding.dobInput.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(requireContext(),
                    getString(R.string.error_name_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                Toast.makeText(requireContext(),
                    getString(R.string.error_email_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(
                RegisterRequest(firstName, lastName, email, phone, selectedGender, blood, dob)
            )
        }

        viewModel.registerResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { body ->
                TriageSession.patientId   = body.patientId!!
                TriageSession.firstName   = binding.firstNameInput.text.toString().trim()
                TriageSession.lastName    = binding.lastNameInput.text.toString().trim()
                TriageSession.email       = binding.emailInput.text.toString().trim()
                TriageSession.phone       = binding.phoneInput.text.toString().trim()
                TriageSession.bloodType   = binding.bloodInput.text.toString().trim()
                TriageSession.dateOfBirth = binding.dobInput.text.toString().trim()
                TriageSession.gender      = selectedGender

                Toast.makeText(requireContext(),
                    getString(R.string.msg_account_created), Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_register_to_home)
            }
            result.onFailure { e ->
                Toast.makeText(requireContext(),
                    getString(R.string.error_server, e.message), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
