package com.example.meditriage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.meditriage.databinding.FragmentDoctorProfileBinding

// AI-assisted: ViewBinding wiring pattern
class DoctorProfileFragment : Fragment() {

    private var _binding: FragmentDoctorProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DoctorProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backBtn.setOnClickListener { findNavController().popBackStack() }

        val doctor = TriageSession.matchedDoctor
        val result = TriageSession.aiResult

        if (doctor != null) {
            binding.doctorNameText.text =
                getString(R.string.label_doctor_name, doctor.firstName, doctor.lastName)
            binding.doctorSpecialtyText.text =
                doctor.specialtyName ?: result?.recommendedDoctor ?: getString(R.string.label_fallback_specialty)
            binding.doctorClinicText.text =
                getString(R.string.label_clinic_prefix,
                    doctor.clinicName ?: getString(R.string.label_fallback_clinic),
                    doctor.clinicAddress ?: "")
            binding.doctorFeeText.text =
                getString(R.string.label_fee_suffix, doctor.consultationFee?.toInt() ?: 0)
            binding.doctorDaysText.text =
                getString(R.string.label_days_prefix,
                    doctor.availableDays ?: getString(R.string.label_fallback_availability))
            binding.doctorRatingText.text =
                getString(R.string.label_rating_prefix, doctor.rating?.toString() ?: "—")
            binding.doctorPhoneText.text =
                getString(R.string.label_phone_prefix,
                    doctor.phone ?: getString(R.string.label_fallback_phone))

            binding.bookAppointmentBtn.setOnClickListener {
                val phone = doctor.phone ?: "+20100000000"
                dialPhone(phone)
            }

            // Log audit via ViewModel — runs on IO dispatcher
            if (TriageSession.patientId != -1) {
                viewModel.logDoctorView(TriageSession.patientId, doctor.doctorId)
            }
        } else {
            binding.doctorSpecialtyText.text =
                result?.recommendedDoctor ?: getString(R.string.label_fallback_specialty)
            binding.bookAppointmentBtn.setOnClickListener {
                dialPhone("+20100000000")
            }
        }
    }

    private fun dialPhone(phone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:${phone.replace(" ", "")}"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(),
                getString(R.string.error_no_dialer), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
