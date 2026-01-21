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
import com.example.bisit.data.model.customerShop.BusinessHourItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.reservation.TreatmentData
import com.example.bisit.databinding.FragmentCustomerReserveBinding
import com.example.bisit.data.model.shop.ShopDetailResponse
import com.example.bisit.data.model.shop.WeeklyBusinessHour
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
    private var treatmentCount: Int = 0
    private var shopName: String = ""
    private var staffDescription: String? = null

    // API data
    private var availableTimes: List<String> = emptyList()
    private var treatments: List<TreatmentData> = emptyList()
    private var storedBusinessHours: List<BusinessHourItem>? = null

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
        treatmentCount = arguments?.getInt("treatmentCount", 0) ?: 0
        shopName = arguments?.getString("shopName") ?: ""
        staffDescription = arguments?.getString("staffDescription")

        if (staffId == -1L || shopId == -1L) {
            Log.e("CustomerReserveFragment", "Missing staffId or shopId")
            Toast.makeText(requireContext(), "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            // Return empty root since we are popping back stack anyway
            return binding!!.root
        }

        setupDesignerInfo()
        setupDesignerComment()

        // Set dynamic shop name
        val tvShopName = binding?.root?.findViewById<TextView>(R.id.tvShopName)
        tvShopName?.text = if (shopName.isNotEmpty()) shopName else "매장 정보"

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
        
        stepView.setOnStepClickListener { step ->
            val bind = binding ?: return@setOnStepClickListener
            val dateTimeSection = bind.layoutTimeSlots.parent as View
            val serviceSection = bind.layoutServiceMenuSection.parent as View
            val visitSection = bind.radioGroupVisitType.parent as View
            
            val targetY = when(step) {
                1 -> dateTimeSection.top - dpToPx(16)
                2 -> serviceSection.top - dpToPx(16)
                3 -> visitSection.top - dpToPx(16)
                else -> 0
            }
            
            isAutoScrolling = true
            bind.scrollView.smoothScrollTo(0, targetY)
            bind.scrollView.postDelayed({ isAutoScrolling = false }, 600)
            
            currentStep = step
            stepView.setCurrentStep(currentStep)
        }

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
                    putString("staffImage", staffImage)
                    putInt("treatmentCount", treatmentCount)
                    putString("staffDescription", staffDescription)
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
            
            // Section offsets - using parent containers for better boundary detection
            // 0: Designer Info / Top
            // 1: Date/Time (starts around line 94 in XML)
            // 2: Service Menu (starts around line 202 in XML)
            // 3: Visit Type (starts around line 240 in XML)

            // Finding the parent LinearLayouts for these sections
            val dateTimeSection = bind.layoutTimeSlots.parent as View
            val serviceSection = bind.layoutServiceMenuSection.parent as View
            val visitSection = bind.radioGroupVisitType.parent as View

            // Improved boundary detection using the actual section tops
            val step = when {
                scrollY >= visitSection.top - 200 -> 3
                scrollY >= serviceSection.top - 200 -> 2
                scrollY >= dateTimeSection.top - 200 -> 1
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
            val isDateChanged = selectedDate?.timeInMillis != date.timeInMillis

            selectedDate = date
            calendarAdapter.setSelectedDate(date)

            if (isDateChanged) {
                resetTimeSelection()
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = dateFormat.format(date.time)

            loadStaffAvailability(staffId, dateStr)

            updateStepProgress()
        }


        binding?.rvCalendar?.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = calendarAdapter
            itemAnimator = null
        }

        updateCalendarDays()
        loadShopDetails(shopId)

        binding?.btnPrevMonth?.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendarDays()
        }

        binding?.btnNextMonth?.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendarDays()
        }
    }

    private fun loadShopDetails(shopId: Long) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getCustomerShopApi(requireContext())
                val response = api.getShopDetail(shopId)

                if (response.isSuccessful && response.body() != null) {
                    val shopData = response.body()?.data
                    // API 응답 타입에 맞춰 BusinessHourItem으로 처리 (혹은 WeeklyBusinessHour)
                    val businessHours = shopData?.weeklyBusinessHours ?: emptyList()
                    val closedDays = mutableSetOf<Int>()

                    businessHours.forEach { hour ->
                        if (hour.isClosed == true) {
                            val dayConstant = mapDayToCalendar(hour.day)
                            if (dayConstant != -1) {
                                closedDays.add(dayConstant)
                            }
                        }
                    }

                    if (closedDays.isNotEmpty()) {
                        Log.d("CustomerReserveFragment", "Closed days: $closedDays")
                        calendarAdapter.setClosedDays(closedDays)
                    }

                    // 전역 변수에 저장
                    storedBusinessHours = businessHours
                    Log.d("CustomerReserveFragment", "Loaded business hours from shop detail: ${storedBusinessHours?.size}")
                    
                    // 신규: 주간 영업시간이 로드되면 달력 UI를 갱신할 수 있음
                    selectedDate?.let { date ->
                        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time)
                        renderAvailableTimes(availableTimes, dateStr)
                    }
                }
            } catch (e: Exception) {
                Log.e("CustomerReserveFragment", "Error loading shop details", e)
            }
        }
    }

    private fun mapDayToCalendar(day: String?): Int {
        if (day == null) return -1
        val normalized = day.uppercase(Locale.ROOT)
        return when {
            normalized.contains("SUN") || normalized.contains("일요일") || normalized == "일" -> Calendar.SUNDAY
            normalized.contains("MON") || normalized.contains("월요일") || normalized == "월" -> Calendar.MONDAY
            normalized.contains("TUE") || normalized.contains("화요일") || normalized == "화" -> Calendar.TUESDAY
            normalized.contains("WED") || normalized.contains("수요일") || normalized == "수" -> Calendar.WEDNESDAY
            normalized.contains("THU") || normalized.contains("목요일") || normalized == "목" -> Calendar.THURSDAY
            normalized.contains("FRI") || normalized.contains("금요일") || normalized == "금" -> Calendar.FRIDAY
            normalized.contains("SAT") || normalized.contains("토요일") || normalized == "토" -> Calendar.SATURDAY
            else -> -1
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
                Log.d("CustomerReserveFragment", "Requesting availability -> staffId: $staffId, date: $date")
                val api = RetrofitClient.getReservationApi(requireContext())
                val response = api.getStaffAvailability(staffId, date)

                if (response.isSuccessful && response.body() != null) {
                    val availabilityResponse = response.body()!!

                    if (availabilityResponse.success && availabilityResponse.data != null) {
                        val data = availabilityResponse.data
                        staffName = data.staffName ?: ""
                        availableTimes = data.availableTimes ?: emptyList()

                        // 핵심 수정: filterNotNull()을 사용하여 List<TreatmentData>로 변환합니다.
                        val filteredTreatments = data.treatments?.filterNotNull() ?: emptyList()
                        treatments = filteredTreatments

                        renderAvailableTimes(availableTimes, date)
                        renderTreatments(filteredTreatments)

                        Log.d("CustomerReserveFragment", "Loaded ${availableTimes.size} slots and ${filteredTreatments.size} treatments")
                    } else {
                        Log.w("CustomerReserveFragment", "Empty availability data or success=false")
                        Toast.makeText(requireContext(), "예약 가능한 시간이 없습니다.", Toast.LENGTH_SHORT).show()
                        clearTimeSlots()
                        clearTreatments()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CustomerReserveFragment", "API call failed: ${response.code()}, Body: $errorBody")
                    Toast.makeText(requireContext(), "정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("CustomerReserveFragment", "Error loading availability", e)
                Toast.makeText(requireContext(), "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderAvailableTimes(availableTimes: List<String>, date: String) {
        val bind = binding ?: return
        bind.layoutTimeSlots.removeAllViews()
        
        Log.d("CustomerReserveFragment", "renderAvailableTimes called: date=$date, availableTimesCount=${availableTimes.size}")
        Log.d("CustomerReserveFragment", "availableTimes content: $availableTimes")

        // 1. Find Open/Close time for the date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = dateFormat.parse(date) ?: Date()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

        val dayString = when (dayOfWeek) {
            Calendar.SUNDAY -> "SUN"
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            else -> ""
        }
        val targetDay = mapDayToCalendar(dayString)

        val businessHour = storedBusinessHours?.find { 
            mapDayToCalendar(it.day) == targetDay
        }

        // Default range if not found (e.g., 10:00 - 20:00)
        val openTime = businessHour?.openFrom ?: "10:00"
        val closeTime = businessHour?.openTo ?: "20:00"

        // Generate full slots
        val fullSlots = generateTimeSlots(openTime, closeTime)

        val now = Calendar.getInstance()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)
        val currentHourMinute = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)

        val isToday = (date == todayStr)
        Log.d("CustomerReserveFragment", "renderAvailableTimes: date=$date, today=$todayStr, isToday=$isToday, now=$currentHourMinute")

        val morningTimes = mutableListOf<String>()
        val afternoonTimes = mutableListOf<String>()

        fullSlots.forEach { time ->
            if (time < "12:00") morningTimes.add(time) else afternoonTimes.add(time)
        }

        // Pre-calculate break info and normalize to "HH:mm"
        val rawBf = businessHour?.breakFrom
        val rawBt = businessHour?.breakTo
        val bf = rawBf?.let { if (it.length >= 5) it.substring(0, 5) else it }
        val bt = rawBt?.let { if (it.length >= 5) it.substring(0, 5) else it }
        
        Log.d("CustomerReserveFragment", "Break time for $dayString: normalized $bf - $bt (raw: $rawBf - $rawBt)")

        if (morningTimes.isNotEmpty()) {
            addSectionTitle("오전")
            addTimeGrid(morningTimes, availableTimes, date, bf, bt, isToday, currentHourMinute)
        }
        if (afternoonTimes.isNotEmpty()) {
            addSectionTitle("오후")
            addTimeGrid(afternoonTimes, availableTimes, date, bf, bt, isToday, currentHourMinute)
        }
    }

    private fun generateTimeSlots(
        openTime: String,
        closeTime: String,
        intervalMinutes: Int = 30
    ): List<String> {

        val result = mutableListOf<String>()
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

        val startDate = sdf.parse(openTime) ?: return emptyList()
        val endDate = sdf.parse(closeTime) ?: return emptyList()

        val cal = Calendar.getInstance()
        cal.time = startDate

        while (cal.time.before(endDate)) {
            result.add(sdf.format(cal.time))
            cal.add(Calendar.MINUTE, intervalMinutes)
        }

        return result
    }



    private fun addMinutes(time: String, minutes: Int): String {
        val parts = time.split(":")
        var h = parts[0].toInt()
        var m = parts[1].toInt()
        m += minutes
        if (m >= 60) {
            h += m / 60
            m %= 60
        }
        return String.format(Locale.US, "%02d:%02d", h, m)
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

    private fun addTimeGrid(
        times: List<String>, 
        availableSet: List<String>, 
        date: String,
        breakFrom: String?,
        breakTo: String?,
        isToday: Boolean,
        currentHM: String
    ) {
        val bind = binding ?: return
        val context = requireContext()
        val chunks = times.chunked(4)

        // API might return HH:mm:ss, normalize it
        val normalizedAvailableSet = availableSet.map { 
            if (it.length >= 5) it.substring(0, 5) else it
        }.toSet()

        chunks.forEach { chunk ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 0, 0, dpToPx(8))
            }

            chunk.forEach { slotTime ->
                // 1. Check if past
                val isPast = isToday && (slotTime <= currentHM)
                
                // 2. Break time blocking
                val isInBreak = if (!breakFrom.isNullOrEmpty() && !breakTo.isNullOrEmpty()) {
                    slotTime >= breakFrom && slotTime < breakTo
                } else false

                // 3. In availability set (from API)
                val isInSet = normalizedAvailableSet.contains(slotTime)

                // [FIX] Check for continuous availability if a treatment is already selected
                val isContinuouslyAvailable = if (isInSet && selectedTreatment != null) {
                    isContinuousAvailability(slotTime, selectedTreatment!!.durationMin, normalizedAvailableSet)
                } else isInSet

                val isAvailable = isContinuouslyAvailable && !isPast && !isInBreak

                Log.d("CustomerReserveFragment", "[SLOT CHECK] $slotTime | FINAL OK:$isAvailable | inSet:$isInSet | contAvail:$isContinuouslyAvailable | inBreak:$isInBreak | isPast:$isPast")

                val button = createTimeButton(
                    time = slotTime,
                    enabled = isAvailable
                )
                button.setTag(R.id.tag_original_enabled, isAvailable)
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

    private fun subtract30Minutes(time: String): String {
        try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = sdf.parse(time) ?: return ""
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MINUTE, -30)
            return sdf.format(cal.time)
        } catch (e: Exception) {
            return ""
        }
    }

    private fun createTimeButton(
        time: String,
        enabled: Boolean
    ): Button {
        return Button(requireContext()).apply {
            text = formatTimeDisplay(time)
            tag = time
            isAllCaps = false
            textSize = 13f
            stateListAnimator = null

            setBackgroundResource(R.drawable.bg_time_slot_selector)

            isEnabled = enabled
            alpha = if (enabled) 1.0f else 0.35f
            setTextColor(ContextCompat.getColor(context, R.color.black))

            if (enabled) {
                setOnClickListener {
                    handleTimeSelection(this, time)
                }
            }
            // ❗ enabled=false → 클릭 리스너 자체 없음
        }
    }


    private fun isTimeCurrentlySelectable(button: Button): Boolean {
        val time = button.tag as? String ?: return false

        // 1. 과거 시간 차단
        val now = Calendar.getInstance()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)
        val currentHM = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)

        val selectedDateStr = selectedDate?.let {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.time)
        }

        if (selectedDateStr == todayStr && time <= currentHM) {
            return false
        }

        // 2. duration 차단
        val duration = selectedTreatment?.durationMin
        val start = selectedTime
        if (duration != null && start != null && button != selectedButton) {
            if (isBlockedByDuration(time, start, duration)) {
                return false
            }
        }

        return true
    }


    private fun handleTimeSelection(clicked: Button, time: String) {
        // 이전 선택 해제
        selectedButton?.let {
            it.isSelected = false
            it.alpha = 1.0f
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        // 새 선택 설정
        selectedButton = clicked
        selectedTime = time

        clicked.isSelected = true
        clicked.alpha = 1.0f
        clicked.setTextColor(Color.WHITE)

        if (selectedTreatment != null && selectedTime != null) {
            updateTimeButtonsByDuration()
        }


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
        
        Log.d("CustomerReserveFragment", "setupDesignerInfo: staffName='$staffName', treatmentCount=$treatmentCount")

        // Set designer info
        if (staffName.isNotEmpty()) {
            tvDesignerName?.text = staffName
        } else {
            tvDesignerName?.text = "디자이너 이름 없음"
        }
        tvRecentCount?.text = "최근 시술 ${treatmentCount}회"
        
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

    private fun setupDesignerComment() {
        val bind = binding ?: return
        val root = bind.root
        
        val tvCommentContent = root.findViewById<TextView>(R.id.tvCommentContent)
        
        if (!staffDescription.isNullOrEmpty()) {
            tvCommentContent?.text = staffDescription
        } else {
            tvCommentContent?.text = "시술 후 관리 잘 부탁드려요 😊" // 기본값
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

        // Ensure RadioButton color is black
        val blackStateList = android.content.res.ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                Color.BLACK,
                Color.BLACK
            )
        )
        radioHome?.let { CompoundButtonCompat.setButtonTintList(it, blackStateList) }
        radioVisit?.let { CompoundButtonCompat.setButtonTintList(it, blackStateList) }
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

        // Filter time slots based on service duration
        filterTimeSlotsByDuration(treatment.durationMin)

        updateStepProgress()
        updateNextButton()
        
        // Scroll to bottom
        binding?.scrollView?.post {
            binding?.scrollView?.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun updateStepProgress() {
        // ...Existing progress logic if any...
        updateNextButton()
    }

    private fun updateNextButton() {
        val isComplete = isAllStepsComplete()
        binding?.btnNextStep?.apply {
            isEnabled = isComplete
            // Visual feedback for disabled state
            if (isComplete) {
                backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4076FF"))
                alpha = 1.0f
            } else {
                backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#E0E0E0"))
                alpha = 0.5f
            }
        }
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
    
    private fun filterTimeSlotsByDuration(durationMin: Int) {
        val bind = binding ?: return
        val selectedDateCal = selectedDate ?: return
        
        // Get closing time and break time for the selected date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(selectedDateCal.time)
        
        val dayOfWeek = selectedDateCal.get(Calendar.DAY_OF_WEEK)
        val dayString = when (dayOfWeek) {
            Calendar.SUNDAY -> "SUN"
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            else -> ""
        }
        val targetDay = mapDayToCalendar(dayString)
        
        val businessHour = storedBusinessHours?.find { 
            mapDayToCalendar(it.day) == targetDay
        }
        
        val closeTime = businessHour?.openTo?.let { 
            if (it.length >= 5) it.substring(0, 5) else it 
        } ?: "20:00"
        
        val breakFrom = businessHour?.breakFrom?.let { 
            if (it.length >= 5) it.substring(0, 5) else it 
        }
        val breakTo = businessHour?.breakTo?.let { 
            if (it.length >= 5) it.substring(0, 5) else it 
        }
        
        val normalizedAvailableSet = availableTimes.map { 
            if (it.length >= 5) it.substring(0, 5) else it
        }.toSet()

        Log.d("CustomerReserveFragment", "filterTimeSlotsByDuration: duration=$durationMin, closeTime=$closeTime, break=$breakFrom~$breakTo, availSetSize=${normalizedAvailableSet.size}")
        
        // Update all time slot buttons
        updateTimeSlotButtons(bind.layoutTimeSlots, durationMin, closeTime, breakFrom, breakTo, normalizedAvailableSet)
    }
    
    private fun updateTimeSlotButtons(
        container: ViewGroup,
        durationMin: Int,
        closeTime: String,
        breakFrom: String?,
        breakTo: String?,
        availableSet: Set<String>
    ) {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is ViewGroup) {
                updateTimeSlotButtons(child, durationMin, closeTime, breakFrom, breakTo, availableSet)
            } else if (child is Button) {
                val timeText = child.text.toString()
                val originalEnabled = child.getTag(R.id.tag_original_enabled) as? Boolean ?: true
                
                if (!originalEnabled) {
                    continue
                }
                
                val wouldExceedClosing = wouldExceedClosingTime(timeText, durationMin, closeTime)
                val wouldOverlapBreak = if (!breakFrom.isNullOrEmpty() && !breakTo.isNullOrEmpty()) {
                    wouldOverlapBreakTime(timeText, durationMin, breakFrom, breakTo)
                } else false
                
                val isContinuouslyAvailable = isContinuousAvailability(timeText, durationMin, availableSet)
                
                val shouldDisable = wouldExceedClosing || wouldOverlapBreak || !isContinuouslyAvailable
                
                if (shouldDisable) {
                    child.isEnabled = false
                    child.alpha = 0.3f
                    Log.d("CustomerReserveFragment", "Disabled slot $timeText: exceedClose=$wouldExceedClosing, overlapBreak=$wouldOverlapBreak, contAvail=$isContinuouslyAvailable")
                } else {
                    child.isEnabled = true
                    child.alpha = 1.0f
                }
            }
        }
    }
    
    private fun wouldExceedClosingTime(startTime: String, durationMin: Int, closeTime: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val start = sdf.parse(startTime) ?: return false
            val close = sdf.parse(closeTime) ?: return false
            
            val cal = Calendar.getInstance()
            cal.time = start
            cal.add(Calendar.MINUTE, durationMin)
            val endTime = cal.time
            
            endTime.after(close)
        } catch (e: Exception) {
            Log.e("CustomerReserveFragment", "Error checking closing time", e)
            false
        }
    }
    
    private fun wouldOverlapBreakTime(
        startTime: String,
        durationMin: Int,
        breakFrom: String,
        breakTo: String
    ): Boolean {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val start = sdf.parse(startTime) ?: return false
            val breakStart = sdf.parse(breakFrom) ?: return false
            val breakEnd = sdf.parse(breakTo) ?: return false
            
            val cal = Calendar.getInstance()
            cal.time = start
            cal.add(Calendar.MINUTE, durationMin)
            val endTime = cal.time
            
            // Service overlaps break if: start < breakEnd AND end > breakStart
            val overlaps = start.before(breakEnd) && endTime.after(breakStart)
            if (overlaps) {
                Log.d("CustomerReserveFragment", "[OVERLAP] $startTime + ${durationMin}m overlaps break $breakFrom~$breakTo (end: ${sdf.format(endTime)})")
            }
            overlaps
        } catch (e: Exception) {
            Log.e("CustomerReserveFragment", "Error checking break overlap", e)
            false
        }
    }

    private fun isContinuousAvailability(
        startTime: String,
        durationMin: Int,
        availableSet: Set<String>
    ): Boolean {
        // Round up to nearest 30 min block
        val blocks = (durationMin + 29) / 30
        var current = startTime
        for (i in 0 until blocks) {
            if (!availableSet.contains(current)) {
                Log.d("CustomerReserveFragment", "[CONT FAIL] $startTime + ${durationMin}m: block $current not in set")
                return false
            }
            current = addMinutes(current, 30)
        }
        return true
    }
    
    private fun isBlockedByDuration(
        candidate: String,
        start: String,
        durationMin: Int
    ): Boolean {

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

        val startDate = sdf.parse(start) ?: return false
        val candidateDate = sdf.parse(candidate) ?: return false

        val cal = Calendar.getInstance()
        cal.time = startDate
        cal.add(Calendar.MINUTE, durationMin)

        val endDate = cal.time

        return (candidateDate == startDate) || (candidateDate.after(startDate) && candidateDate.before(endDate))
    }



    private fun updateTimeButtonsByDuration() {
        val bind = binding ?: return
        val duration = selectedTreatment?.durationMin ?: return
        val start = selectedTime ?: return

        updateButtonsRecursive(bind.layoutTimeSlots, start, duration)
    }

    private fun updateButtonsRecursive(
        view: View,
        start: String,
        duration: Int
    ) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                updateButtonsRecursive(view.getChildAt(i), start, duration)
            }
            return
        }

        if (view !is Button) return

        val time = view.tag as? String ?: return

        // 🔒 선택된 버튼은 절대 건드리지 않음
        if (view == selectedButton) {
            return
        }

        val blocked = isBlockedByDuration(time, start, duration)

        if (blocked) {
            view.alpha = 0.4f
            view.isEnabled = false
        } else {
            view.alpha = 1.0f
        }

        view.isSelected = false
        view.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }

    private fun resetTimeSelection() {
        selectedTime = null
        selectedButton = null

        val bind = binding ?: return
        resetButtonsRecursive(bind.layoutTimeSlots)

        updateStepProgress()
        updateNextButton()
    }

    private fun resetButtonsRecursive(view: View) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                resetButtonsRecursive(view.getChildAt(i))
            }
            return
        }

        if (view !is Button) return

        val originalEnabled = view.getTag(R.id.tag_original_enabled) as? Boolean ?: true

        view.isSelected = false
        view.alpha = if (originalEnabled) 1.0f else 0.35f
        view.isEnabled = originalEnabled
        view.setBackgroundResource(R.drawable.bg_time_slot_selector)
        view.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }



}
