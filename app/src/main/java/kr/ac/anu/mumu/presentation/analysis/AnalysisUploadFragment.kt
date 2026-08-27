package kr.ac.anu.mumu.presentation.analysis

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.FragmentAnalysisUploadBinding

class AnalysisUploadFragment : Fragment() {

    private var _binding: FragmentAnalysisUploadBinding? = null
    private val binding get() = _binding!!
    private var selectedVideoUri: Uri? = null

    private val selectVideo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(::showVideo)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val restoredUri = savedInstanceState?.getString(STATE_SELECTED_VIDEO)?.let(Uri::parse)
        if (restoredUri != null) showVideo(restoredUri)

        binding.btnSelectVideo.setOnClickListener { selectVideo.launch("video/*") }
        binding.btnAnalyzeVideo.setOnClickListener {
            selectedVideoUri?.let { uri ->
                findNavController().navigate(
                    R.id.action_analysisUploadFragment_to_analysisLoadingFragment,
                    bundleOf(ARG_VIDEO_URI to uri.toString())
                )
            }
        }

        if (savedInstanceState == null) selectVideo.launch("video/*")
    }

    private fun showVideo(uri: Uri) {
        selectedVideoUri = uri
        binding.videoPreview.visibility = View.VISIBLE
        binding.videoPreview.setVideoURI(uri)
        binding.videoPreview.setOnPreparedListener { player ->
            player.isLooping = true
            binding.videoPreview.start()
        }
        binding.tvSelectedFile.text = getDisplayName(uri) ?: "선택한 영상"
        binding.btnSelectVideo.text = "다른 영상 선택"
        binding.btnAnalyzeVideo.visibility = View.VISIBLE
    }

    private fun getDisplayName(uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            cursor = requireContext().contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )
            if (cursor?.moveToFirst() == true) cursor.getString(0) else null
        } finally {
            cursor?.close()
        }
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
