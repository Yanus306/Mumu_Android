package kr.ac.anu.mumu.presentation.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import kr.ac.anu.mumu.databinding.ItemCommunityPostBinding
import kr.ac.anu.mumu.domain.model.CommunityPost

class CommunityFeedAdapter(
    private val onClick: (CommunityPost) -> Unit
) : ListAdapter<CommunityPost, CommunityFeedAdapter.PostViewHolder>(PostDiffCallback) {

    class PostViewHolder(
        private val binding: ItemCommunityPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: CommunityPost, onClick: (CommunityPost) -> Unit) = with(binding) {
            tvAuthor.text = "사용자 ${post.userId}"
            tvDate.text = post.createdAt.take(10).replace('-', '.')
            tvCategory.text = post.category.toCategoryLabel()
            tvTitle.text = post.title
            tvContent.text = post.content
            tvHashtags.text = post.hashtags.joinToString(" ") { "#$it" }
            tvHashtags.visibility = if (post.hashtags.isEmpty()) View.INVISIBLE else View.VISIBLE
            tvLikeCount.text = post.likeCount.toString()
            ivPostImage.contentDescription = post.title

            if (post.thumbnailUrl.isNullOrBlank()) {
                ivPostImage.visibility = View.GONE
            } else {
                ivPostImage.visibility = View.VISIBLE
                ivPostImage.load(post.thumbnailUrl)
            }
            root.setOnClickListener { onClick(post) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(
            ItemCommunityPostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    private object PostDiffCallback : DiffUtil.ItemCallback<CommunityPost>() {
        override fun areItemsTheSame(oldItem: CommunityPost, newItem: CommunityPost) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CommunityPost, newItem: CommunityPost) = oldItem == newItem
    }
}

private fun String.toCategoryLabel(): String = when (uppercase()) {
    "FREE" -> "자유"
    "QUESTION" -> "질문"
    "INFO" -> "정보"
    "BRAG" -> "자랑"
    "REVIEW" -> "후기"
    else -> this
}
