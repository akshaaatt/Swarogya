package com.swarogya.app.register_patient

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.chip.Chip
import com.google.common.base.Objects
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.limerse.mlkit.R
import com.limerse.mlkit.barcodedetection.BarcodeProcessor
import com.limerse.mlkit.barcodedetection.BarcodeResultFragment
import com.limerse.mlkit.camera.CameraSource
import com.limerse.mlkit.camera.CameraSourcePreview
import com.limerse.mlkit.camera.GraphicOverlay
import com.limerse.mlkit.camera.WorkflowModel
import com.limerse.mlkit.settings.SettingsActivity
import com.swarogya.app.health_professional.ProfessionalViewActivity
import com.swarogya.app.home.HomeActivity
import com.swarogya.app.utils.*
import java.io.File
import java.io.IOException

class BarcodeScannerActivity : AppCompatActivity(), OnClickListener {

    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var settingsButton: View? = null
    private var flashButton: View? = null
    private var promptChip: Chip? = null
    private var promptChipAnimator: AnimatorSet? = null
    private var workflowModel: WorkflowModel? = null
    private var currentWorkflowState: WorkflowModel.WorkflowState? = null
    private val tag = "LiveBarcodeActivity"
    private var userHere: String? = null
    private var docs = HashMap<String,ArrayList<File>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(com.swarogya.app.R.anim.slide_in_up, com.swarogya.app.R.anim.slide_out_up)

        setContentView(R.layout.activity_live_barcode_kotlin)

        userHere = intent.extras!!.getString("user")
        if(intent.extras!!.containsKey("docs")) {
            docs = intent.extras!!.get("docs") as HashMap<String, ArrayList<File>>
        }

        Log.d("thisDoc",docs.toString())

        preview = findViewById(R.id.camera_preview)
        graphicOverlay = findViewById<GraphicOverlay>(R.id.camera_preview_graphic_overlay).apply {
            setOnClickListener(this@BarcodeScannerActivity)
            cameraSource = CameraSource(this)
        }

        promptChip = findViewById(R.id.bottom_prompt_chip)
        promptChipAnimator =
            (AnimatorInflater.loadAnimator(this, R.animator.bottom_prompt_chip_enter) as AnimatorSet).apply {
                setTarget(promptChip)
            }

        findViewById<View>(R.id.close_button).setOnClickListener(this)
        flashButton = findViewById<View>(R.id.flash_button).apply {
            setOnClickListener(this@BarcodeScannerActivity)
        }
        settingsButton = findViewById<View>(R.id.settings_button).apply {
            setOnClickListener(this@BarcodeScannerActivity)
            visibility = GONE
        }

