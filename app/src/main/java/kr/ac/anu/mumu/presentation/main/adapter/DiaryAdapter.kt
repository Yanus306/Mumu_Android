package kr.ac.anu.mumu.presentation.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import kr.ac.anu.mumu.data.model.DiaryListDto
import kr.ac.anu.mumu.databinding.ItemDiaryBinding

class DiaryAdapter(
    private val onClick: (DiaryListDto) -> Unit
) : ListAdapter<DiaryListDto, DiaryAdapter.ViewHolder>(Diff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemDiaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemDiaryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DiaryListDto) {
            val isHappy = item.mood.equals("happy", ignoreCase = true)
            binding.tvDiaryMood.text = if (isHappy) "☺" else "☹"
            binding.tvDiaryMood.setTextColor(
                ContextCompat.getColor(binding.root.context, if (isHappy) kr.ac.anu.mumu.R.color.cardgreen else kr.ac.anu.mumu.R.color.cardred)
            )
            binding.tvDiaryDate.text = item.diaryDate.replace('-', '.')
            binding.tvDiaryTitle.text = item.title
            binding.tvDiaryPreview.text = item.contentPreview.orEmpty()
            binding.ivDiaryThumbnail.visibility = if (item.thumbnailUrl.isNullOrBlank()) View.GONE else View.VISIBLE
            if (item.thumbnailUrl.isNullOrBlank()) {
                binding.ivDiaryThumbnail.setImageDrawable(null)
            } else {
                binding.ivDiaryThumbnail.load(item.thumbnailUrl)
            }
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<DiaryListDto>() {
        override fun areItemsTheSame(oldItem: DiaryListDto, newItem: DiaryListDto): Boolean =
            oldItem.diaryId == newItem.diaryId

        override fun areContentsTheSame(oldItem: DiaryListDto, newItem: DiaryListDto): Boolean = oldItem == newItem
    }
}

fun String.toMoodLabel(): String = when (lowercase()) {
    "happy" -> "행복"
    "sad" -> "속상"
    else -> "보통"
}
