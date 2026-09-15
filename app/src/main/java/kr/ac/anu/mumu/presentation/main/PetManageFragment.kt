package kr.ac.anu.mumu.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil3.load
import coil3.request.error
import coil3.request.placeholder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.data.model.PetDto
import kr.ac.anu.mumu.databinding.FragmentPetManageBinding
import kr.ac.anu.mumu.databinding.ItemPetProfileBinding
import java.time.LocalDate
import java.time.Period

@AndroidEntryPoint
class PetManageFragment : Fragment() {
    private var _binding: FragmentPetManageBinding? = null
    private val binding get() = _binding!!
    private var selectedId: Long? = null
    private val viewModel: MyViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentPetManageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        binding.btnAddPet.setOnClickListener { openForm(null) }
        binding.btnSaveSelection.setOnClickListener {
            selectedId?.let(viewModel::selectPet)
            findNavController().navigateUp()
        }
        binding.btnEditSelection.setOnClickListener {
            (viewModel.uiState.value as? MyUiState.Ready)?.pets?.firstOrNull { it.petId == selectedId }?.let(::openForm)
        }
        binding.tvPetsState.setOnClickListener { viewModel.loadPets() }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.events.collect { if (it is MyEvent.Message) Toast.makeText(requireContext(), it.text, Toast.LENGTH_SHORT).show() } }
            }
        }
    }

    private fun render(state: MyUiState) {
        binding.progressPets.visibility = if (state is MyUiState.Loading) View.VISIBLE else View.GONE
        binding.btnAddPet.isEnabled = state is MyUiState.Ready && !state.isSaving
        binding.btnSaveSelection.isEnabled = state is MyUiState.Ready && !state.isSaving && state.pets.isNotEmpty()
        binding.btnEditSelection.isEnabled = binding.btnSaveSelection.isEnabled
        binding.layoutPets.removeAllViews()
        if (state is MyUiState.Ready) {
            if (state.pets.none { it.petId == selectedId }) selectedId = state.selectedPetId
            binding.tvPetsState.visibility = if (state.pets.isEmpty()) View.VISIBLE else View.GONE
            binding.tvPetsState.text = "등록한 반려동물이 없어요."
            state.pets.forEach { pet ->
                val row = ItemPetProfileBinding.inflate(layoutInflater, binding.layoutPets, false)
                row.ivPetProfile.scaleType = if (pet.profileImageUrl.isNullOrBlank()) ImageView.ScaleType.CENTER_INSIDE else ImageView.ScaleType.CENTER_CROP
                row.ivPetProfile.load(pet.profileImageUrl) {
                    placeholder(R.drawable.ic_foot)
                    error(R.drawable.ic_foot)
                }
                row.root.alpha = if (selectedId == pet.petId) 1f else 0.65f
                row.root.isSelected = selectedId == pet.petId
                row.root.contentDescription = "${pet.name}, ${if (selectedId == pet.petId) "선택됨" else "선택 안 됨"}. 길게 눌러 삭제"
                row.root.setOnClickListener { if (!state.isSaving) { selectedId = pet.petId; render(state) } }
                row.root.setOnLongClickListener { if (!state.isSaving) confirmDelete(pet); true }
                row.tvPetName.text = pet.name
                val age = pet.ageYears ?: runCatching { Period.between(LocalDate.parse(pet.birthDate), LocalDate.now()).years.coerceAtLeast(0) }.getOrNull()
                val species = pet.speciesLabel ?: when (pet.species?.uppercase()) {
                    "DOG" -> "강아지"
                    "CAT" -> "고양이"
                    else -> "기타"
                }
                row.tvPetInfo.text = listOfNotNull(age?.let { "${it}살" }, species).joinToString(" · ")
                row.btnSelectPet.text = if (state.selectedPetId == pet.petId) "대표 아이 ✓" else "대표로 선택"
                row.btnSelectPet.setOnClickListener { viewModel.selectPet(pet.petId) }
                row.btnEditPet.setOnClickListener { openForm(pet) }
                row.btnDeletePet.setOnClickListener { confirmDelete(pet) }
                binding.layoutPets.addView(row.root)
            }
        } else if (state is MyUiState.Error) {
            binding.tvPetsState.visibility = View.VISIBLE
            binding.tvPetsState.text = state.message
        }
    }

    private fun openForm(pet: PetDto?) {
        findNavController().navigate(R.id.petFormFragment, Bundle().apply { putLong("petId", pet?.petId ?: -1L) })
    }

    private fun confirmDelete(pet: PetDto) {
        AlertDialog.Builder(requireContext()).setMessage("${pet.name} 프로필을 삭제할까요?")
            .setNegativeButton("취소", null).setPositiveButton("삭제") { _, _ -> viewModel.deletePet(pet.petId) }.show()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.uiState.value !is MyUiState.Loading) viewModel.loadPets()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
