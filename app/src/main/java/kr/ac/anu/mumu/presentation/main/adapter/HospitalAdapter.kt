package kr.ac.anu.mumu.presentation.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import kr.ac.anu.mumu.data.model.HospitalListDto
import kr.ac.anu.mumu.databinding.ItemHospitalBinding

class HospitalAdapter(private val onClick: (HospitalListDto) -> Unit) : ListAdapter<HospitalListDto, HospitalAdapter.ViewHolder>(Diff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(ItemHospitalBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemHospitalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HospitalListDto) = with(binding) {
            tvHospitalName.text = item.name
            tvHospitalAddress.text = item.address ?: "주소 정보 없음"
            tvHospitalMeta.text = "★ %.1f (%d) · %.1fkm".format(item.averageRating ?: 0.0, item.totalReviews ?: 0, item.distanceKm ?: 0.0)
            if (item.thumbnailUrl.isNullOrBlank()) ivHospital.setImageDrawable(null) else ivHospital.load(item.thumbnailUrl)
            root.setOnClickListener { onClick(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<HospitalListDto>() {
        override fun areItemsTheSame(oldItem: HospitalListDto, newItem: HospitalListDto) = oldItem.hospitalId == newItem.hospitalId
        override fun areContentsTheSame(oldItem: HospitalListDto, newItem: HospitalListDto) = oldItem == newItem
    }
}
