<<<<<<<< Updated upstream:app/src/main/java/com/example/naottae/ui/myPageOwner/MyPageOwnerFragment.kt
package com.example.naeottae.ui.myPageOwner
========
package com.example.naottae.ui.reservation
>>>>>>>> Stashed changes:app/src/main/java/com/example/naottae/ui/reservation/ReservationFragment.kt

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
<<<<<<<< Updated upstream:app/src/main/java/com/example/naottae/ui/myPageOwner/MyPageOwnerFragment.kt
import com.example.naeottae.R
import com.example.naeottae.databinding.FragmentMyPageOwnerBinding
========
import com.example.naottae.databinding.FragmentReservationBinding
>>>>>>>> Stashed changes:app/src/main/java/com/example/naottae/ui/reservation/ReservationFragment.kt

class MyPageOwnerFragment : Fragment() {
    private var _binding: FragmentMyPageOwnerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageOwnerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}