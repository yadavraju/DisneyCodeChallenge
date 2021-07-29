package com.raju.disney.util

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.raju.disney.R

object DialogUtils {

  fun showSaveGiphyDialogue(
      context: Context,
      downloadUrlOfImage: String,
      filename: String = "Edify"
  ) {
    val items =
        arrayOf<CharSequence>(
            context.getString(R.string.save_gif), context.getString(R.string.cancel))
    val builder = AlertDialog.Builder(context)
    builder.setTitle(context.getString(R.string.confirmation_message))
    builder.setItems(items) { dialog: DialogInterface, item: Int ->
      if (items[item] == context.getString(R.string.save_gif)) {
        DownloadUtils.saveImage(context, filename, downloadUrlOfImage)
      } else {
        dialog.dismiss()
      }
    }
    builder.show()
  }
}
