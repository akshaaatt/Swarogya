package com.swarogya.app.health_professional

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.swarogya.app.R
import com.swarogya.app.models.OngoingTreatments
import java.util.*

class UserCardAdapter(private val context: Context, private val types: ArrayList<OngoingTreatments>, private val stalkingRecyclerClickListener: OnClickListener) : RecyclerView.Adapter<UserCardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.user_card, parent, false)
        return ViewHolder(v, stalkingRecyclerClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val type = types[position]

        holder.toolbar.title = type.name
        holder.toolbar.subtitle = type.patientId

        Glide.with(context).asDrawable().load(type.facePic)
            .transform(CenterCrop(), RoundedCorners(20))
            .override(context.resources.getDimension(R.dimen.custom_image_chats).toInt(), context.resources.getDimension(R.dimen.custom_image_chats).toInt())
            .into(object : CustomTarget<Drawable?>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable?>?) {
                    holder.toolbar.navigationIcon = resource
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    override fun getItemCount(): Int {
        return types.size
    }

    inner class ViewHolder(itemView: View, clickListener: OnClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val toolbar: Toolbar = itemView.findViewById(R.id.toolbar)
        var onClickListener: OnClickListener = clickListener

        override fun onClick(v: View) {
            onClickListener.onUserClicked(adapterPosition)
        }

        init {
            toolbar.setOnClickListener(this)
        }
    }

    interface OnClickListener {
        fun onUserClicked(position: Int)
    }

}