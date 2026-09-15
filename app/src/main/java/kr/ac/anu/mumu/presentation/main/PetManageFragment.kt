package kr.ac.anu.mumu.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.data.model.PetDto
import kr.ac.anu.mumu.databinding.FragmentPetManageBinding
import kr.ac.anu.mumu.databinding.ItemPetProfileBinding

@AndroidEntryPoint
class PetManageFragment : Fragment() {
    private var _binding: FragmentPetManageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentPetManageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        binding.btnAddPet.setOnClickListener { openForm(null) }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.uiState.collect(::render) }
        }
    }

    private fun render(state: MyUiState) {
        binding.progressPets.visibility = if (state is MyUiState.Loading) View.VISIBLE else View.GONE
        binding.layoutPets.removeAllViews()
        if (state is MyUiState.Ready) {
            binding.tvPetsState.visibility = if (state.pets.isEmpty()) View.VISIBLE else View.GONE
            binding.tvPetsState.text = "등록한 반려동물이 없어요."
            state.pets.forEach { pet ->
                val row = ItemPetProfileBinding.inflate(layoutInflater, binding.layoutPets, false)
                row.tvPetName.text = pet.name
                row.tvPetInfo.text = listOfNotNull(pet.breed, pet.ageYears?.let { "${it}살" }, pet.speciesLabel).joinToString(" · ")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
