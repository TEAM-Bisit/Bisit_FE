package com.example.bisit.ui.customerReserve

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.bisit.R
import com.example.bisit.databinding.FragmentCustomerReserveBinding
import com.example.bisit.widget.StepProgressView
import com.google.android.material.card.MaterialCardView

class CustomerReserveFragment : Fragment() {

    private var _binding: FragmentCustomerReserveBinding? = null
    private val binding get() = _binding!!

    private var currentStep = 0
    private lateinit var stepLabels: List<String>

    private var morningGrid: LinearLayout? = null
    private var afternoonGrid: LinearLayout? = null
    private var selectedButton: Button? = null
    private var selectedDate: String? = null

    private var lastCheckedId: Int = -1
    private var isChangingCheck = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerReserveBinding.inflate(inflater, container, false)

        setupStepProgress()
        setupVisitType()
        setupCalendar()
        setupTimeSlotRows()
        loadTimeSlotsForDate("2025-11-05")

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        selectedButton = null
        morningGrid = null
        afternoonGrid = null
        selectedDate = null
        lastCheckedId = -1
        _binding = null
    }

    private fun setupStepProgress() {
        val stepView = binding.stepProgressView as? StepProgressView ?: return
        stepLabels = resources.getStringArray(R.array.step_labels).toList()
        stepView.setStepCount(stepLabels.size)
        stepView.setLabels(stepLabels)
        stepView.setCurrentStep(currentStep)
        stepView.setPadding(24, 20, 24, 20)

        binding.btnNextStep.setOnClickListener {
            currentStep = (currentStep + 1).coerceAtMost(stepLabels.size - 1)
            stepView.setCurrentStep(currentStep)
        }
    }

    private fun setupVisitType() {
        val homeCard = binding.cardHomeService
        val visitCard = binding.cardVisitDesigner

        val radioGroup = binding.radioGroupVisitType
        val radioHome = binding.radioHomeService
        val radioVisit = binding.radioVisitDesigner

        radioGroup.clearCheck()
        updateCardBorder(homeCard, false)
        updateCardBorder(visitCard, false)

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (isChangingCheck) return@setOnCheckedChangeListener

            if (checkedId == lastCheckedId) {
                isChangingCheck = true
                group.clearCheck()
                lastCheckedId = -1
                updateCardBorder(homeCard, false)
                updateCardBorder(visitCard, false)
                isChangingCheck = false
            } else {
                when (checkedId) {
                    R.id.radio_home_service -> {
                        updateCardBorder(homeCard, true)
                        updateCardBorder(visitCard, false)
                    }
                    R.id.radio_visit_designer -> {
                        updateCardBorder(homeCard, false)
                        updateCardBorder(visitCard, true)
                    }
                }
                lastCheckedId = checkedId
            }
        }
    }

    private fun updateCardBorder(cardView: MaterialCardView, selected: Boolean) {
        val color = if (selected)
            ContextCompat.getColor(requireContext(), R.color.red_FE6B6B)
        else
            ContextCompat.getColor(requireContext(), R.color.gray)

        cardView.strokeWidth = 3
        cardView.strokeColor = color
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth"
            loadTimeSlotsForDate(selectedDate!!)
            Toast.makeText(requireContext(), "날짜 선택: $selectedDate", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTimeSlotRows() {
        val context = requireContext()
        binding.layoutTimeSlots.removeAllViews()

        morningGrid = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        afternoonGrid = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        binding.layoutTimeSlots.addView(morningGrid)
        binding.layoutTimeSlots.addView(afternoonGrid)
    }

    private fun addButtonToGrid(grid: LinearLayout, time: String, disabled: Boolean = false) {
        val context = requireContext()
        val button = Button(context).apply {
            text = time
            setBackgroundResource(R.drawable.bg_time_slot_selector)
            setTextColor(ContextCompat.getColor(context, R.color.black))
            textSize = 13f
            isAllCaps = false

            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            val margin = dpToPx(6)
            params.setMargins(margin, margin, margin, margin)
            layoutParams = params

            isEnabled = !disabled
            isSelected = false

            setOnClickListener {
                if (!isEnabled) return@setOnClickListener
                handleSelection(this)
                Toast.makeText(context, "$time 선택됨", Toast.LENGTH_SHORT).show()
            }
        }
        grid.addView(button)
    }

    private fun loadTimeSlotsForDate(date: String) {
        val morningTimes = listOf("09:00", "09:30", "10:00")
        val afternoonTimes = listOf("12:00", "12:30", "13:00", "13:30", "14:00")
        val disabledTimes = setOf("10:00", "13:30")

        morningGrid?.removeAllViews()
        afternoonGrid?.removeAllViews()

        morningTimes.forEach { time ->
            addButtonToGrid(morningGrid!!, time, disabledTimes.contains(time))
        }
        afternoonTimes.forEach { time ->
            addButtonToGrid(afternoonGrid!!, time, disabledTimes.contains(time))
        }
    }

    private fun handleSelection(clicked: Button) {
        clearAllSelections()
        clicked.isSelected = true
        selectedButton = clicked
        clicked.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        clicked.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun clearAllSelections() {
        val container = binding.layoutTimeSlots
        for (i in 0 until container.childCount) {
            val row = container.getChildAt(i)
            if (row is LinearLayout) {
                for (j in 0 until row.childCount) {
                    val child = row.getChildAt(j)
                    if (child is Button) {
                        child.isSelected = false
                        child.setBackgroundResource(R.drawable.bg_time_slot_selector)
                        child.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    }
                }
            }
        }
        selectedButton = null
    }

    fun getSelectedTime(): String? = selectedButton?.text?.toString()

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()
}
