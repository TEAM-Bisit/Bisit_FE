package com.example.bisit.ui.dialog

import android.content.DialogInterface // onCancel을 위해 import
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.bisit.databinding.DialogFindIdBinding

class FindIdDialog(
    private val name: String,
    private val foundId: String,
    // --- 1. "닫힐 때" 실행할 람다 함수를 생성자에 추가 ---
    private val onDismissCallback: () -> Unit
) : DialogFragment() {

    private var _binding: DialogFindIdBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFindIdBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    // onStart()는 가로 폭 설정 (이전 단계에서 추가함)
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDialogMessage.text = "${name} 님의 아이디입니다."
        binding.tvFoundId.text = foundId

        binding.btnClose.setOnClickListener {
            // --- 2. 닫기 버튼 클릭 시 콜백 실행 ---
            onDismissCallback.invoke()
            dismiss()
        }
    }

    // --- 3. 바깥쪽을 눌러 닫힐 때도(onCancel) 콜백 실행 ---
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onDismissCallback.invoke()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}