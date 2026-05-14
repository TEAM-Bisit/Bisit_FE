package kr.bisit.app.ui.myPage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.bisit.app.R

class MyPageAnnounceAdapter(
    private val announceList: List<Pair<String, String>>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<MyPageAnnounceAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconIv: ImageView = itemView.findViewById(R.id.announceIconIv)
        val titleTv: TextView = itemView.findViewById(R.id.announceTitleTv)
        val dateTv: TextView = itemView.findViewById(R.id.announceDateTv)

        init {
            itemView.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_page_announce, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (title, date) = announceList[position]

        holder.iconIv.setImageResource(R.drawable.ic_2star)
        holder.titleTv.text = title
        holder.dateTv.text = date
    }

    override fun getItemCount(): Int = announceList.size
}
