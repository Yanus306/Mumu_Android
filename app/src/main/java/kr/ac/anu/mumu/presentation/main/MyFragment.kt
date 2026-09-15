package kr.ac.anu.mumu.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil3.load
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.data.local.SessionManager
import kr.ac.anu.mumu.data.model.PetDto
import kr.ac.anu.mumu.data.model.PetRequestDto
import kr.ac.anu.mumu.databinding.DialogPetFormBinding
import kr.ac.anu.mumu.databinding.FragmentMyBinding
import kr.ac.anu.mumu.databinding.ItemPetProfileBinding
import kr.ac.anu.mumu.presentation.login.LoginActivity
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class MyFragment : Fragment() {

    @Inject lateinit var sessionManager: SessionManager
    private val viewModel: MyViewModel by viewModels()
    private var petDialog: AlertDialog? = null

    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAddPet.setOnClickListener { showPetForm(null) }
        binding.tvPetsState.setOnClickListener {
            if (viewModel.uiState.value is MyUiState.Error) viewModel.loadPets()
        }
        binding.btnLogout.setOnClickListener {
            sessionManager.clearTokens()
            startActivity(
                Intent(requireContext(), LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            MyEvent.Saved -> {
                                petDialog?.dismiss()
                                petDialog = null
                                Toast.makeText(requireContext(), "반려동물을 저장했습니다.", Toast.LENGTH_SHORT).show()
                            }
                            is MyEvent.Message -> {
                                petDialog?.findViewById<AppCompatButton>(R.id.btn_save_pet)?.isEnabled = true
                                Toast.makeText(requireContext(), event.text, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun render(state: MyUiState) {
        binding.progressPets.visibility = if (state is MyUiState.Loading) View.VISIBLE else View.GONE
        binding.btnAddPet.isEnabled = state is MyUiState.Ready && !state.isSaving
        binding.tvPetsState.visibility = if (
            state is MyUiState.Error || state is MyUiState.Ready && state.pets.isEmpty()
        ) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.tvPetsState.text = when (state) {
            is MyUiState.Error -> "${state.message}\n탭해서 다시 시도해 주세요."
            is MyUiState.Ready -> "아직 등록한 반려동물이 없어요."
            MyUiState.Loading -> ""
        }
        binding.layoutPets.removeAllViews()
        if (state is MyUiState.Ready) {
            val selectedPet = state.pets.firstOrNull { it.petId == state.selectedPetId }
            binding.tvMyName.text = selectedPet?.name ?: "반려동물 미등록"
            binding.tvMyActivity.text = "활동내역 ${selectedPet?.recordDays ?: 0}"
            binding.tvMyLikes.text = "좋아요 ${selectedPet?.likeCount ?: 0}"
            if (selectedPet?.profileImageUrl.isNullOrBlank()) {
                binding.ivMyProfile.setImageResource(R.drawable.dog)
            } else {
                binding.ivMyProfile.load(selectedPet.profileImageUrl)
            }
            state.pets.forEach { pet ->
                val row = ItemPetProfileBinding.inflate(layoutInflater, binding.layoutPets, false)
                row.tvPetName.text = pet.name.orEmpty()
                row.tvPetInfo.text = listOfNotNull(
                    pet.species?.toSpeciesLabel(),
                    pet.breed?.takeIf(String::isNotBlank),
                    pet.birthDate?.takeIf(String::isNotBlank)
                ).joinToString(" · ")
                row.btnEditPet.isEnabled = !state.isSaving
                row.btnDeletePet.isEnabled = !state.isSaving
                row.btnSelectPet.text = if (state.selectedPetId == pet.petId) "대표 아이 ✓" else "대표로 선택"
                row.btnSelectPet.isEnabled = !state.isSaving && state.selectedPetId != pet.petId
                row.btnSelectPet.setOnClickListener { viewModel.selectPet(pet.petId) }
                row.btnEditPet.setOnClickListener { showPetForm(pet) }
                row.btnDeletePet.setOnClickListener { confirmDelete(pet) }
                binding.layoutPets.addView(row.root)
            }
        }
    }

    private fun showPetForm(pet: PetDto?) {
        val form = DialogPetFormBinding.inflate(layoutInflater)
        val species = listOf("강아지", "고양이", "기타")
        val genders = listOf("성별 모름", "수컷", "암컷")
        form.spinnerSpecies.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, species)
        form.spinnerGender.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, genders)
        form.tvFormTitle.text = if (pet == null) "반려동물 등록" else "반려동물 수정"
        pet?.let {
            form.etPetName.setText(it.name)
            form.spinnerSpecies.setSelection(listOf("DOG", "CAT", "OTHER").indexOf(it.species).coerceAtLeast(0))
            form.etBreed.setText(it.breed)
            form.spinnerGender.setSelection(listOf("unknown", "male", "female").indexOf(it.gender).coerceAtLeast(0))
            form.etBirthDate.setText(it.birthDate)
            form.etWeight.setText(it.weight?.toString())
            form.switchNeutered.isChecked = it.neutered
            form.etAllergies.setText(it.allergies)
            form.etChronicDiseases.setText(it.chronicDiseases)
            form.etMedications.setText(it.medications)
        }
        form.etBirthDate.setOnClickListener {
            val today = Calendar.getInstance()
            android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, day -> form.etBirthDate.setText("%04d-%02d-%02d".format(year, month + 1, day)) },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dialog = AlertDialog.Builder(requireContext()).setView(form.root).create()
        petDialog = dialog
        form.btnCancel.setOnClickListener { dialog.dismiss() }
        form.btnSavePet.setOnClickListener {
            val name = form.etPetName.text?.toString()?.trim().orEmpty()
            val date = form.etBirthDate.text?.toString()?.takeIf(String::isNotBlank)
            val weightText = form.etWeight.text?.toString()?.trim().orEmpty()
            val weight = weightText.toDoubleOrNull()
            val validationError = when {
                name.isBlank() -> "이름을 입력해 주세요."
                name.length > 50 -> "이름은 50자 이하로 입력해 주세요."
                date != null && !date.isValidDate() -> "생년월일을 확인해 주세요."
                weightText.isNotEmpty() && (weight == null || weight !in 0.0..999.99) ->
                    "몸무게는 0~999.99kg으로 입력해 주세요."
                else -> null
            }
            if (validationError != null) {
                form.tvFormError.text = validationError
                form.tvFormError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            form.btnSavePet.isEnabled = false
            viewModel.savePet(
                pet?.petId,
                PetRequestDto(
                    name = name,
                    species = listOf("DOG", "CAT", "OTHER")[form.spinnerSpecies.selectedItemPosition],
                    breed = form.etBreed.text?.toString()?.trim().orEmpty().ifEmpty { null },
                    gender = listOf("unknown", "male", "female")[form.spinnerGender.selectedItemPosition],
                    birthDate = date,
                    weight = weight,
                    neutered = form.switchNeutered.isChecked,
                    allergies = form.etAllergies.text?.toString()?.trim().orEmpty().ifEmpty { null },
                    chronicDiseases = form.etChronicDiseases.text?.toString()?.trim().orEmpty().ifEmpty { null },
                    medications = form.etMedications.text?.toString()?.trim().orEmpty().ifEmpty { null }
                )
            )
        }
        dialog.setOnDismissListener { if (petDialog == dialog) petDialog = null }
        dialog.show()
    }

    private fun confirmDelete(pet: PetDto) {
        AlertDialog.Builder(requireContext())
            .setTitle("${pet.name} 정보를 삭제할까요?")
            .setMessage("삭제하면 분석에 사용할 수 없어요.")
            .setNegativeButton("취소", null)
            .setPositiveButton("삭제") { _, _ -> viewModel.deletePet(pet.petId) }
            .show()
    }

    override fun onDestroyView() {
        petDialog?.dismiss()
        petDialog = null
        super.onDestroyView()
        _binding = null
    }
}

private fun String.toSpeciesLabel(): String = when (uppercase()) {
    "DOG" -> "강아지"
    "CAT" -> "고양이"
    else -> "기타"
}

private fun String.isValidDate(): Boolean = try {
    !LocalDate.parse(this).isAfter(LocalDate.now())
} catch (_: DateTimeParseException) {
    false
}
