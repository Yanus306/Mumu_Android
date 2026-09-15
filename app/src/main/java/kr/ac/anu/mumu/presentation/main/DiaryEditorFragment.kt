package kr.ac.anu.mumu.presentation.main

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil3.load
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.data.model.DiaryDetailDto
import kr.ac.anu.mumu.data.model.DiaryRequestDto
import kr.ac.anu.mumu.databinding.DialogDiaryFormBinding
import java.time.LocalDate

@AndroidEntryPoint
class DiaryEditorFragment : Fragment() {
    private var _binding: DialogDiaryFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DiaryViewModel by viewModels()
    private var detail: DiaryDetailDto? = null
    private var imageUri: Uri? = null
    private var requestedDetail = false
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        imageUri = uri
        binding.ivDiaryPreview.visibility = View.VISIBLE
        binding.ivDiaryPreview.load(uri)
        binding.btnRemoveDiaryImage.visibility = View.VISIBLE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = DialogDiaryFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        binding.tvFormTitle.visibility = View.GONE
        binding.btnCancel.visibility = View.GONE
        binding.spinnerMood.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listOf("속상", "보통", "행복"))
        binding.etDiaryDate.setText(requireArguments().getString("diaryDate").orEmpty().ifBlank { LocalDate.now().toString() })
        binding.etDiaryDate.setOnClickListener { showDatePicker() }
        binding.btnPickDiaryImage.setOnClickListener { pickImage.launch("image/*") }
        binding.btnRemoveDiaryImage.setOnClickListener {
            imageUri = null
            binding.ivDiaryPreview.visibility = View.GONE
            binding.btnRemoveDiaryImage.visibility = View.GONE
        }
        binding.btnCancel.setOnClickListener { findNavController().navigateUp() }
        binding.btnSaveDiary.setOnClickListener { save() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { maybeLoadDetail(it) } }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is DiaryEvent.Open -> bindDetail(event.diary)
                            DiaryEvent.Saved -> findNavController().navigateUp()
                            is DiaryEvent.Message -> {
                                binding.btnSaveDiary.isEnabled = true
                                Toast.makeText(requireContext(), event.text, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun maybeLoadDetail(state: DiaryUiState) {
        val diaryId = requireArguments().getLong("diaryId", -1L)
        if (state is DiaryUiState.Ready && diaryId > 0 && !requestedDetail) {
            requestedDetail = true
            viewModel.openDiary(diaryId)
        }
    }

    private fun bindDetail(value: DiaryDetailDto) {
        detail = value
        binding.etDiaryDate.setText(value.diaryDate)
        binding.spinnerMood.setSelection(listOf("sad", "normal", "happy").indexOf(value.mood).coerceAtLeast(1))
        binding.etDiaryTitle.setText(value.title)
        binding.etDiaryContent.setText(value.content)
        value.imageUrls?.firstOrNull()?.let {
            binding.ivDiaryPreview.visibility = View.VISIBLE
            binding.ivDiaryPreview.load(it)
        }
        binding.btnSaveDiary.text = "수정하기"
    }

    private fun showDatePicker() {
        val date = runCatching { LocalDate.parse(binding.etDiaryDate.text.toString()) }.getOrDefault(LocalDate.now())
        DatePickerDialog(requireContext(), { _, y, m, d -> binding.etDiaryDate.setText("%04d-%02d-%02d".format(y, m + 1, d)) }, date.year, date.monthValue - 1, date.dayOfMonth).show()
    }

    private fun save() {
        val state = viewModel.uiState.value as? DiaryUiState.Ready ?: return
        val petId = state.petId ?: return
        val title = binding.etDiaryTitle.text?.toString()?.trim().orEmpty()
        val content = binding.etDiaryContent.text?.toString()?.trim().orEmpty()
        val date = binding.etDiaryDate.text?.toString().orEmpty()
        val error = when {
            title.isBlank() -> "제목을 입력해 주세요."
            content.isBlank() -> "내용을 입력해 주세요."
            runCatching { LocalDate.parse(date) }.getOrNull()?.isAfter(LocalDate.now()) != false -> "날짜를 확인해 주세요."
            else -> null
        }
        if (error != null) {
            binding.tvFormError.text = error
            binding.tvFormError.visibility = View.VISIBLE
            return
        }
        binding.btnSaveDiary.isEnabled = false
        val linked = detail?.analysisSummary
        viewModel.saveDiary(
            detail?.diaryId,
            DiaryRequestDto(
                petId = petId,
                mood = listOf("sad", "normal", "happy")[binding.spinnerMood.selectedItemPosition],
                title = title,
                content = content,
                diaryDate = date,
                behaviorAnalysisId = linked?.analysisId?.takeIf { linked.type.equals("behavior", true) },
                soundAnalysisId = linked?.analysisId?.takeIf { linked.type.equals("sound", true) },
                foodSafetyAnalysisId = linked?.analysisId?.takeIf { linked.type.equals("food_safety", true) }
            ),
            imageUri
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
