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

class PostAdapter : ListAdapter<CommunityPost, PostAdapter.PostViewHolder>(PostDiffCallback) {

    class PostViewHolder(
        private val binding: ItemPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: CommunityPost) {
            binding.ivPostImage.contentDescription = post.title
            if (post.thumbnailUrl.isNullOrBlank()) {
                binding.ivPostImage.setImageResource(R.drawable.ic_diary)
            } else {
                binding.ivPostImage.load(post.thumbnailUrl)
            }
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
        holder.bind(getItem(position))
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
