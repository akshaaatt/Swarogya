package com.aemerse.svarogya.health_professional

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.aemerse.svarogya.R
import com.aemerse.svarogya.databinding.HomeHealthProfActivityBinding
import com.aemerse.svarogya.register_patient.BarcodeScannerActivity
import com.aemerse.svarogya.starting.Splash
import com.aemerse.svarogya.utils.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class HomeHealthProfActivity : AppCompatActivity() {

    private lateinit var binding: HomeHealthProfActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeHealthProfActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomnavview.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        binding.bottomnavview.itemIconTintList = null

        askForPermissions(this, permissionsToGive, permissionsRequest)

        binding.codeScan.setOnClickListener {
            if(!askForPermissions(this,permissionsToGive, permissionsRequest)){
                val intent = Intent(this, BarcodeScannerActivity::class.java)
                intent.putExtra("user","professional")
                startActivity(intent)
            }
        }

        binding.faqButton.setOnClickListener{
            MaterialDialog(this).show {
                title(null,"Instructions")
                message(null,"Press the Scan Patient option and read the barcode on the patient. After that you will get access to prescribe them medicines and update their records.")
                debugMode(false)
                lifecycleOwner(this@HomeHealthProfActivity)
            }
        }

        binding.logoutButton.setOnClickListener {
            GifDialog.Builder(this@HomeHealthProfActivity)
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
                        getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE).commit {
                            putString(getString(R.string.professionalId), null)
                            putString(getString(R.string.professionalHospitalName), null)
                            putString(getString(R.string.professionalName), null)
                        }
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(applicationContext, Splash::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                })
                .OnNegativeClicked(object : GifDialog.GifDialogListener { override fun onClick() {} })
                .build()
        }
        binding.imageView.setOnClickListener {

        }
        binding.titleText.setOnClickListener {

        }

    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        logoAnimate(binding.imageView)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(!askForPermissions(this,permissionsToGive, permissionsRequest)){
            val intent = Intent(this, BarcodeScannerActivity::class.java)
            intent.putExtra("user","professional")
            startActivity(intent)
        }
    }

    private var mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem: MenuItem ->
        when (menuItem.itemId) {
            R.id.bottom_patients -> {
                val intent = Intent(this, PatientsSearch::class.java)
                startActivity(intent)
            }
        }
        false
    }
}