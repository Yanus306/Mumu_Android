package kr.ac.anu.mumu.presentation.main

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import coil3.load
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.data.model.DiaryDetailDto
import kr.ac.anu.mumu.data.model.DiaryRequestDto
import kr.ac.anu.mumu.databinding.DialogDiaryDetailBinding
import kr.ac.anu.mumu.databinding.DialogDiaryFormBinding
import kr.ac.anu.mumu.databinding.FragmentDiaryBinding
import kr.ac.anu.mumu.presentation.main.adapter.DiaryAdapter
import kr.ac.anu.mumu.presentation.main.adapter.toMoodLabel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException

@AndroidEntryPoint
class DiaryFragment : Fragment() {
    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DiaryViewModel by viewModels()
    private val adapter = DiaryAdapter { viewModel.openDiary(it.diaryId) }
    private var formDialog: AlertDialog? = null
    private var detailDialog: AlertDialog? = null
    private var hasResumed = false
    private var autoCompose = false
    private var selectedImageUri: Uri? = null
    private var activeForm: DialogDiaryFormBinding? = null
    private val pickDiaryImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            activeForm?.ivDiaryPreview?.apply {
                visibility = View.VISIBLE
                load(uri)
            }
            activeForm?.btnRemoveDiaryImage?.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDiaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        autoCompose = savedInstanceState == null && arguments?.getBoolean("compose") == true
        binding.rvDiaries.adapter = adapter
        binding.rvDiaries.layoutManager = LinearLayoutManager(requireContext())
        binding.btnWriteDiary.setOnClickListener {
            val selected = (viewModel.uiState.value as? DiaryUiState.Ready)?.selectedDate
            showForm(null, selected)
        }
        binding.btnMoreDiaries.setOnClickListener { viewModel.loadMore() }
        binding.btnPreviousMonth.setOnClickListener { viewModel.changeMonth(-1) }
        binding.btnNextMonth.setOnClickListener { viewModel.changeMonth(1) }
        binding.tvCalendarStatus.setOnClickListener { viewModel.retryCalendar() }
        binding.tvDiaryState.setOnClickListener {
            if (viewModel.uiState.value is DiaryUiState.Error) viewModel.load()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is DiaryEvent.Open -> showDetail(event.diary)
                            DiaryEvent.Saved -> {
                                formDialog?.dismiss()
                                formDialog = null
                                detailDialog?.dismiss()
                                detailDialog = null
                                Toast.makeText(requireContext(), "일기를 저장했습니다.", Toast.LENGTH_SHORT).show()
                            }
                            is DiaryEvent.Message -> {
                                formDialog?.findViewById<AppCompatButton>(R.id.btn_save_diary)?.apply {
                                    isEnabled = true
                                    text = "저장"
                                }
                                Toast.makeText(requireContext(), event.text, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasResumed) viewModel.load() else hasResumed = true
    }

    private fun render(state: DiaryUiState) {
        binding.progressDiaries.visibility = if (state is DiaryUiState.Loading) View.VISIBLE else View.GONE
        binding.btnWriteDiary.isEnabled = state is DiaryUiState.Ready && state.petId != null && !state.isWorking
        binding.tvDiaryState.visibility = if (
            state is DiaryUiState.Error || state is DiaryUiState.Ready && state.entries.isEmpty()
        ) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.tvDiaryState.text = when (state) {
            is DiaryUiState.Error -> "${state.message}\n탭해서 다시 시도해 주세요."
            is DiaryUiState.Ready -> if (state.petId == null) {
                "마이 탭에서 반려동물을 먼저 등록해 주세요."
            } else {
                "아직 작성한 일기가 없어요."
            }
            DiaryUiState.Loading -> ""
        }
        if (state is DiaryUiState.Ready) {
            binding.layoutCalendar.visibility = if (state.petId == null) View.GONE else View.VISIBLE
            binding.tvDiaryPet.text = state.petName?.let { "${it}의 하루를 기록해 주세요." }
                ?: "우리 아이의 하루를 기록해 주세요."
            binding.btnWriteDiary.text = state.selectedDate?.let {
                "${it.toString().replace('-', '.')} 일기 쓰기"
            } ?: "+ 일기 쓰기"
            renderCalendar(state)
            adapter.submitList(state.entries)
            binding.btnMoreDiaries.visibility = if (state.nextPage == null) View.GONE else View.VISIBLE
            binding.btnMoreDiaries.isEnabled = !state.isLoadingMore
            binding.btnMoreDiaries.text = if (state.isLoadingMore) "불러오는 중…" else "더 보기"
            if (autoCompose && state.petId != null) {
                autoCompose = false
                arguments?.putBoolean("compose", false)
                showForm(null)
            }
        } else {
            binding.layoutCalendar.visibility = View.GONE
            adapter.submitList(emptyList())
            binding.btnMoreDiaries.visibility = View.GONE
        }
    }

    private fun renderCalendar(state: DiaryUiState.Ready) {
        binding.tvCalendarMonth.text = "${state.calendarMonth.year}년 ${state.calendarMonth.monthValue}월"
        binding.btnNextMonth.isEnabled = state.calendarMonth.isBefore(YearMonth.now())
        binding.tvCalendarStatus.visibility = if (state.isCalendarLoading || state.calendarError != null || state.writtenDates.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.tvCalendarStatus.text = when {
            state.isCalendarLoading -> "기록한 날짜를 불러오는 중…"
            state.calendarError != null -> "${state.calendarError} · 탭해서 다시 시도"
            state.writtenDates.isEmpty() -> "이 달에는 아직 기록한 날짜가 없어요."
            else -> ""
        }
        binding.tvCalendarStatus.isClickable = state.calendarError != null

        val density = resources.displayMetrics.density
        val cellHeight = (46 * density).toInt()
        binding.layoutCalendarDays.removeAllViews()
        val header = LinearLayout(requireContext())
        listOf("월", "화", "수", "목", "금", "토", "일").forEach { label ->
            val headerLabel = TextView(requireContext()).apply {
                text = label
                textSize = 11f
                gravity = android.view.Gravity.CENTER
                setTextColor(ContextCompat.getColor(requireContext(), R.color.mumugray_350))
            }
            header.addView(headerLabel, LinearLayout.LayoutParams(0, (25 * density).toInt(), 1f))
        }
        binding.layoutCalendarDays.addView(header)

        state.calendarMonth.toCalendarCells().chunked(7).forEach { week ->
            val row = LinearLayout(requireContext())
            week.forEach { date ->
                val cell = AppCompatButton(requireContext()).apply {
                    minWidth = 0
                    minHeight = 0
                    isAllCaps = false
                    textSize = 12f
                    setPadding(0, 0, 0, 0)
                    if (date == null) {
                        visibility = View.INVISIBLE
                    } else {
                        val written = date in state.writtenDates
                        text = if (written) "${date.dayOfMonth}\n•" else date.dayOfMonth.toString()
                        contentDescription = "${date.monthValue}월 ${date.dayOfMonth}일" +
                            if (written) ", 일기 있음" else ""
                        isEnabled = !date.isAfter(LocalDate.now())
                        val textColor = if (date == state.selectedDate) R.color.white else R.color.mumugray_400
                        setTextColor(ContextCompat.getColor(requireContext(), textColor))
                        when {
                            date == state.selectedDate -> setBackgroundResource(R.drawable.bg_analysis_primary)
                            written -> setBackgroundResource(R.drawable.bg_community_chip)
                            else -> setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        }
                        backgroundTintList = null
                        setOnClickListener { viewModel.selectDate(date) }
                    }
                }
                row.addView(cell, LinearLayout.LayoutParams(0, cellHeight, 1f))
            }
            binding.layoutCalendarDays.addView(row)
        }
    }

    private fun showForm(diary: DiaryDetailDto?, selectedDate: LocalDate? = null) {
        val state = viewModel.uiState.value as? DiaryUiState.Ready ?: return
        val petId = state.petId ?: return
        val form = DialogDiaryFormBinding.inflate(layoutInflater)
        selectedImageUri = null
        activeForm = form
        form.btnPickDiaryImage.visibility = if (diary == null) View.VISIBLE else View.GONE
        form.btnPickDiaryImage.setOnClickListener { pickDiaryImage.launch("image/*") }
        form.btnRemoveDiaryImage.setOnClickListener {
            selectedImageUri = null
            form.ivDiaryPreview.setImageDrawable(null)
            form.ivDiaryPreview.visibility = View.GONE
            form.btnRemoveDiaryImage.visibility = View.GONE
        }
        diary?.imageUrls?.firstOrNull()?.let {
            form.ivDiaryPreview.visibility = View.VISIBLE
            form.ivDiaryPreview.load(it)
        }
        form.tvFormTitle.text = if (diary == null) "일기 쓰기" else "일기 수정"
        form.spinnerMood.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf("행복", "보통", "속상")
        )
        form.etDiaryDate.setText(diary?.diaryDate ?: selectedDate?.toString() ?: LocalDate.now().toString())
        form.spinnerMood.setSelection(listOf("happy", "normal", "sad").indexOf(diary?.mood).coerceAtLeast(1))
        form.etDiaryTitle.setText(diary?.title)
        form.etDiaryContent.setText(diary?.content)
        form.etDiaryDate.setOnClickListener {
            val initial = runCatching { LocalDate.parse(form.etDiaryDate.text.toString()) }.getOrDefault(LocalDate.now())
            DatePickerDialog(
                requireContext(),
                { _, year, month, day -> form.etDiaryDate.setText("%04d-%02d-%02d".format(year, month + 1, day)) },
                initial.year,
                initial.monthValue - 1,
                initial.dayOfMonth
            ).show()
        }
        val dialog = AlertDialog.Builder(requireContext()).setView(form.root).create()
        formDialog = dialog
        form.btnCancel.setOnClickListener { dialog.dismiss() }
        form.btnSaveDiary.setOnClickListener {
            val title = form.etDiaryTitle.text?.toString()?.trim().orEmpty()
            val content = form.etDiaryContent.text?.toString()?.trim().orEmpty()
            val date = form.etDiaryDate.text?.toString().orEmpty()
            val error = when {
                title.isBlank() -> "제목을 입력해 주세요."
                title.length > 200 -> "제목은 200자 이하로 입력해 주세요."
                content.isBlank() -> "내용을 입력해 주세요."
                !date.isValidDiaryDate() -> "날짜를 확인해 주세요."
                else -> null
            }
            if (error != null) {
                form.tvFormError.text = error
                form.tvFormError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            form.btnSaveDiary.isEnabled = false
            form.btnSaveDiary.text = if (selectedImageUri == null) "저장 중…" else "사진 업로드 중…"
            val linkedAnalysis = diary?.analysisSummary
            viewModel.saveDiary(
                diary?.diaryId,
                DiaryRequestDto(
                    petId = petId,
                    mood = listOf("happy", "normal", "sad")[form.spinnerMood.selectedItemPosition],
                    title = title,
                    content = content,
                    diaryDate = date,
                    behaviorAnalysisId = linkedAnalysis?.analysisId
                        ?.takeIf { linkedAnalysis.type.equals("behavior", ignoreCase = true) },
                    soundAnalysisId = linkedAnalysis?.analysisId
                        ?.takeIf { linkedAnalysis.type.equals("sound", ignoreCase = true) },
                    foodSafetyAnalysisId = linkedAnalysis?.analysisId
                        ?.takeIf { linkedAnalysis.type.equals("food_safety", ignoreCase = true) }
                ),
                selectedImageUri
            )
        }
        dialog.setOnDismissListener {
            if (formDialog == dialog) {
                formDialog = null
                activeForm = null
                selectedImageUri = null
            }
        }
        dialog.show()
    }

    private fun showDetail(diary: DiaryDetailDto) {
        val detail = DialogDiaryDetailBinding.inflate(layoutInflater)
        detail.tvDetailDate.text = "${diary.diaryDate.replace('-', '.')} · ${diary.mood.toMoodLabel()}"
        detail.tvDetailTitle.text = diary.title
        detail.tvDetailContent.text = diary.content
        diary.analysisSummary?.let {
            detail.tvDetailAnalysis.visibility = View.VISIBLE
            detail.tvDetailAnalysis.text = "연결된 분석 · ${it.resultLabel.orEmpty()}"
        }
        diary.imageUrls?.firstOrNull()?.let {
            detail.ivDetailImage.visibility = View.VISIBLE
            detail.ivDetailImage.load(it)
        }
        val dialog = AlertDialog.Builder(requireContext()).setView(detail.root).create()
        detailDialog = dialog
        detail.btnClose.setOnClickListener { dialog.dismiss() }
        detail.btnEdit.setOnClickListener {
            dialog.dismiss()
            showForm(diary)
        }
        detail.btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("일기를 삭제할까요?")
                .setMessage("삭제한 일기와 사진은 복구할 수 없어요.")
                .setNegativeButton("취소", null)
                .setPositiveButton("삭제") { _, _ ->
                    dialog.dismiss()
                    viewModel.deleteDiary(diary.diaryId)
                }
                .show()
        }
        dialog.setOnDismissListener { if (detailDialog == dialog) detailDialog = null }
        dialog.show()
    }

    override fun onDestroyView() {
        formDialog?.dismiss()
        detailDialog?.dismiss()
        formDialog = null
        detailDialog = null
        super.onDestroyView()
        _binding = null
    }
}

private fun String.isValidDiaryDate(): Boolean = try {
    !LocalDate.parse(this).isAfter(LocalDate.now())
} catch (_: DateTimeParseException) {
    false
}
