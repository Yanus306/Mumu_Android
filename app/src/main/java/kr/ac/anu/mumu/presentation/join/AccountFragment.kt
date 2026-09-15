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
import androidx.transition.TransitionManager
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    // Activity와 공유하는 ViewModel
    private val viewModel: JoinViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 텍스트 색상 변경
        viewSet()

        binding.etId.setText(viewModel.inputId.value.orEmpty())
        binding.etPw.setText(viewModel.inputPw.value.orEmpty())
        binding.etPwCheck.setText(viewModel.inputPwCheck.value.orEmpty())

        // 입력값 리스너
        binding.etId.addTextChangedListener {
            viewModel.inputId.value = it.toString()
            viewModel.checkButtonEnabled()
            viewModel.isIdErrorVisible.value = false
        }
        binding.etPw.addTextChangedListener {
            viewModel.inputPw.value = it.toString()
            viewModel.checkButtonEnabled()
            viewModel.isPwErrorVisible.value = false
        }
        binding.etPwCheck.addTextChangedListener {
            viewModel.inputPwCheck.value = it.toString()
            viewModel.checkButtonEnabled()
            viewModel.isPwCheckErrorVisible.value = false
        }
        binding.etId.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_NEXT) {
                viewModel.onNextClick()
                true
            } else {
                false
            }
        }
        binding.etPw.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_NEXT) {
                viewModel.onNextClick()
                true
            } else {
                false
            }
        }
        binding.etPwCheck.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                viewModel.onNextClick()
                true
            } else {
                false
            }
        }

        // 단계별 UI 오픈
        viewModel.accountStep.observe(viewLifecycleOwner) { step ->
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
            binding.groupStepPw.visibility = if (step >= 1) View.VISIBLE else View.GONE

            binding.groupStepPwCheck.visibility = if (step >= 2) View.VISIBLE else View.GONE

            when (step) {
                1 -> binding.etPw.requestFocus()
                2 -> binding.etPwCheck.requestFocus()
            }

            viewModel.checkButtonEnabled()
        }

        // 에러 메시지 UI 반영
        viewModel.isIdErrorVisible.observe(viewLifecycleOwner) { isVisible ->
            binding.tvIdError.visibility = if (isVisible) View.VISIBLE else View.GONE
            if (isVisible) binding.etId.requestFocus()
        }
        viewModel.idErrorMessage.observe(viewLifecycleOwner) { binding.tvIdError.text = it }
        viewModel.isPwErrorVisible.observe(viewLifecycleOwner) { isVisible ->
            binding.tvPwError.visibility = if (isVisible) View.VISIBLE else View.GONE
        }
        viewModel.isPwCheckErrorVisible.observe(viewLifecycleOwner) { isVisible ->
            binding.tvPwCheckError.visibility = if (isVisible) View.VISIBLE else View.GONE
        }
    }

    private fun viewSet() {
        val originText = binding.tvTitle.text.toString()
        val spannable = SpannableStringBuilder(originText)

        val targetWord = "Mumu"
        val start = originText.indexOf(targetWord)
        val end = start + targetWord.length

        if (start != -1) {
            context?.let { ctx ->
                val color = ContextCompat.getColor(ctx, R.color.mumu_300)

                spannable.setSpan(
                    ForegroundColorSpan(color),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        binding.tvTitle.text = spannable
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
