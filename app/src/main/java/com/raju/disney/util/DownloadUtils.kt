package com.raju.disney.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import java.io.File

object DownloadUtils {
  /*
  This method can be used to download an image from the internet using a url in Android. This use Android Download Manager to
  download the file and added it to the Gallery. Downloaded image will be saved to "Pictures"
  Folder in your internal storage
  */
  fun saveImage(context: Context, filename: String, downloadUrlOfImage: String?) {
    try {
      val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
      val downloadUri = Uri.parse(downloadUrlOfImage)
      val request = DownloadManager.Request(downloadUri)
      request
          .setAllowedNetworkTypes(
              DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
          .setAllowedOverRoaming(false)
          .setTitle(filename)
          .setMimeType(
              "image/gif") // Your file type. You can use this code to download other file types also
          .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
          .setDestinationInExternalPublicDir(
              Environment.DIRECTORY_PICTURES, File.separator + filename + ".gif")
      dm.enqueue(request)
      Toast.makeText(context, "Download started.", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
      Toast.makeText(context, "Download failed.", Toast.LENGTH_SHORT).show()
    }
  }
}
