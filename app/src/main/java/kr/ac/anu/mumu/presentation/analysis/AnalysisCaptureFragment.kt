package kr.ac.anu.mumu.presentation.analysis

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.FragmentAnalysisCaptureBinding
import java.io.File

class AnalysisCaptureFragment : Fragment() {

    private var _binding: FragmentAnalysisCaptureBinding? = null
    private val binding get() = _binding!!
    private var selectedVideoUri: Uri? = null
    private var pendingCaptureUri: Uri? = null

    private val captureVideo = registerForActivityResult(ActivityResultContracts.CaptureVideo()) { saved ->
        if (saved) {
            pendingCaptureUri?.let(::showVideo)
        } else {
            Toast.makeText(requireContext(), "영상 촬영이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }
        pendingCaptureUri = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisCaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.getString(STATE_SELECTED_VIDEO)?.let { showVideo(Uri.parse(it)) }

        binding.btnRecord.setOnClickListener {
            val uri = createVideoUri()
            pendingCaptureUri = uri
            captureVideo.launch(uri)
        }
        binding.btnRetake.setOnClickListener {
            val uri = createVideoUri()
            pendingCaptureUri = uri
            captureVideo.launch(uri)
        }
        binding.btnAnalyzeVideo.setOnClickListener {
            selectedVideoUri?.let { uri ->
                findNavController().navigate(
                    R.id.action_analysisCaptureFragment_to_analysisLoadingFragment,
                    bundleOf(ARG_VIDEO_URI to uri.toString())
                )
            }
        }
        binding.videoPreview.setOnClickListener {
            if (binding.videoPreview.isPlaying) binding.videoPreview.pause() else binding.videoPreview.start()
        }
    }

    private fun createVideoUri(): Uri {
        val directory = File(requireContext().cacheDir, "analysis-videos").apply { mkdirs() }
        val file = File.createTempFile("mumu-analysis-", ".mp4", directory)
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
    }

    private fun showVideo(uri: Uri) {
        selectedVideoUri = uri
        binding.tvCaptureGuide.visibility = View.GONE
        binding.videoPreview.visibility = View.VISIBLE
        binding.videoPreview.setVideoURI(uri)
        binding.videoPreview.setOnPreparedListener { player ->
            player.isLooping = true
            binding.videoPreview.start()
        }
        binding.btnRecord.visibility = View.INVISIBLE
        binding.btnRetake.visibility = View.VISIBLE
        binding.btnAnalyzeVideo.visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        selectedVideoUri?.let { outState.putString(STATE_SELECTED_VIDEO, it.toString()) }
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        binding.videoPreview.pause()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val STATE_SELECTED_VIDEO = "selected_video"
    }
}
