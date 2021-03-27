package com.swarogya.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.text.InputFilter
import android.text.Spanned
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.swarogya.app.utils.view_animator.ViewAnimator
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

var toast: Toast? = null
const val permissionsRequest: Int = 931

const val sharedPrefFile = "swarogyaSharedPreferences"
val permissionsToGive = arrayOf(
  Manifest.permission.CAMERA,
  Manifest.permission.ACCESS_COARSE_LOCATION,
  Manifest.permission.INTERNET,
  Manifest.permission.ACCESS_FINE_LOCATION,
  Manifest.permission.WRITE_EXTERNAL_STORAGE,
  Manifest.permission.READ_EXTERNAL_STORAGE
)

internal fun Activity.toast(message: CharSequence) {
  toast?.cancel()
  toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
    .apply { show() }
}

typealias PrefEditor = SharedPreferences.Editor

internal fun SharedPreferences.boolean(
  key: String,
  defaultValue: Boolean = false
): Boolean {
  return getBoolean(key, defaultValue)
}

internal inline fun SharedPreferences.commit(crossinline exec: PrefEditor.() -> Unit) {
  val editor = this.edit()
  editor.exec()
  editor.apply()
}
fun askForPermissions(activity: Activity, permissions: Array<String>, requestCode: Int): Boolean {
  val permissionsToRequest: MutableList<String> = ArrayList()
  for (permission in permissions) {
    if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
      permissionsToRequest.add(permission)
    }
  }
  if (permissionsToRequest.isEmpty()) {
    return false
  }
  if (permissionsToRequest.isNotEmpty()) {
    ActivityCompat.requestPermissions(activity, permissionsToRequest.toTypedArray(), requestCode)
  }
  return true
}
fun logoAnimate(imageView: ImageView) {
  ViewAnimator.animate(imageView)
    .dp().width(150f, 250f)
    .alpha(1F, 0.1f)
    .interpolator(DecelerateInterpolator())
    .duration(800)
    .thenAnimate(imageView)
    .dp().width(250f, 150f)
    .alpha(0.1f, 1f)
    .interpolator(AccelerateInterpolator())
    .duration(1200)
    .start()
}
fun uploadFile(file: File, index: Int, docName: String, documentId:String,context: Context) {
  GlobalScope.launch(Dispatchers.Default) {
    FirebaseStorage.getInstance().reference.child("OngoingTreatments")
      .child(documentId).child("Documents")
      .child(docName).child("Pic$index")
      .putFile(Uri.fromFile(Compressor.compress(context, file)))
      .addOnSuccessListener { taskSnapshot ->
        taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { fileUri: Uri ->
          FirebaseFirestore.getInstance().collection("OngoingTreatments")
            .document(documentId).update("documents.$docName.Pic$index",fileUri.toString())
        }
      }
  }
}
class PercentageInputFilter internal constructor(val min: Float, val max: Float) : InputFilter {
  override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
    try {
      // Get input
      val stringToMatch = dest.toString() + source.toString()
      val input = stringToMatch.toFloat()

      // Check if the input is in range.
      if (isInRange(min, max, input)) {
        // return null to accept the original replacement in case the format matches and text is in range.
        return null
      }
    } catch (nfe: NumberFormatException) {
    }
    return ""
  }
  private fun isInRange(min: Float, max: Float, input: Float): Boolean {
    return input in min..max
  }
}