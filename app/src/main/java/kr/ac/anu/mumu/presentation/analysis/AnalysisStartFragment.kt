package kr.ac.anu.mumu.presentation.analysis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.FragmentAnalysisStartBinding

class AnalysisStartFragment : Fragment() {

    private var _binding: FragmentAnalysisStartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCapture.setOnClickListener {
            findNavController().navigate(R.id.action_analysisStartFragment_to_analysisCaptureFragment)
        }
        binding.btnUpload.setOnClickListener {
            findNavController().navigate(R.id.action_analysisStartFragment_to_analysisUploadFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
