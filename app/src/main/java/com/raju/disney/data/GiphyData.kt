package com.raju.disney.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GiphyData(
    @SerializedName("data") val giphyGDataList: List<GData> = listOf(),
    @SerializedName("meta") val meta: Meta = Meta()
) : Parcelable

@Parcelize
data class GData(
    @SerializedName("bitly_gif_url") val bitlyGifUrl: String = "",
    @SerializedName("bitly_url") val bitlyUrl: String = "",
    @SerializedName("content_url") val contentUrl: String = "",
    @SerializedName("embed_url") val embedUrl: String = "",
    @SerializedName("id") val id: String = "",
    @SerializedName("images") val images: Images = Images(),
    @SerializedName("import_datetime") val importDatetime: String = "",
    @SerializedName("is_sticker") val isSticker: Int = 0,
    @SerializedName("rating") val rating: String = "",
    @SerializedName("slug") val slug: String = "",
    @SerializedName("source") val source: String = "",
    @SerializedName("source_post_url") val sourcePostUrl: String = "",
    @SerializedName("source_tld") val sourceTld: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("trending_datetime") val trendingDatetime: String = "",
    @SerializedName("type") val type: String = "",
    @SerializedName("url") val url: String = "",
    @SerializedName("username") val username: String = ""
) : Parcelable

@Parcelize
data class Images(
    @SerializedName("original") val original: Original = Original(),
    @SerializedName("preview") val preview: Preview = Preview()
) : Parcelable {
  @Parcelize
  data class Original(
      @SerializedName("frames") val frames: String = "",
      @SerializedName("hash") val hash: String = "",
      @SerializedName("height") val height: String = "",
      @SerializedName("mp4") val mp4: String = "",
      @SerializedName("mp4_size") val mp4Size: String = "",
      @SerializedName("size") val size: String = "",
      @SerializedName("url") val url: String = "",
      @SerializedName("webp") val webp: String = "",
      @SerializedName("webp_size") val webpSize: String = "",
      @SerializedName("width") val width: String = ""
  ) : Parcelable

  @Parcelize
  data class Preview(
      @SerializedName("height") val height: String = "",
      @SerializedName("mp4") val mp4: String = "",
      @SerializedName("mp4_size") val mp4Size: String = "",
      @SerializedName("width") val width: String = ""
  ) : Parcelable
}

@Parcelize
data class Meta(
    @SerializedName("msg") val msg: String = "",
    @SerializedName("response_id") val responseId: String = "",
    @SerializedName("status") val status: Int = 0
) : Parcelable
