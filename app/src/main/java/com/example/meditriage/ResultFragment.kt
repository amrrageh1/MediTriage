package com.example.meditriage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.meditriage.databinding.FragmentResultBinding

// AI-assisted: ViewBinding wiring pattern
class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val result = TriageSession.aiResult
        if (result != null) {
            binding.severityText.text =
                getString(R.string.label_severity_prefix, result.severity)
            binding.conditionText.text  = result.possibleCondition
            binding.confidenceText.text = getString(R.string.label_confidence_suffix, result.confidence)
            binding.adviceText.text     = getString(R.string.label_advice_prefix, result.advice)
            binding.doctorText.text     = getString(R.string.label_doctor_prefix, result.recommendedDoctor)
        }

        binding.backBtn.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
        binding.viewDoctorBtn.setOnClickListener {
            findNavController().navigate(R.id.action_result_to_doctor)
        }
        binding.confirmVisitBtn.setOnClickListener {
            findNavController().navigate(R.id.action_result_to_confirm)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
