package kr.bisit.app.ui.signUp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.bisit.app.databinding.ItemStoreCategoryBinding

class StoreCategoryAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<String, StoreCategoryAdapter.CategoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemStoreCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(
        private val binding: ItemStoreCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(categoryPath: String) {
            // "대분류 > 중분류 > 소분류" 텍스트 세팅
            binding.tvCategoryPath.text = categoryPath

            // 아이템 클릭 시 콜백 호출
            binding.root.setOnClickListener {
                onItemClick(categoryPath)
            }
        }
    }

    // 리스트 변경 사항을 효율적으로 계산하기 위한 DiffUtil
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }
}