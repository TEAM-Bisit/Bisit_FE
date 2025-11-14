package com.example.bisit.ui.dialog // (공용 패키지)

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.example.bisit.databinding.DialogCustomBinding // 바인딩 이름 변경

class CustomDialog( // 클래스 이름 변경
    private val title: String,
    private val subtitle: String? = null, // 서브타이틀은 선택적 (null 가능)
    private val onConfirm: (() -> Unit)? = null // 닫기 버튼 클릭 시 동작 (선택적)
) : DialogFragment() {

    private var _binding: DialogCustomBinding? = null // 바인딩 타입 변경
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCustomBinding.inflate(inflater, container, false) // 바인딩 이름 변경
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 전달받은 텍스트로 설정
        binding.tvTitle.text = title

        // 2. 서브타이틀이 null이 아니면 보여주고, null이면 숨김
        binding.tvSubtitle.text = subtitle
        binding.tvSubtitle.isVisible = (subtitle != null)

        // 3. 닫기 버튼
        binding.btnClose.setOnClickListener {
            onConfirm?.invoke() // 전달받은 동작(onConfirm)이 있으면 실행
            dismiss() // 다이얼로그 닫기
        }
    }

    override fun onStart() {
        super.onStart()
        // 다이얼로그의 가로 폭을 화면 전체로 설정
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}