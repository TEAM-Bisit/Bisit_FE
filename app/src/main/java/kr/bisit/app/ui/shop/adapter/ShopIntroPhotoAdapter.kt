package kr.bisit.app.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kr.bisit.app.databinding.ItemStoreImageAddBinding
import kr.bisit.app.databinding.ItemStorePhotoBinding
import kr.bisit.app.data.model.shop.ShopPhotoItem

class ShopPhotoAdapter(
    private val maxCount: Int = 5,
    private val onAddClick: () -> Unit,
    private val onDeleteClick: (photoId: Long) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ShopPhotoItem>()

    companion object {
        private const val TYPE_PHOTO = 0
        private const val TYPE_ADD = 1
    }

    /* ===================== Adapter 기본 ===================== */

    override fun getItemCount(): Int {
        return if (items.size < maxCount) items.size + 1 else items.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < items.size) TYPE_PHOTO else TYPE_ADD
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_PHOTO) {
            PhotoViewHolder(
                ItemStorePhotoBinding.inflate(inflater, parent, false)
            )
        } else {
            AddViewHolder(
                ItemStoreImageAddBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PhotoViewHolder -> holder.bind(items[position])
            is AddViewHolder -> holder.bind()
        }
    }

    /* ===================== ViewHolder ===================== */

    inner class PhotoViewHolder(
        private val binding: ItemStorePhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShopPhotoItem) {
            Glide.with(binding.ivPhoto.context)
                .load(item.url)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.ivPhoto)

            binding.btnRemovePhoto.setOnClickListener {
                onDeleteClick(item.id)
            }
        }
    }

    inner class AddViewHolder(
        private val binding: ItemStoreImageAddBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.root.setOnClickListener {
                onAddClick()
            }
        }
    }

    /* ===================== 외부 제어 ===================== */

    fun submitList(newItems: List<ShopPhotoItem>) {
        items.clear()
        items.addAll(newItems.sortedBy { it.sortOrder })
        notifyDataSetChanged()
    }
}
