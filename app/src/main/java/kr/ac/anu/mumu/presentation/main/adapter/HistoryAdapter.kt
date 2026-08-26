package kr.ac.anu.mumu.presentation.main.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.ItemHistoryBinding
import kr.ac.anu.mumu.domain.model.AnalysisHistory

class HistoryAdapter(
    private var items: List<AnalysisHistory> = emptyList() // 초기값은 빈 리스트
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    // 외부(프래그먼트나 뷰모델)에서 데이터를 업데이트할 때 호출할 함수
    fun submitList(newItems: List<AnalysisHistory>) {
        items = newItems
        notifyDataSetChanged() // 데이터가 바뀌었음을 리사이클러뷰에 알림
    }

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AnalysisHistory) {
            val context = binding.root.context

            // 1. 공통 데이터 바인딩
            binding.tvDate.text = item.date
            binding.tvProbability.text = "확률 : ${item.probability}%"

            // 2. 상태(정상/비정상)에 따른 동적 UI 변경
            if (item.isNormal) {
                // 정상: 초록색 테두리
                val greenColor = ContextCompat.getColor(context, R.color.mumu_300)
                binding.cardResult.strokeColor = greenColor
                binding.tvResultStatus.text = "검사결과 : 정상"
                binding.tvDescription.text = "검사목록 : ${item.behaviorText}"
            } else {
                // 비정상: 빨간색 테두리
                val redColor = Color.parseColor("#FF5252") // 또는 colors.xml에 정의한 색상 사용
                binding.cardResult.strokeColor = redColor
                binding.tvResultStatus.text = "검사결과 : 비정상"
                binding.tvDescription.text = "의심행동 : ${item.behaviorText}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}