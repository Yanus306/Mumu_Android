package kr.ac.anu.mumu.presentation.join

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kr.ac.anu.mumu.databinding.FragmentTermsBinding

class TermsFragment : Fragment() {
    private var _binding: FragmentTermsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: JoinViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTermsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.checkAgreementStep()

        // 상태에 따른 체크박스 UI 변경
        viewModel.isAllAgreed.observe(viewLifecycleOwner) { isChecked ->
            binding.cbAll.isChecked = isChecked
        }
        viewModel.isTermsAgreed.observe(viewLifecycleOwner) { isChecked ->
            binding.cbTerms.isChecked = isChecked
        }
        viewModel.isPrivacyAgreed.observe(viewLifecycleOwner) { isChecked ->
            binding.cbPrivacy.isChecked = isChecked
        }

        viewModel.isMarketingAgreed.observe(viewLifecycleOwner) { isChecked ->
            binding.cbMarketing.isChecked = isChecked
        }

        // 뷰 클릭시 viewModel에 상태 전달
        binding.cbAll.setOnClickListener {
            val isChecked = binding.cbAll.isChecked
            viewModel.onAllAgreeClicked(isChecked)
        }

        binding.cbTerms.setOnClickListener {
            viewModel.isTermsAgreed.value = binding.cbTerms.isChecked
            viewModel.onSingleAgreeClicked()
        }

        binding.cbPrivacy.setOnClickListener {
            viewModel.isPrivacyAgreed.value = binding.cbPrivacy.isChecked
            viewModel.onSingleAgreeClicked()
        }

        binding.cbMarketing.setOnClickListener {
            viewModel.isMarketingAgreed.value = binding.cbMarketing.isChecked
            viewModel.onSingleAgreeClicked()
        }

        binding.tvTermsDetail.setOnClickListener {
            // 약관 내용 보여주기
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}