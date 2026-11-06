package com.example.meditriage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.meditriage.databinding.FragmentLoginBinding

// AI-assisted: ViewBinding wiring pattern
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // ViewModel survives screen rotation — no data loss
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backBtn.setOnClickListener { findNavController().popBackStack() }

        binding.loginSubmitBtn.setOnClickListener {
            val email = binding.loginEmailInput.text.toString().trim()
            val phone = binding.loginPhoneInput.text.toString().trim()

            if (email.isEmpty() && phone.isEmpty()) {
                Toast.makeText(requireContext(),
                    getString(R.string.error_login_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(LoginRequest(email = email, phone = phone))
        }

        // Observe login result — Fragment only reacts to state changes
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { body ->
                TriageSession.patientId   = body.patientId!!
                TriageSession.firstName   = body.firstName  ?: ""
                TriageSession.lastName    = body.lastName   ?: ""
                TriageSession.email       = body.email      ?: ""
                TriageSession.phone       = body.phone      ?: ""
                TriageSession.bloodType   = body.bloodType  ?: ""
                TriageSession.dateOfBirth = body.dateOfBirth ?: ""
                TriageSession.gender      = body.gender     ?: ""

                Toast.makeText(requireContext(),
                    getString(R.string.msg_welcome_back, TriageSession.firstName),
                    Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_login_to_home)
            }
            result.onFailure { e ->
                Toast.makeText(requireContext(),
                    getString(R.string.error_login_failed, e.message),
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Prevent memory leaks — binding tied to view lifecycle
    }
}
