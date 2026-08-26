package kr.ac.anu.mumu.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kr.ac.anu.mumu.databinding.FragmentHistoryBinding
import kr.ac.anu.mumu.domain.model.AnalysisHistory
import kr.ac.anu.mumu.presentation.main.adapter.HistoryAdapter

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    // 어댑터 전역 변수 선언
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 리사이클러뷰와 어댑터 연결
        setupRecyclerView()

        // 2. 데이터 불러오기 (테스트용)
        // 실제로는 ViewModel에서 서버 데이터를 관찰(observe)하고 변경될 때 submitList를 호출합니다.
        loadDummyData()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.rvHistory.apply {
            adapter = historyAdapter
            // XML에 app:layoutManager를 설정해 두었다면 아래 줄은 생략해도 됩니다.
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadDummyData() {
        // 방금 만든 도메인 모델 형태에 맞춰 임시 데이터를 만듭니다.
        val dummyData = listOf(
            AnalysisHistory(id = 1L, isNormal = false, behaviorText = "자세 비정상, 발바닥 이상", probability = 72, date = "2026.04.15"),
            AnalysisHistory(id = 2L, isNormal = true, behaviorText = "자세 비정상, 절뚝거림", probability = 72, date = "2026.04.15"),
            AnalysisHistory(id = 3L, isNormal = true, behaviorText = "자세 비정상, 절뚝거림", probability = 72, date = "2026.04.15")
        )

        // 생성한 데이터를 어댑터에 넘겨서 화면에 그려지게 합니다.
        historyAdapter.submitList(dummyData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 메모리 누수 방지
        _binding = null
    }
}