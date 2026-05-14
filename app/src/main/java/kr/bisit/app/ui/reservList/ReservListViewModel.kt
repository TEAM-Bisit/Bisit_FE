package kr.bisit.app.ui.reservList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kr.bisit.app.data.model.reservList.ReservationListItem
import kr.bisit.app.data.repository.reservList.ReservationRepository
import kotlinx.coroutines.launch

class ReservListViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ReservationRepository(application)

    fun setOnboardingMode(enabled: Boolean) {
        repository.setOnboardingMode(enabled)
    }

    private val _reservationList = MutableLiveData<List<ReservationListItem>>()
    val reservationList: LiveData<List<ReservationListItem>> = _reservationList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentPage = 0
    private var hasNext = true

    fun loadReservationList(
        shopId: Long,
        date: String? = null,
        status: String? = null,
        isRefresh: Boolean = false
    ) {
        if (_isLoading.value == true || !hasNext) return

        if (isRefresh) {
            currentPage = 0
            hasNext = true
            _reservationList.value = emptyList()
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getReservationList(
                    shopId = shopId,
                    date = date,
                    status = status,
                    page = currentPage
                )

                if (response.isSuccessful) {
                    response.body()?.data?.reservations?.let { pageData ->
                        val current = _reservationList.value.orEmpty()
                        _reservationList.value = current + pageData.content

                        hasNext = pageData.hasNext
                        currentPage++
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
