package kr.ac.anu.mumu.presentation.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.ItemHistoryBinding
import kr.ac.anu.mumu.domain.model.AnalysisHistory

class HistoryAdapter(
    private val onClick: (AnalysisHistory) -> Unit
) : ListAdapter<AnalysisHistory, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback) {

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AnalysisHistory) {
            val context = binding.root.context

            binding.tvDate.text = item.date
            binding.tvProbability.text = "확률 : ${item.probability}%"

            if (item.isNormal) {
                val greenColor = ContextCompat.getColor(context, R.color.cardgreen)
                binding.cardResult.strokeColor = greenColor
                binding.tvResultStatus.text = "검사결과 : 정상"
                binding.tvDescription.text = "검사목록 : ${item.behaviorText}"
            } else {
                val redColor = ContextCompat.getColor(context, R.color.cardred)
                binding.cardResult.strokeColor = redColor
                binding.tvResultStatus.text = "검사결과 : 비정상"
                binding.tvDescription.text = "의심행동 : ${item.behaviorText}"
            }
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object HistoryDiffCallback : DiffUtil.ItemCallback<AnalysisHistory>() {
        override fun areItemsTheSame(oldItem: AnalysisHistory, newItem: AnalysisHistory): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: AnalysisHistory, newItem: AnalysisHistory): Boolean =
            oldItem == newItem
    }
}
