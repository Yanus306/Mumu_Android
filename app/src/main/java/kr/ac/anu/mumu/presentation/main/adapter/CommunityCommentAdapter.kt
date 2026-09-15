package kr.ac.anu.mumu.presentation.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.anu.mumu.databinding.ItemCommunityCommentBinding
import kr.ac.anu.mumu.domain.model.CommunityComment

class CommunityCommentAdapter(
    private val onEdit: (CommunityComment) -> Unit,
    private val onDelete: (CommunityComment) -> Unit
) :
    ListAdapter<CommunityComment, CommunityCommentAdapter.CommentViewHolder>(DiffCallback) {

    private var currentUserId: Long? = null

    inner class CommentViewHolder(
        private val binding: ItemCommunityCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: CommunityComment) {
            binding.tvAuthor.text = "사용자 ${comment.userId}"
            binding.tvContent.text = comment.content
            binding.tvMeta.text =
                "${comment.createdAt.take(10).replace('-', '.')} · 좋아요 ${comment.likeCount}"
            val isOwner = currentUserId != null && currentUserId == comment.userId
            binding.layoutCommentManage.visibility = if (isOwner) View.VISIBLE else View.GONE
            binding.btnEditComment.setOnClickListener { onEdit(comment) }
            binding.btnDeleteComment.setOnClickListener { onDelete(comment) }
        }
    }

    fun setCurrentUserId(userId: Long?) {
        if (currentUserId == userId) return
        currentUserId = userId
        notifyItemRangeChanged(0, itemCount)
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
