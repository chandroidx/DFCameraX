package ai.deepfine.dfcamerax.demo

import ai.deepfine.dfcamerax.demo.databinding.ActivityMainBinding
import ai.deepfine.dfcamerax.utils.CameraTimer
import ai.deepfine.dfcamerax.config.DFCameraXHandler
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.databinding.DataBindingUtil

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private lateinit var cameraHandler: DFCameraXHandler
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    binding.view = this

    cameraHandler = DFCameraXHandler.Builder(this, this)
      .setPreviewView(binding.previewView)
      .setOutputDirectory(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString())
      .setOnImageSavedCallback(onImageSavedCallback)
      .build()

    setTimer()
    setTargetResolution()
    cameraHandler.startCamera()
  }


  private fun setTimer() {
    cameraHandler.timer = CameraTimer.OFF
    cameraHandler.setOnTimerCallback { leftSeconds -> Log.d("PYC", "${leftSeconds}초 남았습니다.") }
  }

  fun controlFlash() {
    when (cameraHandler.flashMode) {
      ImageCapture.FLASH_MODE_OFF -> cameraHandler.flashMode = ImageCapture.FLASH_MODE_ON
      ImageCapture.FLASH_MODE_ON -> cameraHandler.flashMode = ImageCapture.FLASH_MODE_OFF
    }
  }

  fun capture() {
    cameraHandler.takePicture()
  }

  fun toggle() {
    when (cameraHandler.lensFacing) {
      CameraSelector.DEFAULT_BACK_CAMERA -> cameraHandler.lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA
      CameraSelector.DEFAULT_FRONT_CAMERA -> cameraHandler.lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    }
  }

  private fun setTargetResolution() {
    cameraHandler.setTargetResolution(Size(360, 640))
  }

  private val onImageSavedCallback by lazy {
    object : ImageCapture.OnImageSavedCallback {
      override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
        Toast.makeText(this@MainActivity, outputFileResults.savedUri.toString(), Toast.LENGTH_SHORT).show()
      }

      override fun onError(exception: ImageCaptureException) {
        Log.e("PYC", exception.toString())
        Toast.makeText(this@MainActivity, exception.toString(), Toast.LENGTH_SHORT).show()
      }
    }
  }
}