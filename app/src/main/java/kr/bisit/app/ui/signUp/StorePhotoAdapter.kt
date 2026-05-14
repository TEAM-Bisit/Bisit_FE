package kr.bisit.app.ui.signUp

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kr.bisit.app.databinding.ItemStoreImageAddBinding
import kr.bisit.app.databinding.ItemStorePhotoBinding

class StorePhotoAdapter(
    private val onAddClick: () -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Uri>() // 이미지 URI 리스트
    private val MAX_IMAGE_COUNT = 5

    companion object {
        private const val TYPE_PHOTO = 0
        private const val TYPE_ADD = 1
    }

    override fun getItemViewType(position: Int): Int {
        // 아이템 개수가 최대치보다 적고, 마지막 포지션인 경우 '추가' 버튼 표시
        return if (position < items.size) TYPE_PHOTO else TYPE_ADD
    }

    override fun getItemCount(): Int {
        // 이미지 개수가 최대치 미만이면 '추가' 버튼 포함 (+1), 아니면 이미지들만 표시
        return if (items.size < MAX_IMAGE_COUNT) items.size + 1 else items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_PHOTO) {
            val binding = ItemStorePhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            PhotoViewHolder(binding)
        } else {
            val binding = ItemStoreImageAddBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AddViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PhotoViewHolder) {
            holder.bind(items[position], position)
        } else if (holder is AddViewHolder) {
            holder.bind()
        }
    }

    // 사진 아이템 뷰홀더
    inner class PhotoViewHolder(private val binding: ItemStorePhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: Uri, position: Int) {
            Glide.with(binding.ivPhoto.context)
                .load(uri)
                .override(300, 300)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.ivPhoto)

            binding.btnRemovePhoto.setOnClickListener {
                onDeleteClick(position)
            }
        }
    }

    // 추가 버튼 뷰홀더
    inner class AddViewHolder(private val binding: ItemStoreImageAddBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.root.setOnClickListener { onAddClick() }
        }
    }

    fun submitList(newItems: List<Uri>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}