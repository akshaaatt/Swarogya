package com.swarogya.app.register_patient

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.bumptech.glide.Glide
import com.swarogya.app.R
import com.swarogya.app.databinding.DocumentImagesActivityBinding
import com.swarogya.app.utils.*
import java.io.File

class DocumentImages : AppCompatActivity() {

    private var allImagesFiles = ArrayList<File>()
    private var docName: String? = null
    private lateinit var binding: DocumentImagesActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DocumentImagesActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        docName = intent.getStringExtra("docName")

        binding.addImage.setOnClickListener {
            if(!askForPermissions(this,permissionsToGive, permissionsRequest)) {
                intent = Intent(applicationContext, ImageCameraActivity::class.java)
                startActivityForResult(intent, 1)
            }
        }

        binding.endIcon.setOnClickListener {
            MaterialDialog(this).show {
                title(null,"Need Help")
                icon(R.drawable.ic_help_30dp,null)
                message(null,"Don't worry, just ask any hospital staff for the manual instructions and smoothly get the job done.\nYour safety is our utmost priority.")
                debugMode(false)
                lifecycleOwner(this@DocumentImages)
            }
        }

        binding.titleIcon.setOnClickListener {
            onBackPressed()
        }

        binding.doneText.setOnClickListener {
            if(allImagesFiles.isEmpty()){
                toast("Please add some pictures to the document!")
                return@setOnClickListener
            }
            MaterialDialog(this).show {
                title(null,"Sure?")
                message(null,"Add all the images to $docName?")
                positiveButton(R.string.yes){
                    intent.putExtra("allImagesFiles", allImagesFiles)
                    intent.putExtra("docName",docName)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                negativeButton(R.string.no)
                debugMode(false)
                lifecycleOwner(this@DocumentImages)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null && requestCode == 1) {
            val file = data.extras!!.get("file") as File

            val inflate = LayoutInflater.from(this).inflate(R.layout.scrollitem_image, binding.imageContainer,false)
            val iv = inflate as ImageView
            iv.setOnClickListener {
                val intent = Intent(applicationContext,SelectedImage::class.java)
                intent.putExtra("file",file)
                startActivity(intent)
            }

            iv.setOnLongClickListener {viewIv->
                MaterialDialog(this).show {
                    title(null,"Delete Image?")
                    positiveButton(R.string.yes){
                        removeView(viewIv,file)
                    }
                    negativeButton(R.string.no)
                    debugMode(false)
                    lifecycleOwner(this@DocumentImages)
                }
                true
            }

            Glide.with(this).load(file).into(iv)
            binding.imageContainer.addView(iv)

            allImagesFiles.add(file)
        }
    }

    private fun removeView(viewIv: View?, file: File) {
        binding.imageContainer.removeView(viewIv)
        allImagesFiles.remove(file)
    }

    override fun onBackPressed() {
        if(allImagesFiles.isNotEmpty()){
            MaterialDialog(this).show {
                title(null,"Go back?")
                message(null,"You have some pictures added.")
                positiveButton(R.string.yes){
                    super.onBackPressed()
                }
                negativeButton(R.string.no)
                debugMode(false)
                lifecycleOwner(this@DocumentImages)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(!askForPermissions(this,permissionsToGive, permissionsRequest)) {
            intent = Intent(applicationContext, ImageCameraActivity::class.java)
            startActivityForResult(intent, 1)
        }
    }
}