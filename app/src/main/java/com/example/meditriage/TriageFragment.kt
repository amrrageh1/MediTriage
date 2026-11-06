package com.example.meditriage

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.meditriage.databinding.FragmentTriageBinding

// AI-assisted: ViewBinding wiring pattern
class TriageFragment : Fragment() {

    private var _binding: FragmentTriageBinding? = null
    private val binding get() = _binding!!

    private data class TriageQuestion(
        val icon: String,
        val question: String,
        val subtitle: String,
        val options: List<String>
    )

    private val questions = listOf(
        TriageQuestion("🤒", "Do you have a fever?",
            "Select what best describes your temperature",
            listOf("High fever above 38.5°C", "Mild fever 37.5°C - 38.5°C", "No fever")),
        TriageQuestion("😮‍💨", "Do you have breathing difficulty?",
            "Choose the closest option",
            listOf("Severe shortness of breath", "Mild breathing difficulty", "No breathing difficulty")),
        TriageQuestion("🤕", "Do you have pain?",
            "Choose your pain level",
            listOf("Severe pain", "Moderate pain", "Mild pain", "No pain")),
        TriageQuestion("🧠", "Do you feel dizzy or confused?",
            "This helps detect emergency signs",
            listOf("Very dizzy or confused", "Slight dizziness", "No dizziness or confusion")),
        TriageQuestion("🤢", "Do you have nausea or vomiting?",
            "Select what applies to you",
            listOf("Repeated vomiting", "Nausea only", "No nausea or vomiting")),
        TriageQuestion("🩸", "Do you have bleeding?",
            "Choose the most accurate answer",
            listOf("Heavy bleeding", "Minor bleeding", "No bleeding")),
        TriageQuestion("⏱️", "How long have you had these symptoms?",
            "Duration helps estimate urgency",
            listOf("Less than 24 hours", "1 to 3 days", "More than 3 days", "More than 1 week"))
    )

    private var currentQuestionIndex = 0
    private var selectedAnswer: String? = null
    private val answers = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTriageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backBtn.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                if (answers.isNotEmpty()) answers.removeAt(answers.lastIndex)
                showQuestion()
            } else {
                findNavController().popBackStack()
            }
        }

        binding.continueBtn.setOnClickListener {
            val answer = selectedAnswer
            if (answer == null) {
                Toast.makeText(requireContext(),
                    getString(R.string.error_select_answer), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            answers.add(answer)
            if (currentQuestionIndex < questions.lastIndex) {
                currentQuestionIndex++
                showQuestion()
            } else {
                TriageSession.answers = answers.toList()
                findNavController().navigate(R.id.action_triage_to_analyzing)
            }
        }

        showQuestion()
    }

    private fun showQuestion() {
        selectedAnswer = null
        val question = questions[currentQuestionIndex]

        binding.questionCounterText.text =
            getString(R.string.label_question_counter, currentQuestionIndex + 1, questions.size)
        binding.questionIcon.text     = question.icon
        binding.questionTitle.text    = question.question
        binding.questionSubtitle.text = question.subtitle

        val progress = (((currentQuestionIndex + 1).toDouble() / questions.size) * 100).toInt()
        binding.progressBar.progress = progress

        binding.optionsContainer.removeAllViews()
        question.options.forEach { optionText ->
            val optionView = createOptionView(optionText)
            optionView.setOnClickListener { selectOption(optionView, optionText) }
            binding.optionsContainer.addView(optionView)
        }

        val btnText = if (currentQuestionIndex == questions.lastIndex)
            getString(R.string.btn_analyze) else getString(R.string.btn_continue)
        binding.continueBtn.text = btnText
    }

    private fun createOptionView(optionText: String): TextView {
        return TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 72.dp()
            ).also { it.setMargins(0, 0, 0, 10.dp()) }
            setBackgroundResource(R.drawable.bg_option_normal)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16.dp(), 0, 16.dp(), 0)
            text = optionText
            textSize = 14f
            setTextColor(resources.getColor(R.color.ink, null))
            setTypeface(null, Typeface.BOLD)
        }
    }

    private fun selectOption(selectedView: View, answer: String) {
        for (i in 0 until binding.optionsContainer.childCount) {
            binding.optionsContainer.getChildAt(i)
                .setBackgroundResource(R.drawable.bg_option_normal)
        }
        selectedView.setBackgroundResource(R.drawable.bg_option_selected)
        selectedAnswer = answer
    }

    private fun Int.dp() = (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
