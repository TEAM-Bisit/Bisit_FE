package kr.bisit.app.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.bisit.app.databinding.ItemHomeStoreImageBinding

class HomeListImageAdapter(
    private val images: List<String>,
    private val hasVisitService: Boolean
) : RecyclerView.Adapter<HomeListImageAdapter.ImageViewHolder>() {

    private val TAG = "HomeListImageAdapter"

    inner class ImageViewHolder(val binding: ItemHomeStoreImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemHomeStoreImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = images[position]
        Log.d(TAG, "Loading image at position $position: $imageUrl")

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)  // 로딩 중
            .error(android.R.drawable.ic_menu_gallery)        // 에러 시 갤러리 아이콘
            .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                override fun onLoadFailed(
                    e: com.bumptech.glide.load.engine.GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e(TAG, "❌ Image load failed for: $imageUrl")
                    Log.e(TAG, "Error: ${e?.message}")
                    e?.logRootCauses(TAG)
                    return false
                }

                override fun onResourceReady(
                    resource: android.graphics.drawable.Drawable,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d(TAG, "✅ Image loaded successfully for: $imageUrl")
                    return false
                }
            })
            .into(holder.binding.imgStoreItem)

        holder.binding.chipVisitService.visibility =
            if (position == 0 && hasVisitService) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = images.size.also {
        Log.d(TAG, "Total images count: $it, images: $images")
    }
}
