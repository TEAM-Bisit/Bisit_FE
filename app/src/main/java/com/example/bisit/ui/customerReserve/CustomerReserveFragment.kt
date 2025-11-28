package com.example.bisit.ui.customerReserve

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bisit.R
import com.example.bisit.databinding.FragmentCustomerReserveBinding
import com.example.bisit.widget.StepProgressView
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class CustomerReserveFragment : Fragment() {

    private var _binding: FragmentCustomerReserveBinding? = null
    private val binding get() = _binding

    private var currentStep = 0
    private lateinit var stepLabels: List<String>

    private var isDesignerViewed = true

    private var selectedDate: Calendar? = null
    private var selectedTime: String? = null
    private var selectedButton: Button? = null
    private var currentCalendar: Calendar = Calendar.getInstance()
    private lateinit var calendarAdapter: CalendarAdapter

    private var selectedServiceCard: MaterialCardView? = null
    private var selectedServicePrice: Int = 0

    private var selectedVisitType: String? = null

    private var isAutoScrolling = false

    private var scrollChangedListener: ViewTreeObserver.OnScrollChangedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerReserveBinding.inflate(inflater, container, false)

        setupStepProgress()
        setupCalendar()
        setupServiceMenu()
        setupVisitType()
        setupBackButton()
        setupScrollListener()
        loadTimeSlotsForDate("2025-11-05")
        updateNextButton()

        return binding!!.root
    }

    override fun onDestroyView() {
        binding?.scrollView?.viewTreeObserver?.removeOnScrollChangedListener(scrollChangedListener)
        scrollChangedListener = null

        selectedButton = null
        selectedDate = null
        selectedServiceCard = null

        _binding = null
        super.onDestroyView()
    }

    private fun setupStepProgress() {
        val stepView = binding?.stepProgressView as? StepProgressView ?: return
        stepLabels = resources.getStringArray(R.array.step_labels).toList()
        stepView.setStepCount(stepLabels.size)
        stepView.setLabels(stepLabels)
        stepView.setCurrentStep(currentStep)

        binding?.btnNextStep?.setOnClickListener {
            if (isAllStepsComplete()) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateStr = selectedDate?.let { dateFormat.format(it.time) } ?: ""
                val bundle = Bundle().apply {
                    putInt("totalPrice", selectedServicePrice)
                    putString("serviceName", getSelectedServiceName())
                    putString("selectedDate", dateStr)
                    putString("selectedTime", selectedTime)
                    putString("visitType", selectedVisitType)
                }
                findNavController().navigate(
                    R.id.action_customerReserveFragment_to_customerPayFragment,
                    bundle
                )
            }
        }
    }

    private fun setupScrollListener() {
        scrollChangedListener = ViewTreeObserver.OnScrollChangedListener {
            val bind = binding ?: return@OnScrollChangedListener
            if (isAutoScrolling) return@OnScrollChangedListener

            val scrollY = bind.scrollView.scrollY

            val serviceSectionTop = bind.layoutServiceMenuSection.top
            val visitSectionTop = bind.radioGroupVisitType.top

            if (serviceSectionTop == 0 || visitSectionTop == 0) return@OnScrollChangedListener

            val step = when {
                scrollY >= visitSectionTop - 600 -> 3
                scrollY >= serviceSectionTop - 400 -> 2
                scrollY >= 300 -> 1
                else -> 0
            }

            if (step != currentStep) {
                currentStep = step
                (bind.stepProgressView as? StepProgressView)?.setCurrentStep(currentStep)
            }
        }

        binding?.scrollView?.viewTreeObserver?.addOnScrollChangedListener(scrollChangedListener)
    }

    private fun setupBackButton() {
        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter { date ->
            val isFirstSelection = selectedDate == null
            selectedDate = date
            calendarAdapter.setSelectedDate(date)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = dateFormat.format(date.time)
            loadTimeSlotsForDate(dateStr)
            if (isFirstSelection) smoothScrollToView(binding?.layoutTimeSlots ?: return@CalendarAdapter)
            updateStepProgress()
        }

        binding?.rvCalendar?.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = calendarAdapter
            itemAnimator = null
        }

        updateCalendarDays()

        binding?.btnPrevMonth?.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendarDays()
        }

        binding?.btnNextMonth?.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendarDays()
        }
    }

    private fun updateCalendarDays() {
        val bind = binding ?: return
        val year = currentCalendar.get(Calendar.YEAR)
        val month = currentCalendar.get(Calendar.MONTH)

        val monthFormat = SimpleDateFormat("yy.MM", Locale.getDefault())
        bind.tvCurrentMonth.text = monthFormat.format(currentCalendar.time)

        val days = ArrayList<Calendar?>()
        val calendar = currentCalendar.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        for (i in 1 until dayOfWeek) days.add(null)

        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..maxDay) {
            val day = calendar.clone() as Calendar
            days.add(day)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        calendarAdapter.submitList(days)

        if (selectedDate != null &&
            selectedDate!!.get(Calendar.YEAR) == year &&
            selectedDate!!.get(Calendar.MONTH) == month
        ) {
            calendarAdapter.setSelectedDate(selectedDate)
        } else {
            calendarAdapter.setSelectedDate(null)
        }
    }

    private fun loadTimeSlotsForDate(date: String) {
        val bind = binding ?: return

        val morningTimes = listOf("09:00", "09:30", "10:00", "10:30", "11:00", "11:30")
        val afternoonTimes =
            listOf("12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "16:00", "16:30", "17:00", "17:30")
        val disabledTimes = setOf("10:00", "13:30", "15:00")

        bind.layoutTimeSlots.removeAllViews()

        addSectionTitle("오전")
        addTimeGrid(morningTimes, disabledTimes)

        addSectionTitle("오후")
        addTimeGrid(afternoonTimes, disabledTimes)
    }

    private fun addSectionTitle(title: String) {
        val bind = binding ?: return
        val textView = TextView(requireContext()).apply {
            text = title
            textSize = 14f
            setTextColor(Color.parseColor("#222222"))
            setPadding(0, dpToPx(16), 0, dpToPx(8))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        bind.layoutTimeSlots.addView(textView)
    }

    private fun addTimeGrid(times: List<String>, disabledTimes: Set<String>) {
        val bind = binding ?: return
        val context = requireContext()
        val chunks = times.chunked(4)

        chunks.forEach { chunk ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 0, 0, dpToPx(8))
            }

            chunk.forEach { time ->
                val button = createTimeButton(time, disabledTimes.contains(time))
                val params = LinearLayout.LayoutParams(0, dpToPx(40), 1f)
                params.setMargins(dpToPx(4), 0, dpToPx(4), 0)
                button.layoutParams = params
                row.addView(button)
            }

            if (chunk.size < 4) {
                for (i in 0 until (4 - chunk.size)) {
                    val spacer = View(context)
                    val params = LinearLayout.LayoutParams(0, dpToPx(40), 1f)
                    params.setMargins(dpToPx(4), 0, dpToPx(4), 0)
                    spacer.layoutParams = params
                    row.addView(spacer)
                }
            }

            bind.layoutTimeSlots.addView(row)
        }
    }

    private fun createTimeButton(time: String, disabled: Boolean): Button {
        return Button(requireContext()).apply {
            text = time
            setBackgroundResource(R.drawable.bg_time_slot_selector)
            setTextColor(ContextCompat.getColor(context, R.color.black))
            textSize = 13f
            isAllCaps = false
            stateListAnimator = null
            isEnabled = !disabled
            isSelected = false

            setOnClickListener {
                if (!isEnabled) return@setOnClickListener
                handleTimeSelection(this, time)
            }
        }
    }

    private fun handleTimeSelection(clicked: Button, time: String) {
        val bind = binding ?: return
        val isFirstSelection = selectedTime == null
        clearAllTimeSelections()
        clicked.isSelected = true
        selectedButton = clicked
        selectedTime = time
        clicked.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        if (isFirstSelection) smoothScrollToView(bind.layoutServiceMenuSection)
        updateStepProgress()
        updateNextButton()
    }

    private fun clearAllTimeSelections() {
        val bind = binding ?: return
        clearSelectionsRecursive(bind.layoutTimeSlots)
        selectedButton = null
    }

    private fun clearSelectionsRecursive(view: View) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) clearSelectionsRecursive(view.getChildAt(i))
        } else if (view is Button) {
            view.isSelected = false
            view.setBackgroundResource(R.drawable.bg_time_slot_selector)
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    private fun setupServiceMenu() {
        val bind = binding ?: return
        val serviceCard = bind.root.findViewById<MaterialCardView>(R.id.cardServiceMenu)
        serviceCard?.setOnClickListener {
            handleServiceSelection(serviceCard, "일반 펌", 80000)
        }
    }

    private fun handleServiceSelection(card: MaterialCardView, serviceName: String, price: Int) {
        val bind = binding ?: return
        val isFirstSelection = selectedServiceCard == null
        selectedServiceCard?.let {
            it.strokeColor = Color.parseColor("#E0E0E0")
            it.strokeWidth = dpToPx(1)
        }

        card.strokeColor = Color.parseColor("#4076FF")
        card.strokeWidth = dpToPx(2)
        selectedServiceCard = card
        selectedServicePrice = price

        if (isFirstSelection) {
            bind.root.postDelayed({
                bind.scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }, 200)
        }

        updateStepProgress()
        updateNextButton()
    }

    private fun setupVisitType() {
        val bind = binding ?: return

        val homeCard = bind.cardHomeService
        val visitCard = bind.cardVisitDesigner

        val radioHome = bind.radioHomeService
        val radioVisit = bind.radioVisitDesigner

        val blackTint = android.content.res.ColorStateList.valueOf(Color.BLACK)
        CompoundButtonCompat.setButtonTintList(radioHome, blackTint)
        CompoundButtonCompat.setButtonTintList(radioVisit, blackTint)

        updateCardBorder(homeCard, false)
        updateCardBorder(visitCard, false)
        radioHome.isChecked = false
        radioVisit.isChecked = false

        homeCard.setOnClickListener {
            if (selectedVisitType != "방문 서비스") {
                selectedVisitType = "방문 서비스"
                radioHome.isChecked = true
                radioVisit.isChecked = false
                updateCardBorder(homeCard, true)
                updateCardBorder(visitCard, false)
                updateStepProgress()
                updateNextButton()
            }
        }

        visitCard.setOnClickListener {
            if (selectedVisitType != "직접 방문") {
                selectedVisitType = "직접 방문"
                radioHome.isChecked = false
                radioVisit.isChecked = true
                updateCardBorder(homeCard, false)
                updateCardBorder(visitCard, true)
                updateStepProgress()
                updateNextButton()
            }
        }
    }

    private fun updateCardBorder(cardView: MaterialCardView, selected: Boolean) {
        cardView.strokeWidth = 0
    }

    private fun updateStepProgress() {
        val bind = binding ?: return
        val newStep = when {
            selectedVisitType != null -> 3
            selectedServiceCard != null -> 2
            selectedTime != null -> 1
            else -> 0
        }

        if (newStep != currentStep) {
            currentStep = newStep
            (bind.stepProgressView as? StepProgressView)?.setCurrentStep(currentStep)
        }
    }

    private fun isAllStepsComplete(): Boolean {
        return isDesignerViewed &&
                selectedDate != null &&
                selectedTime != null &&
                selectedServiceCard != null &&
                selectedVisitType != null
    }

    private fun updateNextButton() {
        val bind = binding ?: return
        val isComplete = isAllStepsComplete()
        bind.btnNextStep.isEnabled = isComplete
        bind.btnNextStep.backgroundTintList = if (isComplete) {
            ContextCompat.getColorStateList(requireContext(), R.color.blue_4076FF)
        } else {
            ContextCompat.getColorStateList(requireContext(), R.color.gray_CCCCCC)
        }

        bind.btnNextStep.text = if (isComplete) {
            "예약하기 (${formatPrice(selectedServicePrice)})"
        } else {
            "모든 항목을 선택해주세요"
        }
    }

    private fun smoothScrollToView(view: View) {
        val bind = binding ?: return
        isAutoScrolling = true
        bind.root.post {
            bind.scrollView.smoothScrollTo(0, view.top - dpToPx(20))
            bind.root.postDelayed({ isAutoScrolling = false }, 500)
        }
    }

    private fun formatPrice(price: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
        return "${formatter.format(price)}원"
    }

    private fun getSelectedServiceName(): String = "일반 펌"

    fun getSelectedTime(): String? = selectedButton?.text?.toString()

    fun getTotalPrice(): Int = selectedServicePrice

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()
}
