package com.example.bisit.ui.customerReserve

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.reservation.TreatmentData
import com.example.bisit.databinding.FragmentCustomerReserveBinding
import com.example.bisit.widget.StepProgressView
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
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
    private var selectedTreatment: TreatmentData? = null

    private var selectedVisitType: String? = null

    private var isAutoScrolling = false

    private var scrollChangedListener: ViewTreeObserver.OnScrollChangedListener? = null

    // API parameters
    private var staffId: Long = -1L
    private var shopId: Long = -1L
    private var staffName: String = ""
    private var staffImage: String? = null
    private var reviewCount: Int = 0
    private var shopName: String = ""

    // API data
    private var availableTimes: List<String> = emptyList()
    private var treatments: List<TreatmentData> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerReserveBinding.inflate(inflater, container, false)

        // Get arguments
        staffId = arguments?.getLong("staffId") ?: -1L
        shopId = arguments?.getLong("shopId") ?: -1L
        staffName = arguments?.getString("staffName") ?: ""
        staffImage = arguments?.getString("staffImage")
        reviewCount = arguments?.getInt("reviewCount", 0) ?: 0
        shopName = arguments?.getString("shopName") ?: ""

        if (staffId == -1L || shopId == -1L) {
            Log.e("CustomerReserveFragment", "Missing staffId or shopId")
            Toast.makeText(requireContext(), "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
        }

        setupDesignerInfo()

        setupStepProgress()
        setupCalendar()
        setupServiceMenu()
        setupVisitType()
        setupBackButton()
        setupScrollListener()
        updateNextButton()

        return binding!!.root
    }

    override fun onResume() {
        super.onResume()
        // 상태바 색상을 흰색으로 설정
        activity?.window?.statusBarColor = android.graphics.Color.WHITE
        activity?.window?.decorView?.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        // 서비스 메뉴 섹션 배경색을 흰색으로 설정
        binding?.layoutServiceMenuSection?.setBackgroundColor(android.graphics.Color.WHITE)
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
                    putLong("shopId", shopId)
                    putLong("staffId", staffId)
                    putLong("treatmentId", selectedTreatment?.treatmentId ?: 0L)
                    putString("selectedDate", dateStr)
                    putString("selectedTime", selectedTime)
                    putString("visitType", selectedVisitType)
                    putInt("totalPrice", selectedServicePrice)
                    putString("serviceName", getSelectedServiceName())
                    putString("staffName", staffName)
                    putString("shopName", shopName)
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

            // Force step 0 at the very top
            if (scrollY == 0) {
                if (currentStep != 0) {
                    currentStep = 0
                    (bind.stepProgressView as? StepProgressView)?.setCurrentStep(currentStep)
                }
                return@OnScrollChangedListener
            }

            val serviceSectionTop = bind.layoutServiceMenuSection.top
            val visitSectionTop = bind.radioGroupVisitType.top

            if (serviceSectionTop == 0 || visitSectionTop == 0) return@OnScrollChangedListener

            // Adjusted thresholds to be more responsive
            val step = when {
                scrollY >= visitSectionTop - 300 -> 3
                scrollY >= serviceSectionTop - 300 -> 2
                scrollY >= 200 -> 1
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
            loadStaffAvailability(staffId, dateStr)
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

    private fun loadStaffAvailability(staffId: Long, date: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getReservationApi(requireContext())
                val response = api.getStaffAvailability(staffId, date)

                if (response.isSuccessful && response.body() != null) {
                    val availabilityResponse = response.body()!!
                    if (availabilityResponse.success && availabilityResponse.data != null) {
                        val data = availabilityResponse.data
                        staffName = data.staffName
                        availableTimes = data.availableTimes
                        treatments = data.treatments

                        renderAvailableTimes(availableTimes, date)
                        renderTreatments(treatments)

                        Log.d("CustomerReserveFragment", "Loaded ${availableTimes.size} time slots and ${treatments.size} treatments")
                    } else {
                        Log.w("CustomerReserveFragment", "Empty availability data")
                        Toast.makeText(requireContext(), "예약 가능한 시간이 없습니다.", Toast.LENGTH_SHORT).show()
                        clearTimeSlots()
                        clearTreatments()
                    }
                } else {
                    Log.e("CustomerReserveFragment", "API call failed: ${response.code()}")
                    Toast.makeText(requireContext(), "예약 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("CustomerReserveFragment", "Error loading availability", e)
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderAvailableTimes(times: List<String>, date: String) {
        val bind = binding ?: return
        bind.layoutTimeSlots.removeAllViews()

        if (times.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "예약 가능한 시간이 없습니다."
                textSize = 14f
                setTextColor(Color.parseColor("#999999"))
                setPadding(0, dpToPx(16), 0, dpToPx(16))
            }
            bind.layoutTimeSlots.addView(emptyText)
            return
        }

        val morningTimes = times.filter { it < "12:00" }
        val afternoonTimes = times.filter { it >= "12:00" }

        if (morningTimes.isNotEmpty()) {
            addSectionTitle("오전")
            addTimeGrid(morningTimes, emptySet())
        }

        if (afternoonTimes.isNotEmpty()) {
            addSectionTitle("오후")
            addTimeGrid(afternoonTimes, emptySet())
        }
    }

    private fun clearTimeSlots() {
        binding?.layoutTimeSlots?.removeAllViews()
    }

    private fun clearTreatments() {
        treatments = emptyList()
        binding?.layoutServiceMenuSection?.removeAllViews()
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
            // Format time to show only HH:mm (remove seconds if present)
            text = formatTimeDisplay(time)
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
        // Clear placeholder views defined in XML
        binding?.layoutServiceMenuSection?.removeAllViews()
    }

    // ... (renderTreatments and other methods remain same)

    private fun setupDesignerInfo() {
        val bind = binding ?: return
        val root = bind.root
        
        // Find views from included layout
        val tvDesignerName = root.findViewById<TextView>(R.id.tvDesignerName)
        val tvRecentCount = root.findViewById<TextView>(R.id.tvRecentCount)
        val ivProfile = root.findViewById<android.widget.ImageView>(R.id.ivProfile)
        
        Log.d("CustomerReserveFragment", "setupDesignerInfo: staffName='$staffName', reviewCount=$reviewCount")

        // Set designer info
        if (staffName.isNotEmpty()) {
            tvDesignerName?.text = staffName
        } else {
            tvDesignerName?.text = "디자이너 이름 없음"
        }
        tvRecentCount?.text = "최근 시술 ${reviewCount}회"
        
        // Load designer image
        if (!staffImage.isNullOrEmpty() && ivProfile != null) {
            Glide.with(this)
                .load(staffImage)
                .placeholder(R.drawable.img_designer)
                .error(R.drawable.img_designer)
                .centerCrop()
                .into(ivProfile)
        }
    }
    private fun setupVisitType() {
        val cardHome = binding?.root?.findViewById<MaterialCardView>(R.id.card_home_service)
        val cardVisit = binding?.root?.findViewById<MaterialCardView>(R.id.card_visit_designer)
        val radioHome = binding?.root?.findViewById<android.widget.RadioButton>(R.id.radio_home_service)
        val radioVisit = binding?.root?.findViewById<android.widget.RadioButton>(R.id.radio_visit_designer)

        fun updateSelection(isHome: Boolean) {
            selectedVisitType = if (isHome) "person" else "vehicle"
            
            radioHome?.isChecked = isHome
            radioVisit?.isChecked = !isHome
            
            cardHome?.strokeColor = if (isHome) ContextCompat.getColor(requireContext(), R.color.blue_4076FF) else Color.TRANSPARENT
            cardHome?.strokeWidth = if (isHome) dpToPx(2) else dpToPx(1)
            
            cardVisit?.strokeColor = if (!isHome) ContextCompat.getColor(requireContext(), R.color.blue_4076FF) else Color.TRANSPARENT
            cardVisit?.strokeWidth = if (!isHome) dpToPx(2) else dpToPx(1)

            updateStepProgress()
            updateNextButton()
        }

        cardHome?.setOnClickListener { updateSelection(true) }
        cardVisit?.setOnClickListener { updateSelection(false) }
        
        // Initial state
        radioHome?.isChecked = false
        radioVisit?.isChecked = false
    }

    private fun renderTreatments(treatments: List<TreatmentData>) {
        val bind = binding ?: return
        bind.layoutServiceMenuSection.removeAllViews()

        if (treatments.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "시술 메뉴가 없습니다."
                textSize = 14f
                setTextColor(Color.parseColor("#999999"))
                setPadding(0, dpToPx(16), 0, dpToPx(16))
            }
            bind.layoutServiceMenuSection.addView(emptyText)
            return
        }

        val inflater = LayoutInflater.from(requireContext())
        treatments.forEach { treatment ->
            val itemView = inflater.inflate(R.layout.item_service_menu, bind.layoutServiceMenuSection, false) as MaterialCardView
            
            val tvName = itemView.findViewById<TextView>(R.id.tvServiceName)
            val tvTime = itemView.findViewById<TextView>(R.id.tvServiceTime)
            val tvDesc = itemView.findViewById<TextView>(R.id.tvServiceDescription)
            val tvPrice = itemView.findViewById<TextView>(R.id.tvServicePrice)

            tvName.text = treatment.treatmentName
            tvTime.text = "${treatment.durationMin}분"
            tvDesc.text = "" // Description not available in data model
            
            val priceFormat = NumberFormat.getNumberInstance(Locale.KOREA)
            tvPrice.text = "${priceFormat.format(treatment.price)}원"

            itemView.setOnClickListener {
                handleServiceSelection(itemView, treatment)
            }

            bind.layoutServiceMenuSection.addView(itemView)
        }
    }

    private fun handleServiceSelection(card: MaterialCardView, treatment: TreatmentData) {
        // Deselect previous
        selectedServiceCard?.apply {
            strokeColor = Color.parseColor("#E0E0E0")
            strokeWidth = dpToPx(1)
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
        }

        // Select new
        selectedServiceCard = card
        selectedTreatment = treatment
        selectedServicePrice = treatment.price
        
        card.strokeColor = ContextCompat.getColor(requireContext(), R.color.blue_4076FF)
        card.strokeWidth = dpToPx(2)
        card.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F0F7FF"))

        updateStepProgress()
        updateNextButton()
        
        // Scroll to bottom
        binding?.scrollView?.post {
            binding?.scrollView?.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun updateStepProgress() {
        var progress = 0
        if (selectedDate != null) progress = 1
        if (selectedTime != null) progress = 2
        if (selectedTreatment != null) progress = 3
        if (selectedVisitType != null) progress = 4
        
        // Map to 0-3 range for StepProgressView (which has 4 steps)
        // Step 0: Date/Time (combined logic in UI flow, but here separated)
        // Let's align with scroll listener logic:
        // 0: Initial
        // 1: Date Selected
        // 2: Time Selected
        // 3: Service Selected
        
        // Actually, let's just update based on what's done
        // The scroll listener updates currentStep based on scroll position.
        // Here we might want to advance the step index if we want to show progress.
        // But the StepProgressView seems to be about "current active step" rather than "completed steps".
        // So maybe we don't need to force set it here unless we want to jump.
        // Let's just leave it to scroll listener or manual jumps.
        
        // However, the error log says updateStepProgress is missing.
        // So I must provide it.
        // Let's make it check completion and maybe update UI if needed.
        
        if (isAllStepsComplete()) {
             // Maybe highlight all?
        }
    }

    private fun updateNextButton() {
        binding?.btnNextStep?.isEnabled = isAllStepsComplete()
    }

    private fun isAllStepsComplete(): Boolean {
        return selectedDate != null && 
               selectedTime != null && 
               selectedTreatment != null && 
               selectedVisitType != null
    }

    private fun getSelectedServiceName(): String {
        return selectedTreatment?.treatmentName ?: ""
    }

    private fun formatTimeDisplay(time: String): String {
        return if (time.length > 5) time.substring(0, 5) else time
    }

    private fun smoothScrollToView(view: View) {
        isAutoScrolling = true
        binding?.scrollView?.post {
            binding?.scrollView?.smoothScrollTo(0, view.top)
            binding?.scrollView?.postDelayed({ isAutoScrolling = false }, 500)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
    
    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }
}
