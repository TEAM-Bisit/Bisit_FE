package kr.bisit.app.ui.customerReserve

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kr.bisit.app.R
import kr.bisit.app.databinding.FragmentCustomerReserveCompleteBinding

class CustomerReserveCompleteFragment : Fragment() {

    private var _binding: FragmentCustomerReserveCompleteBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCustomerReserveCompleteBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // arguments에서 데이터 가져오기
        val shopName = arguments?.getString("shopName") ?: "알 수 없는 매장"
        val staffName = arguments?.getString("staffName") ?: "알 수 없는 디자이너"
        val serviceName = arguments?.getString("serviceName") ?: "알 수 없는 서비스"
        val date = arguments?.getString("reservedDate") ?: ""
        val time = arguments?.getString("reservedTime") ?: ""
        val orderId = arguments?.getString("orderId") ?: ""
        val reservationId = arguments?.getLong("reservationId", -1L) ?: -1L

        binding?.apply {
            tvShopName.text = shopName
            tvDesignerName.text = staffName
            tvServiceName.text = serviceName
            tvSchedule.text = formatScheduleText(date, time)
            tvReservationNumber.text = orderId

            btnCheckHistory.setOnClickListener {
                findNavController().navigate(
                    R.id.CustomerMyReserve, 
                    null, 
                    androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true) // 홈까지 백스택 모두 날리고 새로 시작할 수도 있음. 여기서는 홈/지도 등으로 돌아가고 싶을 수 있으나 요청대로 내역 화면으로.
                        // 내역 화면이 탭인 경우 단순 이동이 아닐 수 있지만, nav_graph상 fragment라면 action이나 id로 이동.
                        // 단, CustomerMyReserve가 BottomNav 탭 중 하나라면 Global Action으로 처리되거나 id 이동.
                        .build()
                )
            }
        }
    }

    private fun formatScheduleText(date: String, time: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("M.d (E)", java.util.Locale.KOREAN)
            val parsedDate = inputFormat.parse(date)
            val formattedDate = parsedDate?.let { outputFormat.format(it) } ?: date
            val formattedTime = if (time.length > 5) time.substring(0, 5) else time
            "$formattedDate • $formattedTime"
        } catch (e: Exception) { "$date • $time" }
    }

    override fun onResume() {
        super.onResume()
        // 상태바 흰색
        activity?.window?.statusBarColor = android.graphics.Color.WHITE
        activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
