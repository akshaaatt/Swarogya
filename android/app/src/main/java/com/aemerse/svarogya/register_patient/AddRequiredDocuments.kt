package com.aemerse.svarogya.register_patient

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.aemerse.svarogya.R
import com.aemerse.svarogya.databinding.AddRequiredDocumentsActivityBinding
import com.aemerse.svarogya.home.HomeActivity
import com.aemerse.svarogya.utils.*
import java.io.File

class AddRequiredDocuments : AppCompatActivity() {

    private var docs = HashMap<String,ArrayList<File>>()
    private var userHere: String? = null
    private lateinit var binding:AddRequiredDocumentsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddRequiredDocumentsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userHere = intent.extras!!.getString("user")

        if(userHere=="professional"){
            binding.addDocumentText.text = getString(R.string.otherwise)
        }

        binding.titleIcon.setOnClickListener {
            onBackPressed()
        }
        binding.endIcon.setOnClickListener {
            MaterialDialog(this).show {
                title(null,"Need Help")
                icon(R.drawable.ic_help_30dp,null)
                message(null,"Don't worry, just ask any hospital staff for the manual instructions and smoothly get the job done. Your safety is our utmost priority.")
                debugMode(false)
                lifecycleOwner(this@AddRequiredDocuments)
            }
        }
        binding.addDocument.setOnClickListener {
            MaterialDialog(this).show {
                title(null,"Continue?")
                message(null,"Set the document name.\nEx: Blood Reports")
                input(hint = "Document name", inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS) { _, text ->
                    val intent = Intent(applicationContext,DocumentImages::class.java)
                    intent.putExtra("docName",text.toString())
                    startActivityForResult(intent,0)
                }
                positiveButton(R.string.set_name)
                negativeButton(R.string.cancel)
                debugMode(false)
                lifecycleOwner(this@AddRequiredDocuments)
            }
        }

