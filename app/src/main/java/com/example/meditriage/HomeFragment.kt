package com.example.meditriage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.meditriage.databinding.FragmentHomeBinding
import java.util.Calendar

// AI-assisted: ViewBinding wiring pattern
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Greeting based on time of day
        val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11  -> getString(R.string.greeting_morning)
            in 12..17 -> getString(R.string.greeting_afternoon)
            else      -> getString(R.string.greeting_evening)
        }
        binding.greetingLabel.text = greeting
        binding.patientNameText.text =
            "${TriageSession.firstName} ${TriageSession.lastName}".trim()

        // Observe history from ViewModel — no direct API calls in the View layer
        viewModel.historyResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { list ->
                val visits = list.count { it.visited == "Y" }
                binding.sessionsCountText.text = "📋\n${list.size}\nSessions"
                binding.visitsCountText.text   = "🩺\n$visits\nVisits"

                if (list.isNotEmpty()) {
                    val latest = list.first()
                    val icon = severityIcon(latest.severityLevel)
                    binding.recentSessionText.text =
                        "$icon  ${latest.primaryCondition ?: "—"}\n" +
                        "${latest.sessionDate ?: ""}          ${latest.severityLevel ?: ""}"

                    if (list.size > 1) {
                        val prev = list[1]
                        val prevIcon = severityIcon(prev.severityLevel)
                        binding.prevSessionText.text =
                            "$prevIcon  ${prev.primaryCondition ?: "—"}\n" +
                            "${prev.sessionDate ?: ""}          ${prev.severityLevel ?: ""}"
                    }
                }
            }
            // On failure keep placeholder text — graceful degradation
        }

        viewModel.loadHistory(TriageSession.patientId)

        // Navigation
        binding.startTriageCard.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_triage)
        }
        binding.triageTab.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_triage)
        }
        binding.historyTab.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_history)
        }
        binding.profileTab.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }
    }

    private fun severityIcon(level: String?) = when (level?.lowercase()) {
        "high risk"   -> getString(R.string.label_severity_high)
        "medium risk" -> getString(R.string.label_severity_medium)
        else          -> getString(R.string.label_severity_low)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
