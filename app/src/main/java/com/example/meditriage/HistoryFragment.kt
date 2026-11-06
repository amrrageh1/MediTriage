package com.example.meditriage

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.meditriage.databinding.FragmentHistoryBinding

// AI-assisted: ViewBinding wiring pattern
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.homeTab.setOnClickListener {
            findNavController().navigate(R.id.action_history_to_home)
        }
        binding.profileTab.setOnClickListener {
            findNavController().navigate(R.id.action_history_to_profile)
        }

        // Observe history items from ViewModel
        viewModel.historyItems.observe(viewLifecycleOwner) { result ->
            result.onSuccess { list -> renderHistory(list) }
            result.onFailure { showError() }
        }

        viewModel.loadHistory(TriageSession.patientId)
    }

    private fun renderHistory(list: List<HistoryItem>) {
        binding.historyContainer.removeAllViews()

        if (list.isEmpty()) {
            val empty = TextView(requireContext()).apply {
                text = getString(R.string.label_no_history)
                gravity = Gravity.CENTER
                setPadding(0, 60, 0, 0)
                setTextColor(resources.getColor(R.color.slate, null))
            }
            binding.historyContainer.addView(empty)
            return
        }

        var lastMonth = ""
        list.forEach { item ->
            val monthLabel = formatMonthLabel(item.sessionDate)
            if (monthLabel != lastMonth) {
                lastMonth = monthLabel
                val header = TextView(requireContext()).apply {
                    text = monthLabel
                    setTextColor(resources.getColor(R.color.fog, null))
                    setTypeface(null, Typeface.BOLD)
                    val p = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    p.topMargin = 18.dp()
                    layoutParams = p
                }
                binding.historyContainer.addView(header)
            }

            val icon = when (item.severityLevel?.lowercase()) {
                "high risk"   -> getString(R.string.label_severity_high)
                "medium risk" -> getString(R.string.label_severity_medium)
                else          -> getString(R.string.label_severity_low)
            }
            val visitStatus = when (item.visited) {
                "Y"  -> getString(R.string.label_visited_yes)
                "N"  -> getString(R.string.label_visited_no)
                else -> ""
            }

            val card = TextView(requireContext()).apply {
                text = "${formatDay(item.sessionDate)}   ${item.primaryCondition ?: "—"}\n" +
                       "${item.recommendedSpecialty ?: ""}$visitStatus\n" +
                       "$icon ${item.severityLevel ?: ""}"
                setBackgroundResource(R.drawable.bg_card_white)
                setTextColor(resources.getColor(R.color.ink, null))
                setTypeface(null, Typeface.BOLD)
                setPadding(16.dp(), 14.dp(), 16.dp(), 14.dp())
                gravity = Gravity.CENTER_VERTICAL
                val p = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 90.dp()
                )
                p.topMargin = 8.dp()
                layoutParams = p
            }
            binding.historyContainer.addView(card)
        }
    }

    private fun showError() {
        binding.historyContainer.removeAllViews()
        val errView = TextView(requireContext()).apply {
            text = getString(R.string.error_history_load)
            gravity = Gravity.CENTER
            setPadding(0, 60, 0, 0)
            setTextColor(resources.getColor(R.color.slate, null))
        }
        binding.historyContainer.addView(errView)
    }

    private fun formatMonthLabel(date: String?): String {
        if (date.isNullOrBlank()) return getString(R.string.label_unknown_date)
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val d = sdf.parse(date) ?: return date
            java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
                .format(d).uppercase()
        } catch (_: Exception) { date }
    }

    private fun formatDay(date: String?): String {
        if (date.isNullOrBlank()) return getString(R.string.label_date_placeholder)
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val d = sdf.parse(date) ?: return date
            java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(d)
        } catch (_: Exception) { date }
    }

    private fun Int.dp() = (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
