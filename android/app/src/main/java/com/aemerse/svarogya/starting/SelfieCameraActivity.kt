package com.aemerse.svarogya.starting

import android.app.Activity
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.otaliastudios.cameraview.*
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.controls.Facing
import com.aemerse.svarogya.databinding.CameraActivityBinding
import com.aemerse.svarogya.utils.toast
import java.io.File

class SelfieCameraActivity : AppCompatActivity() {
    private lateinit var binding: CameraActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CameraActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .enableTracking()
                .build()
        val detector = FaceDetection.getClient(options)

        binding.camera.facing = Facing.FRONT
        binding.camera.audio = Audio.OFF

        toast("Click a proper selfie!")

        binding.capture.setOnClickListener {
            binding.progressBar.visibility = VISIBLE
            binding.camera.takePictureSnapshot()
        }

        binding.camera.addCameraListener(object : CameraListener() {
            override fun onCameraOpened(options: CameraOptions) {}
            override fun onCameraClosed() {}
            override fun onCameraError(error: CameraException) {
                binding.progressBar.visibility = GONE
            }
            override fun onPictureTaken(result: PictureResult) {
                if (!binding.camera.isTakingVideo) {
                    result.toFile(File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_"+ System.currentTimeMillis().toString() + ".jpg")) { file ->
                        val image =  InputImage.fromFilePath(this@SelfieCameraActivity, Uri.fromFile(file))

                        detector.process(image).addOnSuccessListener {results ->
                            binding.progressBar.visibility = GONE

                            if (results.size == 1) {
                                toast("Great!")
                                intent.putExtra("file", file)
                                setResult(Activity.RESULT_OK, intent)
                                finish()
                            }
                            else{
                                toast("Invalid Image! Please make sure that only your face is present, properly.")
                            }
                        }.addOnFailureListener{error->
                            binding.progressBar.visibility = GONE
                            Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onVideoTaken(result: VideoResult) {}
            override fun onOrientationChanged(orientation: Int) {}
            override fun onAutoFocusStart(point: PointF) {}
            override fun onAutoFocusEnd(successful: Boolean, point: PointF) {}

            override fun onZoomChanged(newValue: Float, bounds: FloatArray, fingers: Array<PointF>?) {}

            override fun onExposureCorrectionChanged(newValue: Float, bounds: FloatArray, fingers: Array<PointF>?) {}

            override fun onVideoRecordingStart() {}
            override fun onVideoRecordingEnd() {}
        })
    }

    public override fun onResume() {
        super.onResume()
        binding.camera.open()
    }

    public override fun onPause() {
        super.onPause()
        binding.camera.close()
    }

    public override fun onDestroy() {
        super.onDestroy()
        binding.camera.destroy()
    }
}