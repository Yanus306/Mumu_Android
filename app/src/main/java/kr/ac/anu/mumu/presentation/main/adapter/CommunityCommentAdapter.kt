package kr.ac.anu.mumu.presentation.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.anu.mumu.databinding.ItemCommunityCommentBinding
import kr.ac.anu.mumu.domain.model.CommunityComment

class CommunityCommentAdapter :
    ListAdapter<CommunityComment, CommunityCommentAdapter.CommentViewHolder>(DiffCallback) {

    class CommentViewHolder(
        private val binding: ItemCommunityCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: CommunityComment) {
            binding.tvAuthor.text = "사용자 ${comment.userId}"
            binding.tvContent.text = comment.content
            binding.tvMeta.text =
                "${comment.createdAt.take(10).replace('-', '.')} · 좋아요 ${comment.likeCount}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommunityCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<CommunityComment>() {
        override fun areItemsTheSame(oldItem: CommunityComment, newItem: CommunityComment) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CommunityComment, newItem: CommunityComment) = oldItem == newItem
    }
}
