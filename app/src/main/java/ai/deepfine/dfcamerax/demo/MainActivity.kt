package ai.deepfine.dfcamerax.demo

import ai.deepfine.dfcamerax.demo.databinding.ActivityMainBinding
import ai.deepfine.dfcamerax.utils.CameraTimer
import ai.deepfine.dfcamerax.config.DFCameraXHandler
import ai.deepfine.dfcamerax.utils.CameraMode
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.VideoCapture
import androidx.databinding.DataBindingUtil

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private lateinit var cameraHandler: DFCameraXHandler
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    binding.view = this

    cameraHandler = DFCameraXHandler.Builder(this, this)
      .setCameraMode(CameraMode.Image)
      .setPreviewView(binding.previewView)
//      .setImageOutputDirectory(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString())
//      .setVideoOutputDirectory(getExternalFilesDir(Environment.DIRECTORY_MOVIES).toString())
      .setOnImageSavedCallback(onImageSavedCallback)
      .setOnVideoSavedCallback(onVideoSavedCallback)
      .build()

    setTimer()
//    setTargetResolution()
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

  fun startRecording() {
    cameraHandler.recordVideo()
  }

  fun stopRecording() {
    cameraHandler.stopRecording()
  }

  fun toggle() {
    when (cameraHandler.lensFacing) {
      CameraSelector.DEFAULT_BACK_CAMERA -> cameraHandler.lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA
      CameraSelector.DEFAULT_FRONT_CAMERA -> cameraHandler.lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    }
  }

  fun changeMode() {
    cameraHandler.changeCameraMode(CameraMode.Video)
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

  private val onVideoSavedCallback by lazy {
    object : VideoCapture.OnVideoSavedCallback {
      @SuppressLint("RestrictedApi")
      override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
        Toast.makeText(this@MainActivity, outputFileResults.savedUri.toString(), Toast.LENGTH_SHORT).show()
      }

      @SuppressLint("RestrictedApi")
      override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
        Log.e("PYC", cause.toString())
        Toast.makeText(this@MainActivity, cause.toString(), Toast.LENGTH_SHORT).show()
      }
    }
  }
}