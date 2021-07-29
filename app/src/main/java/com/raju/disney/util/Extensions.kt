import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * Extension method used to display a [Toast] message to the user.
 */
fun Fragment.TOAST(messageRes: String?, duration: Int = Toast.LENGTH_SHORT) {
  Toast.makeText(context, messageRes, duration).show()
}
