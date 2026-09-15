package kr.ac.anu.mumu.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
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
import kr.ac.anu.mumu.databinding.FragmentHomeBinding

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private var isHistoryActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isHistoryActive = savedInstanceState?.getBoolean(KEY_HISTORY_ACTIVE) ?: isHistoryActive
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showTab(isHistory = isHistoryActive)

        binding.btnPeed.setOnClickListener {
            showTab(isHistory = false)
        }

        binding.btnHistory.setOnClickListener {
            showTab(isHistory = true)
        }

        binding.btnAnalyze.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_analysisStartFragment)
        }

        binding.btnWrite.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_communityWriteFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.petState.collect(::renderPet)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPet()
    }

    private fun renderPet(state: HomePetUiState) {
        val pet = (state as? HomePetUiState.Ready)?.pet
        binding.tvSubTitleName.text = pet?.name ?: "반려동물을 등록해 주세요"
        binding.tvPetName.text = pet?.name ?: "미등록"
        binding.tvRepeat.text = (pet?.recordDays ?: 0).toString()
        binding.tvPetAge.text = pet?.ageYears?.let { "${it}살" } ?: "나이 미상"
        binding.tvHeart.text = (pet?.likeCount ?: 0).toString()
        binding.btnAnalyze.isEnabled = pet != null
        if (pet?.profileImageUrl.isNullOrBlank()) {
            binding.ivPostProfile.setImageResource(R.drawable.dog)
        } else {
            binding.ivPostProfile.load(pet.profileImageUrl)
        }
    }

    private fun showTab(isHistory: Boolean) {
        val current = childFragmentManager.findFragmentById(binding.fragmentContainer.id)
        isHistoryActive = isHistory
        setTabActive(isPeedActive = !isHistory)
        val targetMatches = if (isHistory) current is HistoryFragment else current is PostFragment
        if (!targetMatches) {
            replaceFragment(if (isHistory) HistoryFragment() else PostFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }

    private fun setTabActive(isPeedActive: Boolean) {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.mumu_300)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.mumugray_150)

        val peedIcon = binding.btnPeed.getChildAt(0) as ImageView
        val peedText = binding.btnPeed.getChildAt(1) as TextView
        val historyIcon = binding.btnHistory.getChildAt(0) as ImageView
        val historyText = binding.btnHistory.getChildAt(1) as TextView

        if (isPeedActive) {
            peedIcon.setColorFilter(activeColor)
            peedText.setTextColor(activeColor)

            historyIcon.setColorFilter(inactiveColor)
            historyText.setTextColor(inactiveColor)
        } else {
            peedIcon.setColorFilter(inactiveColor)
            peedText.setTextColor(inactiveColor)

            historyIcon.setColorFilter(activeColor)
            historyText.setTextColor(activeColor)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_HISTORY_ACTIVE, isHistoryActive)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_HISTORY_ACTIVE = "history_active"
    }
}
