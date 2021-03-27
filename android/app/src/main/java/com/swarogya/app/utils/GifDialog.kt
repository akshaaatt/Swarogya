package com.swarogya.app.utils

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.swarogya.app.R
import pl.droidsonroids.gif.GifImageView

class GifDialog private constructor(builder: Builder) {
    private val title: String?
    private val message: String?
    private val positiveBtnText: String?
    private val negativeBtnText: String?
    private val positiveBtnColor: String?
    private val negativeBtnColor: String?
    private val activity: Activity
    private val positiveListener: GifDialogListener?
    private val negativeListener: GifDialogListener?
    private val cancel: Boolean
    private var gifImageResource: Int

    interface GifDialogListener {
        fun onClick()
    }

    class Builder(val activity: Activity) {
        var title: String? = null
        var message: String? = null
        var positiveBtnText: String? = null
        var negativeBtnText: String? = null
        var positiveBtnColor: String? = null
        var negativeBtnColor: String? = null
        var positiveListener: GifDialogListener? = null
        var negativeListener: GifDialogListener? = null
        var cancel = false
        var gifImageResource = 0
        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        fun setMessage(message: String?): Builder {
            this.message = message
            return this
        }

        fun setPositiveBtnText(positiveBtnText: String?): Builder {
            this.positiveBtnText = positiveBtnText
            return this
        }

        fun setNegativeBtnText(negativeBtnText: String?): Builder {
            this.negativeBtnText = negativeBtnText
            return this
        }

        fun setPositiveBtnBackground(positiveBtnColor: String?): Builder {
            this.positiveBtnColor = positiveBtnColor
            return this
        }

        fun setNegativeBtnBackground(negativeBtnColor: String?): Builder {
            this.negativeBtnColor = negativeBtnColor
            return this
        }

        //set Positive Listener
        fun OnPositiveClicked(positiveListener: GifDialogListener?): Builder {
            this.positiveListener = positiveListener
            return this
        }

        //set Negative Listener
        fun OnNegativeClicked(negativeListener: GifDialogListener?): Builder {
            this.negativeListener = negativeListener
            return this
        }

        fun isCancellable(cancel: Boolean): Builder {
            this.cancel = cancel
            return this
        }

        //set GIF Resource
        fun setGifResource(gifImageResource: Int): Builder {
            this.gifImageResource = gifImageResource
            return this
        }

        fun build(): GifDialog {
            val message1: TextView
            val title1: TextView
            var iconImg: ImageView
            val pBtn: Button
            val nBtn: Button
            val gifImageView: GifImageView
            val dialog = Dialog(activity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(cancel)
            dialog.setContentView(R.layout.gif_dialog)

            //init
            title1 = dialog.findViewById<View>(R.id.title) as TextView
            message1 = dialog.findViewById<View>(R.id.message) as TextView
            pBtn = dialog.findViewById<View>(R.id.positiveBtn) as Button
            nBtn = dialog.findViewById<View>(R.id.negativeBtn) as Button
            gifImageView = dialog.findViewById(R.id.gifImageView)
            gifImageView.setImageResource(gifImageResource)

            //values
            title1.text = title
            message1.text = message
            if (positiveBtnText != null) {
                pBtn.text = positiveBtnText
            }
            if (negativeBtnText != null) {
                nBtn.text = negativeBtnText
            }
            if (positiveBtnColor != null) {
                val bgShape = pBtn.background as GradientDrawable
                bgShape.setColor(Color.parseColor(positiveBtnColor))
            }
            if (negativeBtnColor != null) {
                val bgShape = nBtn.background as GradientDrawable
                bgShape.setColor(Color.parseColor(negativeBtnColor))
            }
            if (positiveListener != null) {
                pBtn.setOnClickListener {
                    positiveListener!!.onClick()
                    dialog.dismiss()
                }
            } else {
                pBtn.setOnClickListener { dialog.dismiss() }
            }
            if (negativeListener != null) {
                nBtn.visibility = View.VISIBLE
                nBtn.setOnClickListener {
                    negativeListener!!.onClick()
                    dialog.dismiss()
                }
            }
            dialog.show()
            return GifDialog(this)
        }

    }

    init {
        title = builder.title
        message = builder.message
        activity = builder.activity
        positiveListener = builder.positiveListener
        negativeListener = builder.negativeListener
        positiveBtnColor = builder.positiveBtnColor
        negativeBtnColor = builder.negativeBtnColor
        positiveBtnText = builder.positiveBtnText
        negativeBtnText = builder.negativeBtnText
        gifImageResource = builder.gifImageResource
        cancel = builder.cancel
    }
}