package kr.bisit.app.ui.todayReserv

import android.view.View

interface TodayApproveTargetProvider {
    fun getApproveButtonForGuide(): View?
}

interface TodayStatusTargetProvider {
    fun getChangeStatusButtonForGuide(): View?
}