package com.example.meditriage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.meditriage.databinding.FragmentAnalyzingBinding

// AI-assisted: ViewBinding wiring pattern
class AnalyzingFragment : Fragment() {

    private var _binding: FragmentAnalyzingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnalyzingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyzingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe sealed state from ViewModel
        viewModel.analysisState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AnalyzingViewModel.AnalysisState.Loading -> { /* spinner shown by layout */ }
                is AnalyzingViewModel.AnalysisState.Success -> {
                    if (isAdded) findNavController().navigate(R.id.action_analyzing_to_result)
                }
                is AnalyzingViewModel.AnalysisState.Error -> {
                    if (isAdded) findNavController().navigate(R.id.action_analyzing_to_result)
                }
            }
        }

        // Kick off analysis — ViewModel handles everything on IO dispatcher
        viewModel.analyzeSymptoms(TriageSession.answers)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
