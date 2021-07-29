package com.raju.disney.api.databinding

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.raju.disney.util.SpaceItemDecoration

/**
 * @param recyclerView RecyclerView to bind to SpaceItemDecoration
 * @param spaceInPx space in pixels
 */
@BindingAdapter("spaceItemDecoration")
fun RecyclerView.addItemDecoration(spaceInPx: Float) {
  if (spaceInPx != 0f) {
    val space = spaceInPx.toInt()
    val itemDecoration = SpaceItemDecoration(space, space, space, space)
    this.addItemDecoration(itemDecoration)
  }
}

@BindingAdapter("srcUrl", "circleCrop", "placeholder", requireAll = false)
fun ImageView.bindSrcUrl(url: String, circleCrop: Boolean = false, placeholder: Drawable? = null) {
  val request = Glide.with(this).asGif().load(url)
  if (circleCrop) request.circleCrop()
  if (placeholder != null) request.placeholder(placeholder)
  request.into(this)
}
