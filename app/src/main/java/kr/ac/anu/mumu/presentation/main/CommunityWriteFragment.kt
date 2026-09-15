package kr.ac.anu.mumu.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
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
    private var hasPopulatedForm = false

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
        hasPopulatedForm = savedInstanceState?.getBoolean(KEY_FORM_POPULATED) ?: false

        binding.spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf("자유", "질문", "정보", "자랑", "후기")
        )
        binding.tvWriteTitle.text = if (viewModel.isEditMode) "이야기를 수정할까요?" else "어떤 이야기를 나눌까요?"
        binding.tvWriteDescription.text = if (viewModel.isEditMode) {
            "바꾸고 싶은 내용을 다듬은 뒤 저장해 주세요."
        } else {
            "반려생활의 질문과 순간을 자유롭게 남겨보세요."
        }
        binding.btnSubmit.text = if (viewModel.isEditMode) "수정 내용 저장하기" else "게시글 등록하기"
        binding.etTitle.doAfterTextChanged { text ->
            binding.tvTitleCount.text = "${text?.length ?: 0} / ${CommunityInputValidator.MAX_TITLE_LENGTH}"
        }
        binding.btnSubmit.setOnClickListener {
            if (viewModel.needsEditPostLoad) {
                viewModel.loadPost()
                return@setOnClickListener
            }
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
        val isBusy = state is CommunityWriteUiState.Loading || state is CommunityWriteUiState.Submitting
        binding.progressSubmit.visibility = if (isBusy) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = !isBusy
        binding.btnSubmit.text = if (viewModel.needsEditPostLoad) {
            "게시글 다시 불러오기"
        } else if (viewModel.isEditMode) {
            "수정 내용 저장하기"
        } else {
            "게시글 등록하기"
        }
        binding.tvError.visibility = if (state is CommunityWriteUiState.Error) View.VISIBLE else View.GONE

        when (state) {
            CommunityWriteUiState.Loading,
            CommunityWriteUiState.Submitting -> Unit
            is CommunityWriteUiState.Ready -> {
                val post = state.post ?: return
                if (!hasPopulatedForm) {
                    binding.spinnerCategory.setSelection(
                        categories.indexOf(post.category.uppercase()).coerceAtLeast(0)
                    )
                    binding.etTitle.setText(post.title)
                    binding.etContent.setText(post.content)
                    binding.etHashtags.setText(post.hashtags.joinToString(" ") { "#$it" })
                    hasPopulatedForm = true
                }
            }
            is CommunityWriteUiState.Error -> binding.tvError.text = state.message
            is CommunityWriteUiState.Success -> {
                if (state.isEdit) {
                    findNavController().popBackStack()
                } else {
                    findNavController().navigate(
                        R.id.action_communityWriteFragment_to_communityDetailFragment,
                        Bundle().apply { putLong("postId", state.postId) }
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_FORM_POPULATED, hasPopulatedForm)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val KEY_FORM_POPULATED = "form_populated"
    }
}
