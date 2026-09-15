package kr.ac.anu.mumu.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil3.load
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.data.model.DiaryDetailDto
import kr.ac.anu.mumu.databinding.DialogDiaryDetailBinding

@AndroidEntryPoint
class DiaryDetailPageFragment : Fragment() {
    private var _binding: DialogDiaryDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DiaryViewModel by viewModels()
    private var diary: DiaryDetailDto? = null
    private var requested = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = DialogDiaryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        binding.btnClose.text = "확인"
        binding.btnClose.setOnClickListener { findNavController().navigateUp() }
        binding.btnEdit.setOnClickListener {
            val value = diary ?: return@setOnClickListener
            findNavController().navigate(
                R.id.diaryEditorFragment,
                Bundle().apply { putLong("diaryId", value.diaryId) }
            )
        }
        binding.btnDelete.setOnClickListener { confirmDelete() }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        if (it is DiaryUiState.Ready && !requested) {
                            requested = true
                            viewModel.openDiary(requireArguments().getLong("diaryId"))
                        }
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is DiaryEvent.Open -> render(event.diary)
                            is DiaryEvent.Message -> {
                                Toast.makeText(requireContext(), event.text, Toast.LENGTH_SHORT).show()
                                if (event.text.contains("삭제")) findNavController().navigateUp()
                            }
                            DiaryEvent.Saved -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun render(value: DiaryDetailDto) {
        diary = value
        binding.tvDetailDate.text = value.diaryDate.replace('-', '.')
        binding.tvDetailTitle.text = value.title
        binding.tvDetailContent.text = value.content
        binding.tvDetailMood.text = if (value.mood.equals("happy", true)) "☺" else "☹"
        value.imageUrls?.firstOrNull()?.let {
            binding.ivDetailImage.visibility = View.VISIBLE
            binding.ivDetailImage.load(it)
        }
        value.analysisSummary?.let {
            binding.tvDetailAnalysis.visibility = View.VISIBLE
            binding.tvDetailAnalysis.text = "분석 결과\n${it.resultLabel.orEmpty()}"
        }
    }

    private fun confirmDelete() {
        val value = diary ?: return
        AlertDialog.Builder(requireContext())
            .setMessage("일기를 삭제할까요?")
            .setNegativeButton("취소", null)
            .setPositiveButton("삭제") { _, _ -> viewModel.deleteDiary(value.diaryId) }
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (requested && viewModel.uiState.value is DiaryUiState.Ready) {
            viewModel.openDiary(requireArguments().getLong("diaryId"))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
