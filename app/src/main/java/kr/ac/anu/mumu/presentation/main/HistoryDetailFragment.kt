package kr.ac.anu.mumu.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.FragmentHistoryDetailBinding
import kr.ac.anu.mumu.domain.model.AnalysisHistory

@AndroidEntryPoint
class HistoryDetailFragment : Fragment() {

    private var _binding: FragmentHistoryDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRetry.setOnClickListener { viewModel.loadDetail() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: HistoryDetailUiState) {
        binding.progressDetail.visibility = if (state is HistoryDetailUiState.Loading) View.VISIBLE else View.GONE
        binding.layoutContent.visibility = if (state is HistoryDetailUiState.Success) View.VISIBLE else View.GONE
        binding.layoutError.visibility = if (state is HistoryDetailUiState.Error) View.VISIBLE else View.GONE

        when (state) {
            HistoryDetailUiState.Loading -> Unit
            is HistoryDetailUiState.Success -> bindItem(state.item)
            is HistoryDetailUiState.Error -> binding.tvError.text = state.message
        }
    }

    private fun bindItem(item: AnalysisHistory) {
        val status = if (item.isNormal) "정상" else "비정상"
        val color = if (item.isNormal) R.color.cardgreen else R.color.cardred
        binding.tvResultStatus.text = "분석결과: $status"
        binding.tvResultStatus.setTextColor(ContextCompat.getColor(requireContext(), color))
        binding.tvProbability.text = "${item.probability}% 확률"
        binding.tvDate.text = item.date
        binding.tvSuspectedBehavior.text = item.behaviorText
        binding.tvReason.text = if (item.isNormal) {
            "분석 영상에서 뚜렷한 이상 행동이 감지되지 않았습니다."
        } else {
            "AI 분석에서 ${item.behaviorText} 패턴이 감지되었습니다."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
