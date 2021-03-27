package com.swarogya.app.register_patient

import android.app.Activity
import android.graphics.PointF
import android.os.Bundle
import android.os.Environment
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.otaliastudios.cameraview.*
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.controls.Facing
import com.swarogya.app.databinding.CameraActivityBinding
import java.io.File

class ImageCameraActivity : AppCompatActivity() {
    private lateinit var binding: CameraActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CameraActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.camera.facing = Facing.BACK
        binding.camera.audio = Audio.OFF

        binding.capture.setOnClickListener {
            binding.progressBar.visibility = VISIBLE
            binding.camera.takePicture()
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
                        binding.progressBar.visibility = GONE
                        intent.putExtra("file", file)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
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