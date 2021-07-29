package com.raju.disney.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BookData(
    @SerializedName("attributionHTML") val attributionHTML: String = "",
    @SerializedName("attributionText") val attributionText: String = "",
    @SerializedName("copyright") val copyright: String = "",
    @SerializedName("data") val data: Data = Data(),
    @SerializedName("etag") val etag: String = ""
) : Parcelable

@Parcelize
data class Data(@SerializedName("results") val results: List<Result> = listOf()) : Parcelable

@Parcelize
data class Result(
    @SerializedName("description") val description: String = "",
    @SerializedName("digitalId") val digitalId: Int = 0,
    @SerializedName("format") val format: String = "",
    @SerializedName("id") val id: Int = 0,
    @SerializedName("images") val images: List<ImageThumbUri> = listOf(),
    @SerializedName("isbn") val isbn: String = "",
    @SerializedName("issueNumber") val issueNumber: Int = 0,
    @SerializedName("modified") val modified: String = "",
    @SerializedName("pageCount") val pageCount: Int = 0,
    @SerializedName("prices") val prices: List<Price> = listOf(),
    @SerializedName("thumbnail") val thumbnail: ImageThumbUri = ImageThumbUri(),
    @SerializedName("title") val title: String = "",
    @SerializedName("variantDescription") val variantDescription: String = ""
) : Parcelable

@Parcelize
data class Price(
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("type") val type: String = ""
) : Parcelable

@Parcelize
data class ImageThumbUri(
    @SerializedName("extension") val extension: String = "",
    @SerializedName("path") val path: String = ""
) : Parcelable {
  val imageThumbUri: String = path + extension
}
