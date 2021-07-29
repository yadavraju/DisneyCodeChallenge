package com.raju.disney.ui.adapter

import android.view.View
import com.raju.disney.R
import com.raju.disney.data.GData
import com.raju.disney.ui.viewholder.GiphyViewHolder
import com.raju.disney.ui.adapter.ViewType.GIPHY_IMAGE

class GiphyAdapter(private val giphyGData: GData,
                   private val listener: (uri: String?) -> Unit)
    : BaseDataBoundAdapter<GiphyViewHolder>(R.layout.cell_image_layout) {
    
    override fun createItemViewHolder(view: View): GiphyViewHolder {
        return GiphyViewHolder(view)
    }
    
    override fun bindItemViewHolder(holder: GiphyViewHolder) {
        holder.bind(giphyGData) { listener(giphyGData.images.original.url) }
    }
    
    override val viewType: ViewType
        get() = GIPHY_IMAGE
}
