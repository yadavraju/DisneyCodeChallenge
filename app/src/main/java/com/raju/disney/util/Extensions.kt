import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer

/**
 * Extension method used to display a [Toast] message to the user.
 */
fun Fragment.TOAST(messageRes: String?, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, messageRes, duration).show()
}

fun Context.TOAST(messageRes: String?, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, messageRes, duration).show()
}

fun ImageView.bindSrcUrl(url: String, circleCrop: Boolean = false, placeholder: Drawable? = null) {
    val request = Glide.with(this).load(url)
    if (circleCrop) request.circleCrop()
    if (placeholder != null) request.placeholder(placeholder)
    request.into(this)
}
