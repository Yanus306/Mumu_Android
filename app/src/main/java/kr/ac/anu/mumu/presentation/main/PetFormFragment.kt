package kr.ac.anu.mumu.presentation.main

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.data.model.PetDto
import kr.ac.anu.mumu.data.model.PetRequestDto
import kr.ac.anu.mumu.databinding.DialogPetFormBinding
import java.time.LocalDate
import java.util.Calendar

@AndroidEntryPoint
class PetFormFragment : Fragment() {
    private var _binding: DialogPetFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyViewModel by viewModels()
    private var pet: PetDto? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = DialogPetFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        binding.tvFormTitle.text = if (petId() > 0) "동물 프로필 수정" else "동물 프로필 등록"
        binding.spinnerSpecies.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listOf("강아지", "고양이", "기타"))
        binding.spinnerGender.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listOf("성별 모름", "수컷", "암컷"))
        binding.etBirthDate.setOnClickListener { showDatePicker() }
        binding.btnCancel.setOnClickListener { findNavController().navigateUp() }
        binding.btnSavePet.setOnClickListener { save() }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { ui ->
                        if (ui is MyUiState.Ready && pet == null && petId() > 0) {
                            ui.pets.firstOrNull { it.petId == petId() }?.let(::bind)
                        }
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            MyEvent.Saved -> findNavController().navigateUp()
                            is MyEvent.Message -> {
                                binding.btnSavePet.isEnabled = true
                                Toast.makeText(requireContext(), event.text, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun petId() = requireArguments().getLong("petId", -1L)

    private fun bind(value: PetDto) {
        pet = value
        binding.etPetName.setText(value.name)
        binding.spinnerSpecies.setSelection(listOf("DOG", "CAT", "OTHER").indexOf(value.species).coerceAtLeast(0))
        binding.etBreed.setText(value.breed)
        binding.spinnerGender.setSelection(listOf("unknown", "male", "female").indexOf(value.gender).coerceAtLeast(0))
        binding.etBirthDate.setText(value.birthDate)
        binding.etWeight.setText(value.weight?.toString())
        binding.switchNeutered.isChecked = value.neutered
        binding.etAllergies.setText(value.allergies)
        binding.etChronicDiseases.setText(value.chronicDiseases)
        binding.etMedications.setText(value.medications)
    }

    private fun showDatePicker() {
        val now = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d -> binding.etBirthDate.setText("%04d-%02d-%02d".format(y, m + 1, d)) }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun save() {
        val name = binding.etPetName.text?.toString()?.trim().orEmpty()
        val date = binding.etBirthDate.text?.toString()?.takeIf(String::isNotBlank)
        val weightText = binding.etWeight.text?.toString()?.trim().orEmpty()
        val weight = weightText.toDoubleOrNull()
        val error = when {
            name.isBlank() -> "이름을 입력해 주세요."
            date != null && runCatching { LocalDate.parse(date) }.getOrNull()?.isAfter(LocalDate.now()) != false -> "생년월일을 확인해 주세요."
            weightText.isNotEmpty() && (weight == null || weight !in 0.0..999.99) -> "몸무게를 확인해 주세요."
            else -> null
        }
        if (error != null) {
            binding.tvFormError.text = error
            binding.tvFormError.visibility = View.VISIBLE
            return
        }
        binding.btnSavePet.isEnabled = false
        viewModel.savePet(
            petId().takeIf { it > 0 },
            PetRequestDto(
                name, listOf("DOG", "CAT", "OTHER")[binding.spinnerSpecies.selectedItemPosition],
                binding.etBreed.text?.toString()?.trim().orEmpty().ifEmpty { null },
                listOf("unknown", "male", "female")[binding.spinnerGender.selectedItemPosition], date, weight,
                binding.switchNeutered.isChecked,
                binding.etAllergies.text?.toString()?.trim().orEmpty().ifEmpty { null },
                binding.etChronicDiseases.text?.toString()?.trim().orEmpty().ifEmpty { null },
                binding.etMedications.text?.toString()?.trim().orEmpty().ifEmpty { null }
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
