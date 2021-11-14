package com.aemerse.svarogya.profile

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.aemerse.svarogya.R
import com.aemerse.svarogya.databinding.ProfileActivityBinding
import com.aemerse.svarogya.home.HomeActivity
import com.aemerse.svarogya.models.User
import com.aemerse.svarogya.register_patient.AddRequiredDocuments
import com.aemerse.svarogya.utils.SelectedImage
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ProfileActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        binding = ProfileActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomnavview.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        binding.bottomnavview.itemIconTintList = null

        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().currentUser!!.uid)
            .get().addOnSuccessListener {
                val user = it.toObject(User::class.java)
                when (user!!.gender) {
                    "Male" -> binding.genderIcon.setImageResource(R.drawable.ic_male_gender_24dp)
                    "Female" -> binding.genderIcon.setImageResource(R.drawable.ic_female_gender_24dp)
                    "Other" -> binding.genderIcon.setImageResource(R.drawable.ic_intersex_24dp)
                }
                binding.genderText.text = user.gender
                binding.nameText.text = user.name
                binding.bloodGroupText.text = user.bloodGroup
                binding.addressText.text = user.address
                binding.phoneNumberText.text = user.phoneNumber
                binding.firsEmergencyContactNumberText.text = user.firstEmergencyContactNumber
                binding.firsEmergencyContactNameText.text = user.firstEmergencyContactName
                binding.secondEmergencyContactNumberText.text = user.secondEmergencyContactNumber
                binding.secondEmergencyContactNameText.text = user.secondEmergencyContactName
                binding.emailIdText.text = user.emailId

                try {
                    binding.dobText.text = SimpleDateFormat("MMM d, yyyy",Locale.getDefault()).format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(user.dob!!)!!)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }

                Glide.with(this).load(user.facePic)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                    .listener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                            if (binding.progressBar != null) binding.progressBar.visibility = View.GONE
                            binding.faceRecognitionPic.visibility = VISIBLE
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                            if (binding.progressBar != null) binding.progressBar.visibility = View.GONE
                            binding.faceRecognitionPic.visibility = VISIBLE
                            return false
                        }
                    }).into(binding.faceRecognitionPic)

                binding.settingsButton.setOnClickListener {
                    intent = Intent(this,ProfileUpdate::class.java)
                    intent.putExtra("user",user)
                    startActivity(intent)
                }
                binding.faceRecognitionPic.setOnClickListener {
                    intent = Intent(applicationContext,SelectedImage::class.java)
                    intent.putExtra("file",user.facePic)
                    startActivity(intent)
                }
            }

        binding.flagButton.setOnClickListener {
            MaterialDialog(this).show {
                title(null, "All the best!")
                icon(R.drawable.ic_india_30dp, null)
                message(R.string.message)
                debugMode(false)
                lifecycleOwner(this@ProfileActivity)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
    }

    private var mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem: MenuItem ->
        when (menuItem.itemId) {
            R.id.bottom_home -> {
                intent = Intent(applicationContext, HomeActivity::class.java)
                startActivity(intent)
            }
            R.id.bottom_scan -> {
                val intent = Intent(this, AddRequiredDocuments::class.java)
                intent.putExtra("user","regular")
                startActivity(intent)
            }
            R.id.bottom_profile ->{
                binding.scrollView.smoothScrollTo(0,0)
            }
        }
        false
    }
}