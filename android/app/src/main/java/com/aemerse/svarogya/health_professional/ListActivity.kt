package com.aemerse.svarogya.health_professional

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.aemerse.svarogya.R
import com.aemerse.svarogya.databinding.ActivityListBinding
import com.aemerse.svarogya.utils.sharedPrefFile
import java.text.SimpleDateFormat
import java.util.*

class ListActivity : AppCompatActivity() {
    private var list: ArrayList<String?>? = null
    private var listAdapter: ListAdapter? = null
    private var llm: LinearLayoutManager? = null
    private lateinit var binding: ActivityListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        list = intent.getStringArrayListExtra("list")
        //To show at least one row
        if (list == null || list!!.size == 0) {
            list = ArrayList()
            list!!.add("")
        }
        listAdapter = ListAdapter(list!!, this)
        llm = LinearLayoutManager(this)

        //Setting the adapter
        binding.recyclerView.adapter = listAdapter
        binding.recyclerView.layoutManager = llm

        binding.titleIcon.setOnClickListener {
            onBackPressed()
        }

        binding.updateText.setOnClickListener {
            MaterialDialog(this).show {
                title(null, "Continue?")
                message(R.string.update_patient_medicines)
                icon(R.drawable.ic_pills_24p)
                positiveButton(R.string.proceed) {
                    list = listAdapter!!.medicinesList
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
                    val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Calendar.getInstance().time)

                    while (list!!.contains("")) {
                        list!!.remove("")
                    }
                    if(list!!.isEmpty()){
                        FirebaseFirestore.getInstance().collection("OngoingTreatments")
                            .document(intent.extras!!.getString("doc")!!)
                            .update("medicineRecords.$currentDate.$currentTime", getString(R.string.no_prescribed_medicines),
                                "medicines", FieldValue.delete(),
                                "lastCheckedBy",
                                getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE).getString(getString(R.string.professionalId), null)
                            )
                    }
                    else {
                        FirebaseFirestore.getInstance().collection("OngoingTreatments")
                            .document(intent.extras!!.getString("doc")!!)
                            .update(
                                "medicineRecords.$currentDate.$currentTime", list,
                                "medicines", list,
                                "lastCheckedBy",
                                getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE).getString(
                                    getString(R.string.professionalId),
                                    null
                                )
                            )
                    }
                    finish()
                }
                negativeButton(R.string.cancel)
                debugMode(false)
                lifecycleOwner(this@ListActivity)
            }
        }
    }
    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
    }

}