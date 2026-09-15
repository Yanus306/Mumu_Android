package kr.ac.anu.mumu.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.databinding.DialogHospitalDetailBinding
import kr.ac.anu.mumu.databinding.FragmentHospitalBinding
import kr.ac.anu.mumu.presentation.main.adapter.HospitalAdapter
import java.text.NumberFormat

@AndroidEntryPoint
class HospitalFragment : Fragment() {

    private var _binding: FragmentHospitalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HospitalViewModel by viewModels()
    private val adapter = HospitalAdapter { viewModel.open(it.hospitalId) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHospitalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvHospitals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHospitals.adapter = adapter
        binding.btnHospitalSearch.setOnClickListener { viewModel.search(binding.etHospitalSearch.text.toString()) }
        binding.etHospitalSearch.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.search(binding.etHospitalSearch.text.toString())
                true
            } else {
                false
            }
        }
        binding.btnMoreHospitals.setOnClickListener { viewModel.loadMore() }
        binding.tvHospitalState.setOnClickListener { viewModel.search(binding.etHospitalSearch.text.toString()) }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch {
                    viewModel.events.collect {
                        when (it) {
                            is HospitalEvent.Open -> showDetail(it.content)
                            is HospitalEvent.Message -> Toast.makeText(requireContext(), it.text, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun render(state: HospitalUiState) {
        binding.progressHospitals.visibility = if (state is HospitalUiState.Loading) View.VISIBLE else View.GONE
        binding.tvHospitalState.visibility = if (state is HospitalUiState.Error || state is HospitalUiState.Ready && state.hospitals.isEmpty()) View.VISIBLE else View.GONE
        binding.tvHospitalState.text = when (state) {
            is HospitalUiState.Error -> "${state.message}\n탭해서 다시 시도해 주세요."
            is HospitalUiState.Ready -> "검색 결과가 없습니다."
            HospitalUiState.Loading -> ""
        }
        if (state is HospitalUiState.Ready) {
            adapter.submitList(state.hospitals)
            binding.btnMoreHospitals.visibility = if (state.nextPage == null) View.GONE else View.VISIBLE
            binding.btnMoreHospitals.isEnabled = !state.isLoadingMore
            binding.btnMoreHospitals.text = if (state.isLoadingMore) "불러오는 중…" else "더 보기"
        } else {
            adapter.submitList(emptyList())
            binding.btnMoreHospitals.visibility = View.GONE
        }
    }

    private fun showDetail(content: HospitalDetailContent) {
        val dialogBinding = DialogHospitalDetailBinding.inflate(layoutInflater)
        val hospital = content.hospital
        dialogBinding.tvDetailName.text = hospital.name
        dialogBinding.tvDetailMeta.text = "★ %.1f · 리뷰 %d개".format(hospital.averageRating ?: 0.0, hospital.totalReviews ?: 0)
        dialogBinding.tvDetailContact.text = listOfNotNull(hospital.address, hospital.phone, hospital.website).joinToString("\n")
        dialogBinding.tvDetailSpecialties.text = hospital.specialties?.takeIf { it.isNotEmpty() }?.joinToString(" · ") ?: "진료 분야 정보가 없습니다."
        val currency = NumberFormat.getIntegerInstance()
        dialogBinding.tvDetailPrices.text = content.prices.takeIf { it.isNotEmpty() }?.joinToString("\n\n") {
            val range = listOfNotNull(it.minPrice, it.maxPrice).joinToString(" ~ ") { price -> "${currency.format(price)}원" }
            "${it.treatmentType} (${it.species ?: "공통"})\n${range.ifEmpty { "가격 문의" }}"
        } ?: "등록된 진료비 정보가 없습니다."
        dialogBinding.tvDetailReviews.text = content.reviews.takeIf { it.isNotEmpty() }?.joinToString("\n\n") {
            "★ ${it.rating}  ${it.content.orEmpty()}"
        } ?: "아직 작성된 리뷰가 없습니다."
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogBinding.root).create()
        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
