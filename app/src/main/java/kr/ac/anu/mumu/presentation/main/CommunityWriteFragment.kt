package kr.ac.anu.mumu.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.FragmentCommunityWriteBinding

@AndroidEntryPoint
class CommunityWriteFragment : Fragment() {

    private var _binding: FragmentCommunityWriteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CommunityWriteViewModel by viewModels()
    private val categories = listOf("FREE", "QUESTION", "INFO", "BRAG", "REVIEW")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf("자유", "질문", "정보", "자랑", "후기")
        )
        binding.btnSubmit.setOnClickListener {
            viewModel.submit(
                category = categories[binding.spinnerCategory.selectedItemPosition],
                title = binding.etTitle.text?.toString().orEmpty(),
                content = binding.etContent.text?.toString().orEmpty(),
                hashtags = binding.etHashtags.text?.toString().orEmpty()
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: CommunityWriteUiState) {
        binding.progressSubmit.visibility = if (state is CommunityWriteUiState.Loading) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = state !is CommunityWriteUiState.Loading
        binding.tvError.visibility = if (state is CommunityWriteUiState.Error) View.VISIBLE else View.GONE

        when (state) {
            CommunityWriteUiState.Idle,
            CommunityWriteUiState.Loading -> Unit
            is CommunityWriteUiState.Error -> binding.tvError.text = state.message
            is CommunityWriteUiState.Success -> {
                findNavController().navigate(
                    R.id.communityDetailFragment,
                    Bundle().apply { putLong("postId", state.postId) }
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
