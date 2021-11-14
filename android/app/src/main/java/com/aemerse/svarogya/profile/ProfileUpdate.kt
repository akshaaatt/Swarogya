package com.aemerse.svarogya.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.aemerse.svarogya.R
import com.aemerse.svarogya.databinding.RegistrationActivityBinding
import com.aemerse.svarogya.home.HomeActivity
import com.aemerse.svarogya.models.User
import com.aemerse.svarogya.starting.SelfieCameraActivity
import com.aemerse.svarogya.starting.Splash
import com.aemerse.svarogya.utils.GifDialog
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ProfileUpdate : AppCompatActivity() {

    private lateinit var user: User
    private val imageCode = 99
    private var dob: String? = null
    private var gender: String? = null
    private var bloodGroup: String? = null
    private var faceFile: File? = null
    private lateinit var userHere: DocumentReference
    private lateinit var binding: RegistrationActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        binding = RegistrationActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.endIcon.setImageResource(R.drawable.ic_logout_30dp)

        user = intent.extras!!.get("user") as User

        userHere = FirebaseFirestore.getInstance().collection("Users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)

        Glide.with(this).load(user.facePic)
            .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20))).into(binding.faceRecognitionPic)

        binding.faceRecognitionPic.setOnClickListener {
            intent = Intent(applicationContext, SelfieCameraActivity::class.java)
            startActivityForResult(intent, imageCode)
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

        binding.titleText.text = getString(R.string.profile)
        binding.proceedText.text = getString(R.string.update)
        binding.genderGrid.text = user.gender
        binding.bloodGroupList.text = user.bloodGroup
        binding.nameText.setText(user.name)
        binding.addressText.setText(user.address)
        binding.emailId.setText(user.emailId)
        binding.firstEmergencyContactNumberText.setText(user.firstEmergencyContactNumber)
        binding.firstEmergencyContactNameText.setText(user.firstEmergencyContactName)
        binding.secondEmergencyContactNumberText.setText(user.secondEmergencyContactNumber)
        binding.secondEmergencyContactNameText.setText(user.secondEmergencyContactName)

        try {
            binding.dobText.text = SimpleDateFormat("MMM d, yyyy",Locale.getDefault()).format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(user.dob!!)!!)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

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
                lifecycleOwner(this@ProfileUpdate)
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
                lifecycleOwner(this@ProfileUpdate)
            }
        }

        binding.scrollView.smoothScrollTo(0, 0)

        binding.selfieText.animation = AnimationUtils.loadAnimation(this,R.anim.pulse_fade_in)
        binding.genderGrid.animation = AnimationUtils.loadAnimation(this,R.anim.pulse_fade_in)
        binding.bloodGroupList.animation = AnimationUtils.loadAnimation(this,R.anim.pulse_fade_in)
        binding.dobText.animation = AnimationUtils.loadAnimation(this,R.anim.pulse_fade_in)

        binding.proceedText.setOnClickListener {
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
            GifDialog.Builder(this@ProfileUpdate)
                .setTitle("Update")
                .setMessage("Sure about the details you just entered?")
                .setPositiveBtnText("Yes")
                .setPositiveBtnBackground("#22b573")
                .setNegativeBtnText("No")
                .setNegativeBtnBackground("#c1272d")
                .setGifResource(R.raw.progress)
                .isCancellable(true)
                .OnPositiveClicked(object : GifDialog.GifDialogListener {
                    override fun onClick() {
                        if(faceFile!=null){
                            GlobalScope.launch(Dispatchers.Default) {
                                FirebaseStorage.getInstance().reference.child("Users").child(Firebase.auth.currentUser!!.uid).child("FacePic").child("Pic1")
                                    .putFile(Uri.fromFile(Compressor.compress(applicationContext, faceFile!!)))
                                    .addOnSuccessListener { taskSnapshot1->
                                        taskSnapshot1.metadata!!.reference!!.downloadUrl.addOnSuccessListener { faceUri: Uri ->
                                            userHere.update("facePic",faceUri.toString())
                                                .addOnCompleteListener {
                                                    if (gender != user.gender && !Strings.isNullOrEmpty(gender)) {
                                                        userHere.update("gender", gender)
                                                    }
                                                    if (bloodGroup != user.bloodGroup && !Strings.isNullOrEmpty(bloodGroup)) {
                                                        userHere.update("bloodGroup", bloodGroup)
                                                    }
                                                    if (dob != user.dob && !Strings.isNullOrEmpty(dob)) {
                                                        userHere.update("dob", dob)
                                                    }
                                                    if(binding.nameText.text.toString().trim()!=user.name){
                                                        userHere.update("name", binding.nameText.text.toString().trim())
                                                    }
                                                    if(binding.emailId.text.toString().trim()!=user.emailId){
                                                        userHere.update("emailId", binding.emailId.text.toString().trim())
                                                    }
                                                    if(binding.addressText.text.toString().trim()!=user.address){
                                                        userHere.update("address", binding.addressText.text.toString().trim())
                                                    }
                                                    if(binding.firstEmergencyContactNumberText.text.toString().trim()!=user.firstEmergencyContactNumber){
                                                        userHere.update("firstEmergencyContactNumber", binding.firstEmergencyContactNumberText.text.toString().trim())
                                                    }
                                                    if(binding.firstEmergencyContactNameText.text.toString().trim()!=user.firstEmergencyContactName){
                                                        userHere.update("firstEmergencyContactName", binding.firstEmergencyContactNameText.text.toString().trim())
                                                    }
                                                    if(binding.secondEmergencyContactNumberText.text.toString().trim()!=user.secondEmergencyContactNumber){
                                                        userHere.update("secondEmergencyContactNumber", binding.secondEmergencyContactNumberText.text.toString().trim())
                                                    }
                                                    if(binding.secondEmergencyContactNameText.text.toString().trim()!=user.secondEmergencyContactName){
                                                        userHere.update("secondEmergencyContactName", binding.secondEmergencyContactNameText.text.toString().trim())
                                                    }
                                                    userHere.update("timestamp",FieldValue.serverTimestamp())

                                                }
                                        }
                                    }
                            }
                        }
                        else {
                            GlobalScope.launch(Dispatchers.Default) {
                                if (gender != user.gender && !Strings.isNullOrEmpty(gender)) {
                                    userHere.update("gender", gender)
                                }
                                if (bloodGroup != user.bloodGroup && !Strings.isNullOrEmpty(bloodGroup)) {
                                    userHere.update("bloodGroup", bloodGroup)
                                }
                                if (dob != user.dob && !Strings.isNullOrEmpty(dob)) {
                                    userHere.update("dob", dob)
                                }
                                if (binding.nameText.text.toString().trim() != user.name) {
                                    userHere.update("name", binding.nameText.text.toString().trim())
                                }
                                if (binding.emailId.text.toString().trim() != user.emailId) {
                                    userHere.update("emailId",binding.emailId.text.toString().trim())
                                }
                                if (binding.addressText.text.toString().trim() != user.address) {
                                    userHere.update("address", binding.addressText.text.toString().trim())
                                }
                                if(binding.firstEmergencyContactNumberText.text.toString().trim()!=user.firstEmergencyContactNumber){
                                    userHere.update("firstEmergencyContactNumber", binding.firstEmergencyContactNumberText.text.toString().trim())
                                }
                                if(binding.firstEmergencyContactNameText.text.toString().trim()!=user.firstEmergencyContactName){
                                    userHere.update("firstEmergencyContactName", binding.firstEmergencyContactNameText.text.toString().trim())
                                }
                                if(binding.secondEmergencyContactNumberText.text.toString().trim()!=user.secondEmergencyContactNumber){
                                    userHere.update("secondEmergencyContactNumber", binding.secondEmergencyContactNumberText.text.toString().trim())
                                }
                                if(binding.secondEmergencyContactNameText.text.toString().trim()!=user.secondEmergencyContactName){
                                    userHere.update("secondEmergencyContactName", binding.secondEmergencyContactNameText.text.toString().trim())
                                }
                                userHere.update("timestamp", FieldValue.serverTimestamp())
                            }
                        }
                        GifDialog.Builder(this@ProfileUpdate)
                            .setTitle("Making changes")
                            .setMessage("We'll make the desired changes")
                            .setPositiveBtnText("Ok")
                            .setPositiveBtnBackground("#22b573")
                            .setGifResource(R.raw.submit2)
                            .isCancellable(true)
                            .OnPositiveClicked(object : GifDialog.GifDialogListener{
                                override fun onClick() {
                                    val intent = Intent(applicationContext, HomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            })
                            .build()
                    }
                })
                .OnNegativeClicked(object : GifDialog.GifDialogListener { override fun onClick() {} })
                .build()
        }

        binding.titleIcon.setOnClickListener{
            MaterialDialog(this).show {
                title(null,"General Information")
                message(null,"These details that you provide will work as your official contact details for the hospital that is linked to you. So please make sure that these are updated.")
                debugMode(false)
                lifecycleOwner(this@ProfileUpdate)
            }
        }
        binding.endIcon.setOnClickListener{
            GifDialog.Builder(this@ProfileUpdate)
                .setTitle("Logout")
                .setMessage("Are you sure?")
                .setPositiveBtnText("Yes")
                .setPositiveBtnBackground("#22b573")
                .setNegativeBtnText("No")
                .setNegativeBtnBackground("#c1272d")
                .setGifResource(R.raw.cycling2)
                .isCancellable(true)
                .OnPositiveClicked(object : GifDialog.GifDialogListener{
                    override fun onClick() {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(applicationContext, Splash::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                })
                .OnNegativeClicked(object : GifDialog.GifDialogListener { override fun onClick() {} })
                .build()
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

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imageCode && resultCode == Activity.RESULT_OK && data != null) {
            faceFile = data.extras!!.get("file") as File
            Glide.with(this).load(faceFile)
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20))).into(binding.faceRecognitionPic)
        }
    }
}