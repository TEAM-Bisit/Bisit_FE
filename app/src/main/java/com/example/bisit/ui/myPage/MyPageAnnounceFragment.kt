package com.example.bisit.ui.myPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R

class MyPageAnnounceFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_page_announce, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_announce)

        val adapter = MyPageAnnounceAdapter(
            listOf(
                "공지 제목 1" to "1시간 전",
                "공지 제목 2" to "3시간 전",
                "공지 제목 3" to "1일 전"
            )
        ) { position ->
            findNavController().navigate(R.id.action_myPageAnnounceFragment_to_myPageAnnounceDetailFragment)
        }

        recyclerView.adapter = adapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
    }
}
