package kr.ac.anu.mumu.presentation.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.ItemPostBinding
import kr.ac.anu.mumu.domain.model.CommunityPost

class PostAdapter(
    private val onClick: (CommunityPost) -> Unit
) : ListAdapter<CommunityPost, PostAdapter.PostViewHolder>(PostDiffCallback) {

    class PostViewHolder(
        private val binding: ItemPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: CommunityPost, onClick: (CommunityPost) -> Unit) {
            binding.ivPostImage.contentDescription = post.title
            if (post.thumbnailUrl.isNullOrBlank()) {
                binding.ivPostImage.setImageResource(R.drawable.ic_diary)
            } else {
                binding.ivPostImage.load(post.thumbnailUrl)
            }
            binding.root.setOnClickListener { onClick(post) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    private object PostDiffCallback : DiffUtil.ItemCallback<CommunityPost>() {
        override fun areItemsTheSame(oldItem: CommunityPost, newItem: CommunityPost): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CommunityPost, newItem: CommunityPost): Boolean {
            return oldItem == newItem
        }
    }
}
