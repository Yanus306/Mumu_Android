package kr.ac.anu.mumu.presentation.join

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.FragmentAddressBinding

class AddressFragment : Fragment() {
    private var _binding: FragmentAddressBinding? = null
    private val binding get() = _binding!!
    private val viewModel: JoinViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewSet()

        binding.etAddress.setText(viewModel.inputDetailAddress.value.orEmpty())
        viewModel.checkAddressStep()

        viewModel.inputPostalCode.observe(viewLifecycleOwner) { postal ->
            binding.tvPostalCode.text = postal
        }

        viewModel.inputAddress.observe(viewLifecycleOwner) { address ->
            binding.tvAddress.text = address
        }

        binding.btnSearch.setOnClickListener {
            // TODO 카카오 API
            viewModel.inputPostalCode.value = "36729"
            viewModel.inputAddress.value = "경상북도 안동시 경동로 1375"
        }

        binding.etAddress.addTextChangedListener { text ->
            viewModel.inputDetailAddress.value = text.toString()

            viewModel.checkAddressStep()
        }
        binding.etAddress.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                viewModel.onNextClick()
                true
            } else {
                false
            }
        }
    }

    private fun viewSet() {
        val originText = binding.tvTitle.text.toString()
        val spannable = SpannableStringBuilder(originText)

        val targetWord = "Mumu"
        val start = originText.indexOf(targetWord)
        val end = start + targetWord.length

        if (start != -1) {
            val color = ContextCompat.getColor(requireContext(), R.color.mumu_300)

            spannable.setSpan(
                ForegroundColorSpan(color),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.tvTitle.text = spannable
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
