package com.aemerse.svarogya.health_professional

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.aemerse.svarogya.R
import java.util.*

class ListAdapter(var medicinesList: ArrayList<String?>, var context: Context) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var plus: ImageButton = itemView.findViewById(R.id.plus)
        var minus: ImageButton = itemView.findViewById(R.id.minus)
        var medicine: EditText = itemView.findViewById(R.id.medicine)

        init {
            minus.setOnClickListener {
                val position = adapterPosition
                try {
                    medicinesList.removeAt(position)
                    notifyItemRemoved(position)
                } catch (e: ArrayIndexOutOfBoundsException) {
                    e.printStackTrace()
                }
            }
            plus.setOnClickListener {
                val position = adapterPosition
                try {
                    medicinesList.add(position + 1, "")
                    notifyItemInserted(position + 1)
                } catch (e: ArrayIndexOutOfBoundsException) {
                    e.printStackTrace()
                }
            }
            medicine.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    medicinesList[adapterPosition] = s.toString()
                }

                override fun afterTextChanged(s: Editable) {}
            })
        }
    }

    override fun getItemCount(): Int {
        return medicinesList.size
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val x = holder.layoutPosition
        if (medicinesList[x]!!.isNotEmpty()) {
            holder.medicine.setText(medicinesList[x])
        } else {
            holder.medicine.text = null
            holder.medicine.hint = "Medicine name with dosage"
            holder.medicine.requestFocus()
        }
    }

}