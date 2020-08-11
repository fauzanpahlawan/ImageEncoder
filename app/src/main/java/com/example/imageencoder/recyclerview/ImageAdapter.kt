package com.example.imageencoder.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.imageencoder.R
import com.example.imageencoder.entity.Image
import kotlinx.android.synthetic.main.item_image.view.*

class ImageAdapter(
    private var images: List<Image>,
    private val onItemClickListener: (Image) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    fun setNewData(images: List<Image>) {
        this.images = images
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder =
        ImageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        )


    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]
        holder.bind(image, onItemClickListener)
    }

    override fun getItemCount(): Int {
        return if (images.isNotEmpty()) images.size else 0
    }

    class ImageViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(image: Image, onItemClickListener: (Image) -> Unit) {
            with(itemView) {
                setOnClickListener { onItemClickListener(image) }
                tv_img_id.text = image.imageId.toString()
                tv_base64_data.text = image.imageBase64.toString()
            }
        }
    }
}