        setUpWorkflowModel()
    }

    override fun onResume() {
        super.onResume()

        workflowModel?.markCameraFrozen()
        settingsButton?.isEnabled = true
        currentWorkflowState = WorkflowModel.WorkflowState.NOT_STARTED
        cameraSource?.setFrameProcessor(BarcodeProcessor(graphicOverlay!!, workflowModel!!))
        workflowModel?.setWorkflowState(WorkflowModel.WorkflowState.DETECTING)
    }

    override fun onPostResume() {
        super.onPostResume()
        BarcodeResultFragment.dismiss(supportFragmentManager)
    }

    override fun onPause() {
        super.onPause()
        currentWorkflowState = WorkflowModel.WorkflowState.NOT_STARTED
        stopCameraPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource?.release()
        cameraSource = null
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.close_button -> onBackPressed()
            R.id.flash_button -> {
                flashButton?.let {
                    if (it.isSelected) {
                        it.isSelected = false
                        cameraSource?.updateFlashMode(Camera.Parameters.FLASH_MODE_OFF)
                    } else {
                        it.isSelected = true
                        cameraSource!!.updateFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
                    }
                }
            }
            R.id.settings_button -> {
                settingsButton?.isEnabled = false
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }

    private fun startCameraPreview() {
        val workflowModel = this.workflowModel ?: return
        val cameraSource = this.cameraSource ?: return
        if (!workflowModel.isCameraLive) {
            try {
                workflowModel.markCameraLive()
                preview?.start(cameraSource)
            } catch (e: IOException) {
                Log.e(tag, "Failed to start camera preview!", e)
                cameraSource.release()
                this.cameraSource = null
            }
        }
    }

    private fun stopCameraPreview() {
        val workflowModel = this.workflowModel ?: return
        if (workflowModel.isCameraLive) {
            workflowModel.markCameraFrozen()
            flashButton?.isSelected = false
            preview?.stop()
        }
    }

    private fun setUpWorkflowModel() {
        workflowModel = ViewModelProviders.of(this).get(WorkflowModel::class.java)

        // Observes the workflow state changes, if happens, update the overlay view indicators and
        // camera preview state.
        workflowModel!!.workflowState.observe(this, Observer { workflowState ->
            if (workflowState == null || Objects.equal(currentWorkflowState, workflowState)) {
                return@Observer
            }

            currentWorkflowState = workflowState
            Log.d(tag, "Current workflow state: ${currentWorkflowState!!.name}")

            val wasPromptChipGone = promptChip?.visibility == GONE

            when (workflowState) {
                WorkflowModel.WorkflowState.DETECTING -> {
                    promptChip?.visibility = VISIBLE
                    promptChip?.setText(R.string.prompt_point_at_a_barcode)
                    startCameraPreview()
                }
                WorkflowModel.WorkflowState.CONFIRMING -> {
                    promptChip?.visibility = VISIBLE
                    promptChip?.setText(R.string.prompt_move_camera_closer)
                    startCameraPreview()
                }
                WorkflowModel.WorkflowState.SEARCHING -> {
                    promptChip?.visibility = VISIBLE
                    promptChip?.setText(R.string.prompt_searching)
                    stopCameraPreview()
                }
                WorkflowModel.WorkflowState.DETECTED, WorkflowModel.WorkflowState.SEARCHED -> {
                    promptChip?.visibility = GONE
                    stopCameraPreview()
                }
                else -> promptChip?.visibility = GONE
            }

            val shouldPlayPromptChipEnteringAnimation = wasPromptChipGone && promptChip?.visibility == VISIBLE
            promptChipAnimator?.let {
                if (shouldPlayPromptChipEnteringAnimation && !it.isRunning) it.start()
            }
        })

        workflowModel?.detectedBarcode?.observe(this, Observer { barcode ->
            if (barcode != null && !barcode.rawValue.toString().trim().contains("/")) {
                val barcodeHere = barcode.rawValue.toString().trim()
                findPatientId(barcodeHere)
            }
            else{
                toast("Invalid format")
                finish()
            }
        })
    }

    fun findPatientId(patientIdHere: String) {
        FirebaseFirestore.getInstance().collection("OngoingTreatments")
            .whereEqualTo("patientId",patientIdHere).get(Source.SERVER).addOnCompleteListener {task->
                if (task.isSuccessful) {
                    if(task.result!!.size()==0){
                        toast("No match found!")
                        finish()
                    }
                    else{
                        val treatmentDoc = task.result!!.documents[0]
                        if(!treatmentDoc.contains("hospitalName")){
                            Toast.makeText(applicationContext,"The database is corrupted. Contact the hospital management!",Toast.LENGTH_LONG).show()
                            finish()
                            return@addOnCompleteListener
                        }
                        if (userHere == "regular" && !treatmentDoc.contains("phoneNumber")) {
                            MaterialDialog(this).show {
                                title(null, "Confirm Registration")
                                icon(com.swarogya.app.R.drawable.ic_data_transfer_30dp, null)
                                message(null, "Joining with ${treatmentDoc.getString("hospitalName")}")
                                positiveButton(com.swarogya.app.R.string.accept) {

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
                                                        putString(getString(com.swarogya.app.R.string.currentPatientId),treatmentDoc.getString("patientId"))
                                                    }
                                                    GifDialog.Builder(this@BarcodeScannerActivity)
                                                        .setTitle("Almost done. Uploading files.")
                                                        .setMessage("Do not close your app for a few minutes.")
                                                        .setPositiveBtnText("Okay")
                                                        .setPositiveBtnBackground("#22b573")
                                                        .setGifResource(com.swarogya.app.R.raw.progress)
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
                                                    finish()
                                                }
                                        }
                                }
                                negativeButton(com.swarogya.app.R.string.cancel) {
                                    finish()
                                }
                                debugMode(false)
                                lifecycleOwner(this@BarcodeScannerActivity)
                            }
                        }
                        else if(userHere == "professional" && getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)
                                .getString(getString(com.swarogya.app.R.string.professionalHospitalName),null) == treatmentDoc.getString("hospitalName")){
                            intent = Intent(applicationContext, ProfessionalViewActivity::class.java)
                            intent.putExtra("docId",treatmentDoc.id)
                            startActivity(intent)
                            finish()
                        }
                        else if(userHere == "regular" && treatmentDoc.contains("phoneNumber")){
                            if(treatmentDoc.getString("phoneNumber")==FirebaseAuth.getInstance().currentUser!!.phoneNumber){
                                getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE).commit {
                                    putString(getString(com.swarogya.app.R.string.currentPatientId), treatmentDoc.getString("patientId"))
                                }
                                intent = Intent(applicationContext,HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else{
                                toast("This id is registered to another patient already!")
                                finish()
                            }
                        }
                        else if(userHere == "professional" && getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)
                                .getString(getString(com.swarogya.app.R.string.professionalHospitalName),null) != treatmentDoc.getString("hospitalName")){
                            toast("Sorry but you are not authorized to get details of this patient!")
                            finish()
                        }
                    }
                }
            }
            .addOnFailureListener {
                toast(it.message.toString())
                finish()
            }
    }
}
