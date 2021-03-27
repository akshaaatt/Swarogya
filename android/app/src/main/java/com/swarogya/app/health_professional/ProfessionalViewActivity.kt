package com.swarogya.app.health_professional

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.swarogya.app.R
import com.swarogya.app.databinding.ProfessionalViewActivityBinding
import com.swarogya.app.home.MedicalRecordsActivity
import com.swarogya.app.register_patient.AddRequiredDocuments
import com.swarogya.app.utils.SelectedImage
import java.text.SimpleDateFormat
import java.util.*

class ProfessionalViewActivity : AppCompatActivity() {

    private var docId:String? = null
    private var medicinesList = ArrayList<String>()
    private var medicinesTextHere: String? = null
    private lateinit var binding:ProfessionalViewActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfessionalViewActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        docId = intent.extras!!.getString("docId")

        binding.bottomnavview.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        binding.bottomnavview.itemIconTintList = null

        binding.titleIcon.setOnClickListener {
            onBackPressed()
        }
        binding.endIcon.setOnClickListener {
            MaterialDialog(this).show {
                title(null,"Patient Details")
                icon(R.drawable.ic_help_30dp,null)
                message(null,"Click on the bottom options to either add documents or medical records of the patient, or add medicines to their treatment.")
                debugMode(false)
                lifecycleOwner(this@ProfessionalViewActivity)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)

        FirebaseFirestore.getInstance().collection("OngoingTreatments").document(docId!!)
            .get().addOnSuccessListener { treatmentDoc->
                medicinesList.clear()
                if(treatmentDoc.contains("medicines") && treatmentDoc.get("medicines")!=null) {
                    medicinesList = treatmentDoc.get("medicines") as ArrayList<String>
                    medicinesTextHere = ""
                    medicinesList.forEach { s ->
                        medicinesTextHere += s+"\n"
                    }
                    medicinesTextHere = medicinesTextHere!!.substring(0, medicinesTextHere!!.length - 1)
                }
                else{
                    medicinesTextHere = "No prescribed medicines"
                }

                Glide.with(applicationContext).asDrawable().load(treatmentDoc.get("facePic"))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                    .listener(object : RequestListener<Drawable>{
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            binding.progressBar.visibility = GONE
                            binding.faceRecognitionPic.visibility = VISIBLE
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            binding.progressBar.visibility = GONE
                            binding.faceRecognitionPic.visibility = VISIBLE
                            return false
                        }

                    })
                    .into(binding.faceRecognitionPic)

                when (treatmentDoc.get("gender")) {
                    "Male" ->  binding.genderIcon.setImageResource(R.drawable.ic_male_gender_24dp)
                    "Female" ->  binding.genderIcon.setImageResource(R.drawable.ic_female_gender_24dp)
                    "Other" ->  binding.genderIcon.setImageResource(R.drawable.ic_intersex_24dp)
                }
                binding.genderText.text = treatmentDoc.getString("gender")
                binding.titleText.text = treatmentDoc.getString("name")
                binding.emailIdText.text = treatmentDoc.getString("emailId")
                binding.bloodGroupText.text = treatmentDoc.getString("bloodGroup")
                binding.addressText.text = treatmentDoc.getString("address")
                binding.phoneNumberText.text = treatmentDoc.getString("phoneNumber")
                binding.firsEmergencyContactNumberText.text = treatmentDoc.getString("firstEmergencyContactNumber")
                binding.firsEmergencyContactNameText.text = treatmentDoc.getString("firstEmergencyContactName")

                binding.noTextBed.text = treatmentDoc.getString("bedNumber")
                binding.noTextFloor.text = treatmentDoc.getString("floorNumber")
                binding.noTextRoom.text = treatmentDoc.getString("roomNumber")
                binding.noTextWing.text = treatmentDoc.getString("wingNumber")

                if(treatmentDoc.getString("treatmentMode")=="Home Care") {
                    binding.generalDetailsHospitalized.visibility = GONE
                    binding.generalDetailsHome.visibility = VISIBLE
                }

                binding.faceRecognitionPic.setOnClickListener {
                    val intent = Intent(applicationContext, SelectedImage::class.java)
                    intent.putExtra("file",treatmentDoc.getString("facePic"))
                    startActivity(intent)
                }

                if(medicinesTextHere!=null){
                    binding.medicinesText.text = medicinesTextHere
                }
                else{
                    binding.medicinesText.text = getString(R.string.no_prescribed_medicines)
                }

                try {
                    val c = Calendar.getInstance()
                    c.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(treatmentDoc.getString("dob")!!)!!
                    val currentYear = GregorianCalendar.getInstance()[Calendar.YEAR]
                    val currentMonth = 1 + GregorianCalendar.getInstance()[Calendar.MONTH]
                    val todayDay = GregorianCalendar.getInstance()[Calendar.DAY_OF_MONTH]
                    var age = currentYear - c[Calendar.YEAR]
                    if ((1 + c[Calendar.MONTH] )> currentMonth) {
                        --age
                    } else if ((1 + c[Calendar.MONTH]) == currentMonth) {
                        if (c[Calendar.DAY_OF_MONTH] > todayDay) {
                            --age
                        }
                    }
                    binding.ageText.text = age.toString()
                }catch (ex:Exception){}

                binding.progressBar.visibility = GONE

            }
    }

    override fun onBackPressed() {
        MaterialDialog(this).show {
            title(null,"Go back?")
            message(null,"Just for confirmation.")
            positiveButton(R.string.yes){
                super.onBackPressed()
            }
            negativeButton(R.string.no)
            debugMode(false)
            lifecycleOwner(this@ProfessionalViewActivity)
        }
    }

    private var mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem: MenuItem ->
        when (menuItem.itemId) {
            R.id.bottom_add_docs -> {
                val intent = Intent(this, AddRequiredDocuments::class.java)
                intent.putExtra("doc",docId)
                intent.putExtra("user","professional")
                startActivity(intent)
            }
            R.id.bottom_add_medical_records -> {
                val intent = Intent(applicationContext,MedicalRecordsActivity::class.java)
                intent.putExtra("doc",docId)
                intent.putExtra("user","professional")
                startActivity(intent)
            }
            R.id.bottom_update_medicines -> {
                val intent = Intent(applicationContext, ListActivity::class.java)
                if (medicinesList.isNullOrEmpty()) {
                    medicinesList = ArrayList()
                }
                intent.putStringArrayListExtra("list", medicinesList)
                intent.putExtra("doc",docId)
                intent.putExtra("user","professional")
                startActivity(intent)
            }
        }
        false
    }
}