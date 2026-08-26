package kr.ac.anu.mumu.presentation.analysis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.FragmentAnalysisLoadingBinding

@AndroidEntryPoint
class AnalysisLoadingFragment : Fragment() {

    private var _binding: FragmentAnalysisLoadingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalysisViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val videoUri = requireArguments().getString(ARG_VIDEO_URI).orEmpty()
        require(videoUri.isNotBlank()) { "분석할 영상 URI가 필요합니다." }
        viewModel.analyze(videoUri)

        binding.btnWriteDiary.setOnClickListener {
            findNavController().navigate(R.id.diaryFragment)
        }
        binding.btnLater.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
        binding.btnRetry.setOnClickListener {
            binding.btnRetry.visibility = View.GONE
            binding.progressAnalysis.visibility = View.VISIBLE
            binding.btnWriteDiary.visibility = View.VISIBLE
            binding.btnLater.visibility = View.VISIBLE
            binding.tvLoadingMessage.text = "mumu가 열심히 분석하고 있어요!..."
            viewModel.analyze(videoUri)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AnalysisUiState.Loading -> {
                            binding.progressAnalysis.visibility = View.VISIBLE
                            binding.btnRetry.visibility = View.GONE
                            binding.btnWriteDiary.visibility = View.VISIBLE
                            binding.btnLater.visibility = View.VISIBLE
                        }
                        is AnalysisUiState.Success -> {
                            if (findNavController().currentDestination?.id == R.id.analysisLoadingFragment) {
                                findNavController().navigate(
                                    R.id.action_analysisLoadingFragment_to_analysisResultFragment
                                )
                            }
                        }
                        is AnalysisUiState.Error -> {
                            binding.progressAnalysis.visibility = View.GONE
                            binding.tvLoadingMessage.text = state.message
                            binding.btnRetry.visibility = View.VISIBLE
                            binding.btnWriteDiary.visibility = View.GONE
                            binding.btnLater.visibility = View.GONE
                        }
                        AnalysisUiState.Idle -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
