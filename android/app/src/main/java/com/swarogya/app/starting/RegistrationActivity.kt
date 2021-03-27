package com.swarogya.app.starting

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BasicGridItem
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.gridItems
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.aminography.primecalendar.civil.CivilCalendar
import com.aminography.primedatepicker.picker.PrimeDatePicker
import com.aminography.primedatepicker.picker.theme.DarkThemeFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.common.base.Strings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.swarogya.app.R
import com.swarogya.app.databinding.RegistrationActivityBinding
import com.swarogya.app.home.HomeActivity
import com.swarogya.app.models.User
import com.swarogya.app.utils.*
import com.swarogya.app.utils.GifDialog.GifDialogListener
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RegistrationActivity : AppCompatActivity() {

    private var dob: String? = null
    private var gender: String? = null
    private var bloodGroup: String? = null
    private var faceFile: File? = null
    private val imageCode = 99
    private lateinit var binding: RegistrationActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegistrationActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.faceRecognitionPic.setOnClickListener {
            if(!askForPermissions(this,permissionsToGive, permissionsRequest)){
                intent = Intent(applicationContext, SelfieCameraActivity::class.java)
                startActivityForResult(intent, imageCode)
            }
        }

        binding.materialNameTextView.expand()
        binding.materialNameTextView.clearFocus()
        binding.materialNameTextView.setOnClickListener { }

        binding.materialAddressTextView.expand()
        binding.materialAddressTextView.clearFocus()
        binding.materialAddressTextView.setOnClickListener { }

        binding.materialEmailTextView.expand()
        binding.materialEmailTextView.clearFocus()
        binding.materialEmailTextView.setOnClickListener { }

        binding.materialFirstEmergencyContactNumberTextView.expand()
        binding.materialFirstEmergencyContactNumberTextView.clearFocus()
        binding.materialFirstEmergencyContactNumberTextView.setOnClickListener { }

        binding.materialFirstEmergencyContactNameTextView.expand()
        binding.materialFirstEmergencyContactNameTextView.clearFocus()
        binding.materialFirstEmergencyContactNameTextView.setOnClickListener { }

        binding.materialSecondEmergencyContactNumberTextView.expand()
        binding.materialSecondEmergencyContactNumberTextView.clearFocus()
        binding.materialSecondEmergencyContactNumberTextView.setOnClickListener { }

        binding.materialSecondEmergencyContactNameTextView.expand()
        binding.materialSecondEmergencyContactNameTextView.clearFocus()
        binding.materialSecondEmergencyContactNameTextView.setOnClickListener { }

        binding.dobText.setOnClickListener {
            val calendar = CivilCalendar()
            calendar.set(2000,7,19)

            val datePicker = PrimeDatePicker.bottomSheetWith(calendar)
                .pickSingleDay {
                    calendar.set(it.year, it.month, it.dayOfMonth)
                    dob = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.timeInMillis)
                    updateUI(dob)
                }
                .applyTheme(DarkThemeFactory())
                .build()
            datePicker.show(supportFragmentManager, "datePicker")
        }

        binding.genderGrid.setOnClickListener {
            val items = listOf(
                BasicGridItem(R.drawable.ic_male_gender_24dp, "Male"),
                BasicGridItem(R.drawable.ic_female_gender_24dp, "Female"),
                BasicGridItem(R.drawable.ic_intersex_24dp, "Other")
            )

            MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                gridItems(items) { _, _, item ->
                    gender = item.title
                    updateUI(gender)
                }
                positiveButton(R.string.select)
                negativeButton(R.string.cancel)
                debugMode(false)
                lifecycleOwner(this@RegistrationActivity)
            }
        }

        binding.bloodGroupList.setOnClickListener {
            MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                listItems(R.array.Blood_Group) { _, _, text ->
                    bloodGroup = text.toString()
                    updateUI(bloodGroup)
                }
                positiveButton(R.string.select)
                negativeButton(R.string.cancel)
                debugMode(false)
                lifecycleOwner(this@RegistrationActivity)
            }
        }

        binding.scrollView.smoothScrollTo(0, 0)

        binding.selfieText.animation = AnimationUtils.loadAnimation(this,R.anim.pulse_fade_in)
        binding.genderGrid.animation = AnimationUtils.loadAnimation(this,R.anim.pulse_fade_in)
        binding.bloodGroupList.animation = AnimationUtils.loadAnimation(this,R.anim.pulse_fade_in)
        binding.dobText.animation = AnimationUtils.loadAnimation(this,R.anim.pulse_fade_in)

        binding.proceedText.setOnClickListener {
            if (faceFile == null) {
                toast("Add a selfie!")
                return@setOnClickListener
            }
            if (Strings.isNullOrEmpty(binding.nameText.text.toString().trim())) {
                binding.nameText.error = "Enter your full name"
                binding.nameText.requestFocus()
                return@setOnClickListener
            }
            if (Strings.isNullOrEmpty(binding.emailId.text.toString().trim())) {
                binding.emailId.error = "Enter your email id"
                binding.emailId.requestFocus()
                return@setOnClickListener
            }
            if(!binding.emailId.text.toString().trim().contains("@")){
                binding.emailId.error = "Enter a valid email id"
                binding.emailId.requestFocus()
                return@setOnClickListener
            }
            if (Strings.isNullOrEmpty(binding.firstEmergencyContactNumberText.text.toString().trim()) ||
                binding.firstEmergencyContactNumberText.text.toString().trim ().length < 10) {

                binding.firstEmergencyContactNumberText.error = "Enter valid contact"
                binding.firstEmergencyContactNumberText.requestFocus()
                return@setOnClickListener
            }
            if (Strings.isNullOrEmpty(binding.firstEmergencyContactNameText.text.toString().trim())) {
                binding.firstEmergencyContactNameText.error = "Enter first emergency contact name"
                binding.firstEmergencyContactNameText.requestFocus()
                return@setOnClickListener
            }
            if (Strings.isNullOrEmpty(binding.secondEmergencyContactNumberText.text.toString().trim()) ||
                binding.secondEmergencyContactNumberText.text.toString().trim ().length < 10) {

                binding.secondEmergencyContactNumberText.error = "Enter valid contact"
                binding.secondEmergencyContactNumberText.requestFocus()
                return@setOnClickListener
            }
            if (Strings.isNullOrEmpty(binding.secondEmergencyContactNameText.text.toString().trim())) {
                binding.secondEmergencyContactNameText.error = "Enter second emergency contact name"
                binding.secondEmergencyContactNameText.requestFocus()
                return@setOnClickListener
            }
            if (Strings.isNullOrEmpty(gender) || (gender == "Gender")) {
                toast("Select your gender")
                return@setOnClickListener
            }
            if (Strings.isNullOrEmpty(bloodGroup) || (gender == "bloodGroup")) {
                toast("Select your blood group")
                return@setOnClickListener
            }
            if (Strings.isNullOrEmpty(dob) || (dob == "Date of birth")) {
                toast("Select your date of birth")
                return@setOnClickListener
            }

            GifDialog.Builder(this@RegistrationActivity)
                .setTitle("Recheck")
                .setMessage("Sure about the details you just entered?")
                .setPositiveBtnText("Yes")
                .setPositiveBtnBackground("#22b573")
                .setNegativeBtnText("No")
                .setNegativeBtnBackground("#c1272d")
                .setGifResource(R.raw.progress)
                .isCancellable(true)
                .OnPositiveClicked(object : GifDialogListener {
                    override fun onClick() {
                        toast("Please wait...")
                        binding.progressBar.visibility = View.VISIBLE

                        lifecycleScope.launch(Dispatchers.Default) {
                            FirebaseStorage.getInstance().reference.child("Users").child(Firebase.auth.currentUser!!.uid).child("FacePic").child("Pic1")
                                .putFile(Uri.fromFile(Compressor.compress(applicationContext, faceFile!!)))
                                .addOnSuccessListener { taskSnapshot1->
                                    taskSnapshot1.metadata!!.reference!!.downloadUrl.addOnSuccessListener { faceUri: Uri ->
                                        val user = User(
                                            binding.nameText.text.toString().trim()
                                            , binding.addressText.text.toString().trim()
                                            , binding.emailId.text.toString().trim()
                                            , FirebaseAuth.getInstance().currentUser!!.phoneNumber
                                            , dob
                                            , gender
                                            , null
                                            , null
                                            , binding.firstEmergencyContactNumberText.text.toString().trim()
                                            , binding.firstEmergencyContactNameText.text.toString().trim()
                                            , binding.secondEmergencyContactNumberText.text.toString().trim()
                                            , binding.secondEmergencyContactNameText.text.toString().trim()
                                            , FirebaseAuth.getInstance().currentUser!!.uid
                                            , bloodGroup,
                                            faceUri.toString()
                                        )

                                        FirebaseFirestore.getInstance().collection("Users")
                                            .document(FirebaseAuth.getInstance().currentUser!!.uid).set(user)
                                            .addOnSuccessListener {
                                                intent = Intent(applicationContext, HomeActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                            .addOnFailureListener {
                                                toast(it.message.toString())
                                            }
                                    }
                                }
                                .addOnFailureListener { exception: Exception ->
                                    binding.progressBar.visibility = View.GONE
                                    Toast.makeText(applicationContext, exception.message, Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                })
                .OnNegativeClicked(object : GifDialogListener{ override fun onClick() {} })
                .build()
        }
        binding.titleIcon.setOnClickListener{
            MaterialDialog(this).show {
                title(null,"General Information")
                message(null,"These details that you provide will work as your official contact details for the hospital that is linked to you. So please make sure that these are updated.")
                debugMode(false)
                lifecycleOwner(this@RegistrationActivity)
            }
        }
        binding.endIcon.setOnClickListener {
            MaterialDialog(this).show {
                title(null,"Secure Portal")
                message(null,"These details that you provide is safe and secure with us. You can rest assured.")
                debugMode(false)
                lifecycleOwner(this@RegistrationActivity)
            }
        }
    }

    private fun updateUI(text: String?) {
        when (text) {
            bloodGroup -> {
                binding.bloodGroupList.text = bloodGroup
            }
            dob -> {
                binding.dobText.text = dob
            }
            gender -> {
                binding.genderGrid.text = gender
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imageCode && resultCode == Activity.RESULT_OK && data != null) {
            faceFile = data.extras!!.get("file") as File
            Glide.with(this).load(faceFile)
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20))).into(binding.faceRecognitionPic)
        }
        else {
            if(faceFile == null) {
                toast("Please click a selfie!")
            }
        }
    }

    override fun onBackPressed() {
        GifDialog.Builder(this@RegistrationActivity)
            .setTitle("Quit Registration")
            .setMessage("Are you sure?")
            .setPositiveBtnText("Yes")
            .setPositiveBtnBackground("#22b573")
            .setNegativeBtnText("No")
            .setNegativeBtnBackground("#c1272d")
            .setGifResource(R.raw.cycling2)
            .isCancellable(true)
            .OnPositiveClicked(object : GifDialogListener {
                override fun onClick() {
                    finish()
                }
            })
            .OnNegativeClicked(object : GifDialogListener{ override fun onClick() {} })
            .build()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(!askForPermissions(this,permissionsToGive, permissionsRequest)){
            intent = Intent(applicationContext, SelfieCameraActivity::class.java)
            startActivityForResult(intent, imageCode)
        }
    }
}