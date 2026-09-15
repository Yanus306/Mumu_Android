package kr.ac.anu.mumu.presentation.analysis

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.FragmentAnalysisResultBinding
import kr.ac.anu.mumu.domain.model.BehaviorAnalysisResult

@AndroidEntryPoint
class AnalysisResultFragment : Fragment() {

    private var _binding: FragmentAnalysisResultBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalysisViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val state = viewModel.uiState.value
        if (state !is AnalysisUiState.Success) {
            findNavController().popBackStack(R.id.homeFragment, false)
            return
        }
        bindResult(state.result)

        binding.btnHome.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
        binding.videoResult.setOnClickListener {
            if (binding.videoResult.isPlaying) binding.videoResult.pause() else binding.videoResult.start()
        }
    }

    private fun bindResult(result: BehaviorAnalysisResult) {
        val status = if (result.isNormal) "정상" else "비정상"
        val statusColor = if (result.isNormal) R.color.cardgreen else R.color.cardred

        binding.tvResultStatus.text = "분석결과: $status"
        binding.tvResultStatus.setTextColor(ContextCompat.getColor(requireContext(), statusColor))
        binding.tvProbability.text = "${result.probability}% 확률"
        binding.tvSuspectedBehavior.text = result.suspectedBehavior
        binding.tvReason.text = "이유 : ${result.reason}\n\n추천 방향성 : ${result.recommendation}"
        binding.videoResult.setVideoURI(Uri.parse(result.videoUri))
        binding.videoResult.setOnPreparedListener { player ->
            binding.ivResultThumbnail.visibility = View.GONE
            player.isLooping = true
            binding.videoResult.start()
        }
        binding.videoResult.setOnErrorListener { _, _, _ ->
            binding.ivResultThumbnail.visibility = View.VISIBLE
            true
        }
        loadVideoThumbnail(Uri.parse(result.videoUri))
    }

    private fun loadVideoThumbnail(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = runCatching {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(requireContext(), uri)
                    retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                } finally {
                    retriever.release()
                }
            }.getOrNull()
            if (bitmap != null && isAdded) {
                binding.ivResultThumbnail.post {
                    binding.ivResultThumbnail.setImageBitmap(bitmap)
                    binding.ivResultThumbnail.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onPause() {
        binding.videoResult.pause()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