        binding.proceedText.setOnClickListener {
            if(docs.isEmpty()){
                toast("Please add the required documents!")
                return@setOnClickListener
            }
            if(userHere=="professional"){
                MaterialDialog(this).show {
                    title(null,"Continue?")
                    message(null,"Please make sure that the document names have not been repeated. This will add the given documents to the patient data records.")
                    icon(R.drawable.ic_files_and_folders_24dp)
                    positiveButton(R.string.proceed){
                        docs.forEach { (key, value) ->
                            value.forEachIndexed { index, file ->
                                uploadFile(file,index+1,key,intent.extras!!.getString("doc")!!,applicationContext)
                            }
                        }
                        finish()
                    }
                    negativeButton(R.string.cancel)
                    debugMode(false)
                    lifecycleOwner(this@AddRequiredDocuments)
                }
            }
            else {
                MaterialDialog(this).show {
                    title(null, "Continue?")
                    message(
                        null,
                        "It is advised to have shown all the documents to a staff before proceeding to scanning the barcode to avoid sending wrong or insufficient data."
                    )
                    icon(R.drawable.ic_qr_code_24dp)
                    positiveButton(R.string.proceed) {
                        val intent = Intent(applicationContext, BarcodeScannerActivity::class.java)
                        intent.putExtra("docs", docs)
                        intent.putExtra("user", userHere)
                        startActivity(intent)
                    }
                    negativeButton(R.string.cancel)
                    @Suppress("DEPRECATION")
                    neutralButton(R.string.type){
                        MaterialDialog(this@AddRequiredDocuments).show {
                            title(null,"Id Number")
                            message(null,"Type the id number provided by the hospital")
                            input(hint = "Id number", inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED) { _, text ->
                               findPatientId(text.toString().trim())
                            }
                            positiveButton(R.string.proceed)
                            negativeButton(R.string.cancel)
                            debugMode(false)
                            lifecycleOwner(this@AddRequiredDocuments)
                        }
                    }
                    debugMode(false)
                    lifecycleOwner(this@AddRequiredDocuments)
                }
            }
        }
    }

    override fun onBackPressed() {
        if(docs.isNotEmpty()){
            MaterialDialog(this).show {
                title(null,"Go back?")
                message(null,"You have some documents added.")
                positiveButton(R.string.yes){
                    super.onBackPressed()
                }
                negativeButton(R.string.no)
                debugMode(false)
                lifecycleOwner(this@AddRequiredDocuments)
            }
        }
        else {
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null && requestCode == 0) {
            val allImagesFiles = data.extras!!.get("allImagesFiles") as ArrayList<File>
            docs[data.extras!!.getString("docName")!!] = allImagesFiles

            val inflate = LayoutInflater.from(this).inflate(R.layout.scrollitem_text, binding.docNamesScroll,false)
            val iv = inflate as TextView

            val docName = data.extras!!.getString("docName")!!

            iv.text = data.extras!!.getString("docName")!!
            binding.docNamesScroll.addView(iv)

            iv.setOnClickListener {
                binding.imageContainer.removeAllViews()
                docs[iv.text!!]!!.forEach {file->
                    showImages(file)
                }
            }

            iv.setOnLongClickListener {viewTv->
                MaterialDialog(this).show {
                    title(null,"Delete Document?")
                    positiveButton(R.string.yes){
                        removeView(viewTv,docName)
                    }
                    negativeButton(R.string.no)
                    debugMode(false)
                    lifecycleOwner(this@AddRequiredDocuments)
                }
                true
            }
        }
    }
    private fun removeView(viewTv: View?, docName: String) {
        binding.docNamesScroll.removeView(viewTv)
        docs.remove(docName)
        binding.imageContainer.removeAllViews()
    }

    private fun showImages(file: File) {
        val inflateImage = LayoutInflater.from(this).inflate(R.layout.scrollitem_image, binding.imageContainer,false)
        val ivImage = inflateImage as ImageView
        ivImage.setOnClickListener {
            val intent = Intent(applicationContext,SelectedImage::class.java)
            intent.putExtra("file",file)
            startActivity(intent)
        }

        Glide.with(this).load(file).into(ivImage)
        binding.imageContainer.addView(ivImage)
    }

    fun findPatientId(patientIdHere: String) {
        FirebaseFirestore.getInstance().collection("OngoingTreatments")
            .whereEqualTo("patientId",patientIdHere).get(Source.SERVER).addOnCompleteListener { task->
                if (task.isSuccessful) {
                    if(task.result!!.size()==0){
                        toast("No match found!")
                    }
                    else{
                        val treatmentDoc = task.result!!.documents[0]
                        if(!treatmentDoc.contains("hospitalName")){
                            Toast.makeText(applicationContext,"The database is corrupted. Contact the hospital management!",
                                Toast.LENGTH_LONG).show()
                            return@addOnCompleteListener
                        }
                        if (userHere == "regular" && !treatmentDoc.contains("phoneNumber")) {
                            MaterialDialog(this).show {
                                title(null, "Confirm Registration")
                                icon(R.drawable.ic_data_transfer_30dp, null)
                                message(null, "Joining with ${treatmentDoc.getString("hospitalName")}")
                                positiveButton(R.string.accept) {
                                    FirebaseFirestore.getInstance().collection("Users")
                                        .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                                        .addOnSuccessListener { userDoc->
                                            docs.forEach { (key, value) ->
                                                value.forEachIndexed { index, file ->
                                                    uploadFile(file,index+1,key,treatmentDoc.id,applicationContext)
                                                }
                                            }

                                            FirebaseFirestore.getInstance().collection("OngoingTreatments")
                                                .document(treatmentDoc.id)
                                                .update("bloodGroup",userDoc.get("bloodGroup")
                                                    ,"name",userDoc.get("name")
                                                    , "firstEmergencyContactNumber",userDoc.get("firstEmergencyContactNumber")
                                                    , "firstEmergencyContactName",userDoc.get("firstEmergencyContactName")
                                                    , "secondEmergencyContactNumber",userDoc.get("secondEmergencyContactNumber")
                                                    , "secondEmergencyContactName",userDoc.get("secondEmergencyContactName")
                                                    , "emailId",userDoc.get("emailId")
                                                    , "phoneNumber",userDoc.get("phoneNumber")
                                                    , "dob",userDoc.get("dob")
                                                    , "facePic",userDoc.get("facePic")
                                                    , "address",userDoc.get("address")
                                                    , "gender",userDoc.get("gender")
                                                    , "joinedOn", FieldValue.serverTimestamp()).addOnSuccessListener {

                                                    getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE).commit {
                                                        putString(getString(R.string.currentPatientId),treatmentDoc.getString("patientId"))
                                                    }
                                                    GifDialog.Builder(this@AddRequiredDocuments)
                                                        .setTitle("Almost done. Uploading files.")
                                                        .setMessage("Do not close your app for a few minutes.")
                                                        .setPositiveBtnText("Okay")
                                                        .setPositiveBtnBackground("#22b573")
                                                        .setGifResource(com.aemerse.svarogya.R.raw.progress)
                                                        .isCancellable(false)
                                                        .OnPositiveClicked(object :
                                                            GifDialog.GifDialogListener {
                                                            override fun onClick() {
                                                                intent = Intent(applicationContext, HomeActivity::class.java)
                                                                startActivity(intent)
                                                                finish()
                                                            }
                                                        })
                                                        .build()
                                                }
                                                .addOnFailureListener {ex->
                                                    toast(ex.message.toString())
                                                }
                                        }
                                }
                                negativeButton(R.string.cancel)
                                debugMode(false)
                                lifecycleOwner(this@AddRequiredDocuments)
                            }
                        }
                        else if(userHere == "regular" && treatmentDoc.contains("phoneNumber")){
                            if(treatmentDoc.getString("phoneNumber")== FirebaseAuth.getInstance().currentUser!!.phoneNumber){
                                getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE).commit {
                                    putString(getString(R.string.currentPatientId), treatmentDoc.getString("patientId"))
                                }
                                intent = Intent(applicationContext, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else{
                                toast("This id is registered to another patient already!")
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                toast(it.message.toString())
            }
    }
}