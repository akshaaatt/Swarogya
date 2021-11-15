package com.aemerse.svarogya.home

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.aemerse.svarogya.R
import com.aemerse.svarogya.databinding.HomeActivityBinding
import com.aemerse.svarogya.notifications.AlertReceiver
import com.aemerse.svarogya.profile.ProfileActivity
import com.aemerse.svarogya.register_patient.AddRequiredDocuments
import com.aemerse.svarogya.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.imperiumlabs.geofirestore.GeoFirestore
import org.json.JSONException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeActivity : AppCompatActivity() {

    private lateinit var totalTests: String
    private lateinit var oldTests: String
    private var testsInt = 0
    private lateinit var totalTestsCopy: String
    private lateinit var sharedPreferences: SharedPreferences

    private var updatesDisplay = StringBuilder()
    private var currentIndex:Int? = 0
    private lateinit var binding:HomeActivityBinding

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        binding = HomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        fetchData()

        if(!askForPermissions(this, permissionsToGive, permissionsRequest)) {
            LocationServices.getFusedLocationProviderClient(applicationContext).lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        GeoFirestore(FirebaseFirestore.getInstance().collection("Users"))
                            .setLocation(FirebaseAuth.getInstance().currentUser!!.uid, GeoPoint(location.latitude, location.longitude))
                    }
                }
        }

        binding.bottomnavview.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        binding.bottomnavview.itemIconTintList = null

        binding.faqButton.setOnClickListener {
            MaterialDialog(this).show {
                title(null,"Fight Against Corona")
                icon(R.drawable.ic_virus_30dp,null)
                message(R.string.faq_data)
                debugMode(false)
                lifecycleOwner(this@HomeActivity)
            }
        }
        binding.registerTo.setOnClickListener {
            val intent = Intent(this, AddRequiredDocuments::class.java)
            intent.putExtra("user","regular")
            startActivity(intent)
        }

        binding.titleText.setOnClickListener {

        }
        binding.imageView.setOnClickListener {

        }
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        logoAnimate(binding.imageView)

        FirebaseFirestore.getInstance().collection("OngoingTreatments")
            .whereEqualTo("phoneNumber",FirebaseAuth.getInstance().currentUser!!.phoneNumber).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if(task.result.size()==0){
                        binding.progressBar.visibility = GONE
                        binding.startRel.visibility = VISIBLE
                        val alarm = PendingIntent.getBroadcast(this, 0,Intent(this, AlertReceiver::class.java) , PendingIntent.FLAG_NO_CREATE)
                        if (alarm!=null) {
                            cancelAlarms()
                        }
                        binding.chooseHospitalButton.setOnClickListener {
                            MaterialDialog(this).show {
                                title(null, "Multiple Treatments")
                                icon(R.drawable.ic_hospital_24dp, null)
                                message(R.string.multiple_treatments_message)
                                debugMode(false)
                                lifecycleOwner(this@HomeActivity)
                            }
                        }
                    }
                    else{
                        binding.lastUpdatedAt.visibility = VISIBLE
                        binding.gridView.visibility = VISIBLE
                        binding.progressBar.visibility = GONE

                        val patientIds = ArrayList<String>()
                        task.result.documents.forEachIndexed { index, documentSnapshot ->
                            patientIds.add(documentSnapshot.getString("patientId")!!)
                            if(sharedPreferences.getString(getString(R.string.currentPatientId),null)==documentSnapshot.getString("patientId")){
                                currentIndex = index
                            }
                        }
                        if(currentIndex == 0){
                            sharedPreferences.commit {
                                putString(getString(R.string.currentPatientId), task.result.documents[0].getString("patientId"))
                            }
                        }
                        if(task.result.documents[currentIndex!!].contains("recordsLastUpdatedOn")) {
                            val timestamp = task.result.documents[currentIndex!!].getTimestamp("recordsLastUpdatedOn")
                            val showDate = SimpleDateFormat("MMMM d, hh:mm a", Locale.getDefault()).format(Date(timestamp!!.seconds * 1000L))
                            binding.lastUpdatedAt.text = getString(R.string.recordsLastUpdate, showDate.toString())
                        }

                        binding.assistanceButton.setOnClickListener {
                            MaterialDialog(this).show {
                                title(null,"Ask for Assistance")
                                icon(R.drawable.ic_nurse_24dp,null)
                                positiveButton(R.string.send_alert){
                                    FirebaseFirestore.getInstance().collection("OngoingTreatments")
                                        .document(task.result.documents[currentIndex!!].id).update("needAssistanceAlert",true).addOnSuccessListener {
                                            toast("Assistance alert sent!")
                                        }
                                        .addOnFailureListener {
                                            toast("Could not process. Try again!")
                                        }
                                }
                                negativeButton(R.string.cancel)
                                @Suppress("DEPRECATION")
                                neutralButton(R.string.emergency){
                                    FirebaseFirestore.getInstance().collection("OngoingTreatments")
                                        .document(task.result.documents[currentIndex!!].id).update("emergencyAlert",true).addOnSuccessListener {
                                            toast("Emergency alert sent!")
                                        }
                                        .addOnFailureListener {
                                            toast("Could not process. Try again!")
                                        }
                                }
                                this.getActionButton(WhichButton.NEUTRAL).updateTextColor(ContextCompat.getColor(this@HomeActivity,R.color.red))
                                message(null,"Proceeding with this will send a notification to your common hall and let them know that you need special medical attention.")
                                debugMode(false)
                                lifecycleOwner(this@HomeActivity)
                            }
                        }
                        binding.medicalRecordsButton.setOnClickListener {
                            intent = Intent(applicationContext,MedicalRecordsActivity::class.java)
                            intent.putExtra("doc", task.result.documents[currentIndex!!].id)
                            startActivity(intent)
                        }

                        val bedNumber = task.result.documents[currentIndex!!].getString("bedNumber")
                        val roomNumber = task.result.documents[currentIndex!!].getString("roomNumber")
                        val wingNumber = task.result.documents[currentIndex!!].getString("wingNumber")
                        val floorNumber = task.result.documents[currentIndex!!].getString("floorNumber")
                        val treatmentMode = task.result.documents[currentIndex!!].getString("treatmentMode")
                        val hospitalName = task.result.documents[currentIndex!!].getString("hospitalName")

                        binding.generalDetailsButton.setOnClickListener {
                            if(treatmentMode!="Home Care") {
                                MaterialDialog(this).show {
                                    title(null, "$hospitalName")
                                    icon(R.drawable.ic_bed_24dp, null)
                                    message(
                                        null,
                                        "Bed Number: $bedNumber\n\nRoom Number: $roomNumber\n\nWing Number: $wingNumber\n\nFloor Number: $floorNumber"
                                    )
                                    debugMode(false)
                                    lifecycleOwner(this@HomeActivity)
                                }
                            }
                            else{
                                MaterialDialog(this).show {
                                    title(null, "$hospitalName")
                                    icon(R.drawable.ic_bed_24dp)
                                    message(R.string.homecare)
                                    debugMode(false)
                                    lifecycleOwner(this@HomeActivity)
                                }
                            }
                        }
                        binding.medicinesButton.setOnClickListener {
                            var message = ""
                            if(task.result.documents[currentIndex!!].contains("medicines")) {
                                val medicinesList = task.result.documents[currentIndex!!].get("medicines") as ArrayList<String>
                                medicinesList.forEach { s ->
                                    message += s+"\n"
                                }
                                message = message.substring(0, message.length - 1)
                            }
                            else{
                                message = "No prescribed medicines"
                            }

                            MaterialDialog(this).show {
                                title(null, "Medicines")
                                icon(R.drawable.ic_drug_24dp, null)
                                message(null, message)
                                debugMode(false)
                                lifecycleOwner(this@HomeActivity)
                            }
                        }

                        if(patientIds.size>1) {
                            binding.chooseHospitalButton.setOnClickListener {
                                MaterialDialog(this).show {
                                    title(null, "Select Id")
                                    listItemsSingleChoice(null, patientIds, initialSelection = currentIndex!!) { _, index, _ ->
                                        if(index!=currentIndex) {
                                            sharedPreferences.commit {
                                                putString(
                                                    getString(R.string.currentPatientId),
                                                    patientIds[index]
                                                )
                                            }
                                            recreate()
                                        }
                                    }
                                    positiveButton(R.string.select)
                                    negativeButton(R.string.cancel)
                                    debugMode(false)
                                    lifecycleOwner(this@HomeActivity)
                                }
                            }
                        }
                        else{
                            binding.chooseHospitalButton.setOnClickListener {
                                MaterialDialog(this).show {
                                    title(null, "Multiple Treatments")
                                    icon(R.drawable.ic_hospital_24dp, null)
                                    message(R.string.multiple_treatments_message)
                                    debugMode(false)
                                    lifecycleOwner(this@HomeActivity)
                                }
                            }
                        }
                        val alarm = PendingIntent.getBroadcast(this, 0,Intent(this, AlertReceiver::class.java) , PendingIntent.FLAG_NO_CREATE)
                        if (alarm==null) {
                            startAlarms()
                        }
                    }
                }
            }
    }

    private fun fetchData() {
        val requestQueue = Volley.newRequestQueue(this)
        val apiUrl = "https://api.covid19india.org/data.json"
        //Fetching the API from URL
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, apiUrl, null, { response ->
            lifecycleScope.launch(Dispatchers.Main) {
                try {
                    //Since the objects of JSON are in an Array we need to define the array from which we can fetch objects
                    val jsonArray = response.getJSONArray("statewise")
                    val statewise = jsonArray.getJSONObject(0)
                    //Inserting the fetched data into variables
                    var confirmed = statewise.getString("confirmed")
                    var active = statewise.getString("active")
                    val date = statewise.getString("lastupdatedtime")
                    var recovered = statewise.getString("recovered")
                    var deaths = statewise.getString("deaths")
                    var newConfirmed = statewise.getString("deltaconfirmed")
                    var newDeaths = statewise.getString("deltadeaths")
                    var newRecovered = statewise.getString("deltarecovered")
                    if (date.isNotEmpty()) {

                        val confirmedInt: Int = confirmed.toInt()
                        confirmed = NumberFormat.getInstance()
                            .format(confirmedInt.toLong())

                        updatesDisplay.append(" Total Confirmed Cases at $confirmed ;")

                        val newConfirmedInt: Int = newConfirmed.toInt()
                        newConfirmed = NumberFormat.getInstance()
                            .format(newConfirmedInt.toLong())

                        updatesDisplay.append(" New Confirmed Cases at $newConfirmed ;")

                        val activeInt: Int = active.toInt()
                        active = NumberFormat.getInstance().format(activeInt.toLong())

                        updatesDisplay.append(" Total Active Cases at $active ;")

                        val recoveredInt: Int = recovered.toInt()
                        recovered = NumberFormat.getInstance().format(recoveredInt.toLong())

                        updatesDisplay.append(" Total Recovered Cases at $recovered ;")

                        val recoveredNewInt: Int = newRecovered.toInt()
                        newRecovered = NumberFormat.getInstance()
                            .format(recoveredNewInt.toLong())

                        updatesDisplay.append(" New Recovered Cases at $newRecovered ;")

                        val deathsInt: Int = deaths.toInt()
                        deaths = NumberFormat.getInstance()
                            .format(deathsInt.toLong())

                        updatesDisplay.append(" Total Deaths at $deaths ;")

                        val deathsNewInt: Int = newDeaths.toInt()
                        newDeaths = NumberFormat.getInstance()
                            .format(deathsNewInt.toLong())

                        updatesDisplay.append(" New Deaths at $newDeaths ;")

                        val jsonArrayTests = response.getJSONArray("tested")
                        for (i in 0 until jsonArrayTests.length()) {
                            totalTests = jsonArrayTests.getJSONObject(i).getString("totalsamplestested")
                        }
                        for (i in 0 until jsonArrayTests.length() - 1) {
                            oldTests = jsonArrayTests.getJSONObject(i).getString("totalsamplestested")
                        }
                        if (totalTests.isEmpty()) {
                            for (i in 0 until jsonArrayTests.length() - 1) {
                                totalTests = jsonArrayTests.getJSONObject(i).getString("totalsamplestested")
                            }
                            totalTestsCopy = totalTests
                            testsInt = totalTests.toInt()
                            totalTests = NumberFormat.getInstance().format(testsInt.toLong())

                            updatesDisplay.append(" Total Tests Done at $totalTests ;")

                            for (i in 0 until jsonArrayTests.length() - 2) {
                                oldTests = jsonArrayTests.getJSONObject(i).getString("totalsamplestested")
                            }
                            val testsNew: Int = totalTestsCopy.toInt() - oldTests.toInt()

                            updatesDisplay.append(
                                " New Tests Done at " + NumberFormat.getInstance()
                                    .format(testsNew.toLong())+" ;"
                            )

                        } else {
                            totalTestsCopy = totalTests
                            testsInt = totalTests.toInt()
                            totalTests = NumberFormat.getInstance().format(testsInt.toLong())

                            updatesDisplay.append(" Total Tests Done at $totalTests ;")
                            if (oldTests.isEmpty()) {
                                for (i in 0 until jsonArrayTests.length() - 2) {
                                    oldTests = jsonArrayTests.getJSONObject(i).getString("totalsamplestested")
                                }
                            }
                            val testsNew: Long = totalTestsCopy.toInt() - oldTests.toInt().toLong()
                            updatesDisplay.append(
                                " New Tests Done at " + NumberFormat.getInstance().format(testsNew)+" ;"
                            )

                        }
                        updatesDisplay.append(" Updated on $date")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }.invokeOnCompletion {
                binding.updatesTextView.text = updatesDisplay
                binding.updatesTextView.isSelected = true

                binding.updatesTextView.setOnClickListener {
                    MaterialDialog(this@HomeActivity).show {
                        title(null, " Covid-19 UPDATES")
                        message(null, updatesDisplay.toString().replace(';', '\n'))
                        debugMode(false)
                        lifecycleOwner(this@HomeActivity)
                    }
                }
            }
        },
            { error -> error.printStackTrace() })
        requestQueue.add(jsonObjectRequest)
    }

    private fun startAlarms() {
        val cal0 = Calendar.getInstance()
        cal0[Calendar.HOUR_OF_DAY]=24
        cal0[Calendar.MINUTE]=0
        cal0[Calendar.SECOND]=0

        val cal1 = Calendar.getInstance()
        cal1[Calendar.HOUR_OF_DAY]=6
        cal1[Calendar.MINUTE]=0
        cal1[Calendar.SECOND]=0

        val cal2 = Calendar.getInstance()
        cal2[Calendar.HOUR_OF_DAY]=12
        cal2[Calendar.MINUTE]=0
        cal2[Calendar.SECOND]=0

        val cal3 = Calendar.getInstance()
        cal3[Calendar.HOUR_OF_DAY]=18
        cal3[Calendar.MINUTE]=0
        cal3[Calendar.SECOND]=0

        if (cal0.before(Calendar.getInstance())) {
            cal0.add(Calendar.DATE, 1)
        }
        if (cal1.before(Calendar.getInstance())) {
            cal1.add(Calendar.DATE, 1)
        }
        if (cal2.before(Calendar.getInstance())) {
            cal2.add(Calendar.DATE, 1)
        }
        if (cal3.before(Calendar.getInstance())) {
            cal3.add(Calendar.DATE, 1)
        }

        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, AlertReceiver::class.java)

        intent.putExtra("requestCode",0)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal0.timeInMillis, PendingIntent.getBroadcast(this, 0,intent , PendingIntent.FLAG_UPDATE_CURRENT))
        intent.putExtra("requestCode",1)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal1.timeInMillis, PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT))
        intent.putExtra("requestCode",2)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal2.timeInMillis, PendingIntent.getBroadcast(this, 2,intent, PendingIntent.FLAG_UPDATE_CURRENT))
        intent.putExtra("requestCode",3)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal3.timeInMillis, PendingIntent.getBroadcast(this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT))
    }
    private fun cancelAlarms() {
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertReceiver::class.java)

        alarmManager.cancel(PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
        alarmManager.cancel(PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT))
        alarmManager.cancel(PendingIntent.getBroadcast(this, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT))
        alarmManager.cancel(PendingIntent.getBroadcast(this, 3, intent, PendingIntent.FLAG_CANCEL_CURRENT))

        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT).cancel()
        PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT).cancel()
        PendingIntent.getBroadcast(this, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT).cancel()
        PendingIntent.getBroadcast(this, 3, intent, PendingIntent.FLAG_CANCEL_CURRENT).cancel()
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(!askForPermissions(this, permissionsToGive, permissionsRequest)) {
            LocationServices.getFusedLocationProviderClient(applicationContext).lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        GeoFirestore(FirebaseFirestore.getInstance().collection("Users"))
                            .setLocation(FirebaseAuth.getInstance().currentUser!!.uid, GeoPoint(location.latitude, location.longitude))
                    }
                }
        }
    }

    private var mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem: MenuItem ->
        when (menuItem.itemId) {
            R.id.bottom_scan -> {
                val intent = Intent(this, AddRequiredDocuments::class.java)
                intent.putExtra("user","regular")
                startActivity(intent)
            }
            R.id.bottom_profile -> {
                intent = Intent(applicationContext, ProfileActivity::class.java)
                startActivity(intent)
            }
        }
        false
    }
}