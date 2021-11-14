package com.aemerse.svarogya.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.common.base.Strings
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.aemerse.svarogya.R
import com.aemerse.svarogya.databinding.MedicalRecordsActivityBinding
import com.aemerse.svarogya.utils.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MedicalRecordsActivity : AppCompatActivity() {

    private var vitalParametersAlert = false
    private lateinit var slot: String
    private lateinit var binding: MedicalRecordsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MedicalRecordsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.materialBPHighTextView.expand()
        binding.materialBPHighTextView.clearFocus()
        binding.materialBPHighTextView.setOnClickListener { }

        binding. materialBPLowTextView.expand()
        binding.materialBPLowTextView.clearFocus()
        binding.materialBPLowTextView.setOnClickListener { }

        binding.materialOxygenTextView.expand()
        binding.materialOxygenTextView.clearFocus()
        binding.materialOxygenTextView.setOnClickListener { }

        binding.materialPulseTextView.expand()
        binding.materialPulseTextView.clearFocus()
        binding.materialPulseTextView.setOnClickListener { }

        binding.materialTemperatureTextView.expand()
        binding.materialTemperatureTextView.clearFocus()
        binding.materialTemperatureTextView.setOnClickListener { }

        binding.scrollView.smoothScrollTo(0, 0)

        binding.bpHighText.filters = arrayOf(PercentageInputFilter(1.00F, 200.00F))
        binding.bpLowText.filters = arrayOf(PercentageInputFilter(1.00F, 150.00F))
        binding.oxygenText.filters = arrayOf(PercentageInputFilter(1.00F, 100.00F))
        binding.pulseText.filters = arrayOf(PercentageInputFilter(1.00F, 200.00F))
        binding.temperatureText.filters = arrayOf(PercentageInputFilter(1.00F, 110.00F))

        binding.updateText.setOnClickListener {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
            currentSlot()
            if(intent.extras!!.getString("user")!="professional" &&
                getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)
                    .getString("${intent.extras!!.getString("doc")}currentSlot",null)==slot &&
                getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)
                    .getString(getString(R.string.lastDate),null)==currentDate){

                toast("Already submitted the measurements for $slot")
                return@setOnClickListener
            }

            if(Strings.isNullOrEmpty(binding.bpHighText.text.toString().trim()) || binding.bpHighText.text.toString().trim()=="."){
                binding.bpHighText.error = "Enter BP High value"
                binding.bpHighText.requestFocus()
                return@setOnClickListener
            }
            if(Strings.isNullOrEmpty(binding.bpLowText.text.toString().trim()) || binding.bpLowText.text.toString().trim()=="."){
                binding.bpLowText.error = "Enter BP Low value"
                binding.bpLowText.requestFocus()
                return@setOnClickListener
            }
            if(Strings.isNullOrEmpty(binding.oxygenText.text.toString().trim()) || binding.oxygenText.text.toString().trim()=="."){
                binding.oxygenText.error = "Enter Oxygen Level"
                binding.oxygenText.requestFocus()
                return@setOnClickListener
            }
            if(Strings.isNullOrEmpty(binding.pulseText.text.toString().trim()) || binding.pulseText.text.toString().trim()=="."){
                binding.pulseText.error = "Enter Pulse per minute"
                binding.pulseText.requestFocus()
                return@setOnClickListener
            }
            if(Strings.isNullOrEmpty(binding.temperatureText.text.toString().trim()) || binding.temperatureText.text.toString().trim()=="."){
                binding.temperatureText.error = "Enter Temperature Level"
                binding.temperatureText.requestFocus()
                return@setOnClickListener
            }

            val measurementData = HashMap<String,Any>()
            val bpHigh = String.format("%.2f", binding.bpHighText.text.toString().trim().toFloat()).toDouble()
            val bpLow = String.format("%.2f", binding.bpLowText.text.toString().trim().toFloat()).toDouble()
            val pulse = String.format("%.2f", binding.pulseText.text.toString().trim().toFloat()).toDouble()
            val oxygenLevel = String.format("%.2f", binding.oxygenText.text.toString().trim().toFloat()).toDouble()
            val temperature = String.format("%.2f", binding.temperatureText.text.toString().trim().toFloat()).toDouble()

            measurementData["BP-High"] = bpHigh
            measurementData["BP-Low"] = bpLow
            measurementData["Pulse"] = pulse
            measurementData["Oxygen"] = oxygenLevel
            measurementData["Temperature"] = temperature

            GifDialog.Builder(this)
                .setTitle("Update")
                .setMessage("Sure about the details you just entered?")
                .setPositiveBtnText("Yes")
                .setPositiveBtnBackground("#22b573")
                .setNegativeBtnText("No")
                .setNegativeBtnBackground("#c1272d")
                .setGifResource(R.raw.submit2)
                .isCancellable(true)
                .OnPositiveClicked(object : GifDialog.GifDialogListener {
                    override fun onClick() {
                        if(bpHigh>120){
                            vitalParametersAlert = true
                        }
                        if(bpLow>80){
                            vitalParametersAlert = true
                        }
                        if(oxygenLevel<95){
                            vitalParametersAlert = true
                        }
                        if(temperature<98.0 || temperature>98.8){
                            vitalParametersAlert = true
                        }
                        if(pulse>100 || pulse<50){
                            vitalParametersAlert = true
                        }

                        if(vitalParametersAlert){
                            FirebaseFirestore.getInstance().collection("OngoingTreatments")
                                .document(intent.extras!!.getString("doc")!!)
                                .update("measurements.$currentDate.$slot",measurementData,
                                    "recordsLastUpdatedOn",FieldValue.serverTimestamp(),
                                    "vitalParametersAlert",true).addOnSuccessListener {
                                    toast("Records updated!")
                                    if(intent.extras!!.getString("user")!="professional"){
                                        getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE).commit {
                                            putString("${intent.extras!!.getString("doc")}currentSlot",slot)
                                            putString(getString(R.string.lastDate),currentDate)
                                        }
                                        intent = Intent(applicationContext, HomeActivity::class.java)
                                        startActivity(intent)
                                    }
                                    finish()
                                }
                        }
                        else {
                            FirebaseFirestore.getInstance().collection("OngoingTreatments")
                                .document(intent.extras!!.getString("doc")!!)
                                .update(
                                    "measurements.$currentDate.$slot", measurementData,
                                    "recordsLastUpdatedOn", FieldValue.serverTimestamp()
                                ).addOnSuccessListener {
                                    toast("Records updated!")
                                    if(intent.extras!!.getString("user")!="professional"){
                                        getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE).commit {
                                            putString("${intent.extras!!.getString("doc")}currentSlot",slot)
                                            putString(getString(R.string.lastDate),currentDate)
                                        }
                                        intent = Intent(applicationContext, HomeActivity::class.java)
                                        startActivity(intent)
                                    }
                                    finish()
                                }
                        }
                    }
                })
                .OnNegativeClicked(object : GifDialog.GifDialogListener { override fun onClick() {} })
                .build()
        }

        binding.titleIcon.setOnClickListener {
            onBackPressed()
        }
        binding.endIcon.setOnClickListener {
            MaterialDialog(this).show {
                title(null, "Records")
                icon(R.drawable.ic_first_aid_kit_30dp, null)
                message(null,"Use the equipments provided to you and record the details into the app and update.")
                debugMode(false)
                lifecycleOwner(this@MedicalRecordsActivity)
            }
        }
    }

    private fun currentSlot(){
        when (Calendar.getInstance()[Calendar.HOUR_OF_DAY]) {
            in 1..5, 0 -> {
               slot= "Late Night"
            }
            in 6..11 -> {
                slot = "Morning"
            }
            in 12..17 -> {
                slot = "Afternoon"
            }
            in 18..23 -> {
               slot = "Evening"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
    }

}