package com.example.bisit.ui.signUp

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bisit.databinding.FragmentTermsDetailBinding

class TermsDetailFragment : Fragment() {

    private var _binding: FragmentTermsDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTermsDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val content = requireArguments().getString("termContent")

        binding.tvTermsContent.text = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}