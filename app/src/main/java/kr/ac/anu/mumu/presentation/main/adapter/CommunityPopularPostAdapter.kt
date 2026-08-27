package kr.ac.anu.mumu.presentation.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.ItemCommunityPopularPostBinding
import kr.ac.anu.mumu.domain.model.CommunityPost

class CommunityPopularPostAdapter(
    private val onClick: (CommunityPost) -> Unit
) : ListAdapter<CommunityPost, CommunityPopularPostAdapter.PostViewHolder>(PostDiffCallback) {

    class PostViewHolder(
        private val binding: ItemCommunityPopularPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: CommunityPost, onClick: (CommunityPost) -> Unit) = with(binding) {
            tvTitle.text = post.title
            tvLikeCount.text = post.likeCount.toString()
            val heartColor = if (post.likeCount > 0) R.color.mumured else R.color.mumugray_150
            ivHeart.setColorFilter(ContextCompat.getColor(root.context, heartColor))
            tvLikeCount.setTextColor(ContextCompat.getColor(root.context, heartColor))
            root.setOnClickListener { onClick(post) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(
            ItemCommunityPopularPostBinding.inflate(
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
