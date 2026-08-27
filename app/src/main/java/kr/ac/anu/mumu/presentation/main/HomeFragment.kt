package kr.ac.anu.mumu.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.FragmentHomeBinding

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        if (savedInstanceState == null) {
            setTabActive(isPeedActive = true)
            replaceFragment(PostFragment())
        }

        binding.btnPeed.setOnClickListener {
            setTabActive(isPeedActive = true)
            replaceFragment(PostFragment())
        }

        binding.btnHistory.setOnClickListener {
            setTabActive(isPeedActive = false)
            replaceFragment(HistoryFragment())
        }

        binding.btnAnalyze.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_analysisStartFragment)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
