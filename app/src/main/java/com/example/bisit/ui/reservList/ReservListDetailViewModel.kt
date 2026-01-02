package com.example.bisit.ui.reservList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bisit.data.model.reservList.ReservationDetailData
import com.example.bisit.data.repository.reservList.ReservationRepository
import kotlinx.coroutines.launch

class ReservListDetailViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ReservationRepository(application)

    private val _reservationDetail = MutableLiveData<ReservationDetailData>()
    val reservationDetail: LiveData<ReservationDetailData> = _reservationDetail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadReservationDetail(reservationId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getReservationDetail(reservationId)
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        _reservationDetail.value = it
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
