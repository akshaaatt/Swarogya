package com.aemerse.svarogya.starting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.PhoneBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.aemerse.svarogya.R
import com.aemerse.svarogya.health_professional.HomeHealthProfActivity
import com.aemerse.svarogya.home.HomeActivity
import com.aemerse.svarogya.utils.commit
import com.aemerse.svarogya.utils.sharedPrefFile

class Splash : AppCompatActivity() {

    private val signInCode = 100
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        FirebaseApp.initializeApp(this)

        if(sharedPreferences.getString(getString(R.string.professionalId),null) != null){
            val intent = Intent(this, HomeHealthProfActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {
            if (FirebaseAuth.getInstance().currentUser != null) {
                FirebaseFirestore.getInstance().collection("Users")
                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .get(Source.CACHE).addOnSuccessListener {
                        if (it.exists() && it.get("name")!=null) {
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else {
                            FirebaseFirestore.getInstance().collection("Users")
                                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                .get(Source.SERVER).addOnSuccessListener { doc ->
                                    if (doc.exists() && doc.get("name")!=null) {
                                        val intent = Intent(this, HomeActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        startActivityForResult(
                                            AuthUI.getInstance().createSignInIntentBuilder()
                                                .setAvailableProviders(listOf(PhoneBuilder().build()))
                                                .setTheme(R.style.AuthTheme)
                                                .build(), signInCode
                                        )
                                    }
                                }
                                .addOnFailureListener {
                                    startActivityForResult(
                                        AuthUI.getInstance().createSignInIntentBuilder()
                                            .setAvailableProviders(listOf(PhoneBuilder().build()))
                                            .setTheme(R.style.AuthTheme)
                                            .build(), signInCode
                                    )
                                }
                        }
                    }
                    .addOnFailureListener {
                        startActivityForResult(
                            AuthUI.getInstance().createSignInIntentBuilder()
                                .setAvailableProviders(listOf(PhoneBuilder().build()))
                                .setTheme(R.style.AuthTheme)
                                .build(), signInCode
                        )
                    }
            } else {
                startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(listOf(PhoneBuilder().build()))
                        .setTheme(R.style.AuthTheme)
                        .build(), signInCode
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == signInCode && resultCode == Activity.RESULT_OK) {
            loginDialog()
        }
        else{
            finish()
        }
    }

    private fun loginDialog() {
        MaterialDialog(this).show {
            title(R.string.continue_as)
            listItemsSingleChoice(R.array.Continue_As, initialSelection = 0) { _, index, _ ->
                if (index == 0) {
                    FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener {
                        if(it.exists() && it.get("name")!=null){
                            val intent = Intent(this@Splash, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else{
                            val intent = Intent(this@Splash, RegistrationActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else if (index == 1) {
                    FirebaseFirestore.getInstance().collection("Employees")
                        .whereEqualTo("phoneNumber",FirebaseAuth.getInstance().currentUser!!.phoneNumber).get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                if(task.result!!.size()==0){
                                    loginDialog()
                                    Toast.makeText(applicationContext,"Your phone number is not authenticated to any of the Hospitals",Toast.LENGTH_LONG).show()
                                }
                                else{
                                    professionalLogin(task.result!!.documents[0].id,task.result!!.documents[0].getString("hospitalName"),task.result!!.documents[0].getString("name"))
                                }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(applicationContext,it.message,Toast.LENGTH_LONG).show()
                        }

                }
            }
            cancelable(false)
            positiveButton(R.string.choose)
            debugMode(false)
            lifecycleOwner(this@Splash)
        }
    }

    private fun professionalLogin(professionalId: String, hospitalName: String?, name: String?) {
        sharedPreferences.commit {
            putString(getString(R.string.professionalId), professionalId)
            putString(getString(R.string.professionalHospitalName), hospitalName)
            putString(getString(R.string.professionalName), name)
        }
        val intent = Intent(this, HomeHealthProfActivity::class.java)
        startActivity(intent)
        finish()
    }

}