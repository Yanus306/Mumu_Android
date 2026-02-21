package kr.ac.anu.mumu.presentation.join

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.FragmentPhoneBinding

class PhoneFragment : Fragment() {
    private var _binding: FragmentPhoneBinding? = null
    private val binding get() = _binding!!
    private val viewModel: JoinViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPhoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.checkPhoneStep()

        viewModel.isVerificationVisible.observe(viewLifecycleOwner) { isVisible ->
            android.transition.TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
            binding.groupStepCheckNum.visibility = if (isVisible) View.VISIBLE else View.GONE
        }

        binding.etPhoneNum.addTextChangedListener(object : TextWatcher {

            private var isFormatting = false // 무한 루프 방지용 플래그

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return

                isFormatting = true

                // 입력 값중 숫자만 추출
                val phoneNumber = s.toString().replace(Regex("[^0-9]"), "")

                // viewModel에 숫자만 저장
                viewModel.inputPhoneNum.value = phoneNumber

                // 화면에 보여줄 하이픈 포맷 만들기
                val formattedNumber = formatPhoneNumber(phoneNumber)
                s.replace(0, s.length, formattedNumber)

                viewModel.checkPhoneStep()

                isFormatting = false

            }
        })

        viewModel.isCheckNumErrorVisible.observe(viewLifecycleOwner) { isError ->
            if (isError) {
                binding.etCheckNum.setBackgroundResource(R.drawable.bg_edit_error_underline)
            } else {
                binding.etCheckNum.setBackgroundResource(R.drawable.bg_edit_underline)
            }
        }

        binding.etCheckNum.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(s: Editable?) {
                viewModel.inputCheckNum.value = s?.toString() ?: ""

                viewModel.isCheckNumErrorVisible.value = false

                viewModel.checkPhoneButtonEnabled()
            }
        })
    }

    private fun formatPhoneNumber(raw: String): String {
        return when {
            raw.length <= 3 -> raw
            raw.length <= 7 -> "${raw.substring(0, 3)}-${raw.substring(3)}"
            else -> {
                val limit = if (raw.length > 11) raw.substring(0, 11) else raw
                "${limit.substring(0, 3)}-${limit.substring(3, 7)}-${limit.substring(7)}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}