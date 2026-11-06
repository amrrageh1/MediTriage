package com.example.meditriage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.meditriage.databinding.FragmentConfirmVisitBinding

// AI-assisted: ViewBinding wiring pattern
class ConfirmVisitFragment : Fragment() {

    private var _binding: FragmentConfirmVisitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConfirmVisitViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmVisitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.yesVisitedBtn.setOnClickListener { viewModel.saveHistory("Y") }
        binding.noVisitedBtn.setOnClickListener  { viewModel.saveHistory("N") }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { msg ->
                if (msg != "skip") {
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
            result.onFailure { e ->
                Toast.makeText(requireContext(),
                    getString(R.string.error_could_not_save, e.message),
                    Toast.LENGTH_SHORT).show()
            }
            findNavController().navigate(R.id.action_confirm_to_history)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
