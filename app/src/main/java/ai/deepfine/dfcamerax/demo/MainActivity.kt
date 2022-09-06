package ai.deepfine.dfcamerax.demo

import ai.deepfine.dfcamerax.demo.databinding.ActivityMainBinding
import ai.deepfine.dfcamerax.utils.CameraTimer
import ai.deepfine.dfcamerax.config.DFCameraXHandler
import ai.deepfine.dfcamerax.utils.CameraMode
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.util.Consumer
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
      .setPreviewTargetResolution(Size(360, 640))
//      .setImageOutputDirectory(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString())
//      .setVideoOutputDirectory(getExternalFilesDir(Environment.DIRECTORY_MOVIES).toString())
      .setOnImageSavedCallback(onImageSavedCallback)
      .setOnVideoSavedCallback(onVideoSavedCallback)
      .build()

    setTimer()
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

  fun changeToImageMode() {
    cameraHandler.changeCameraMode(CameraMode.Image)
    binding.videoModeButton.visibility = View.VISIBLE
    binding.imageModeButton.visibility = View.GONE
    binding.takePictureButton.visibility = View.VISIBLE
    binding.startRecordingButton.visibility = View.GONE
    binding.stopRecordingButton.visibility = View.GONE
  }

  fun changeToVideoMode() {
    cameraHandler.changeCameraMode(CameraMode.Video)
    binding.videoModeButton.visibility = View.GONE
    binding.imageModeButton.visibility = View.VISIBLE
    binding.takePictureButton.visibility = View.GONE
    binding.startRecordingButton.visibility = View.VISIBLE
    binding.stopRecordingButton.visibility = View.GONE
  }

  private val onImageSavedCallback by lazy {
    object : ImageCapture.OnImageSavedCallback {
      override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
        Toast.makeText(this@MainActivity, outputFileResults.savedUri.toString(), Toast.LENGTH_SHORT).show()
      }

      override fun onError(exception: ImageCaptureException) {
        Toast.makeText(this@MainActivity, exception.toString(), Toast.LENGTH_SHORT).show()
      }
    }
  }

  private val onVideoSavedCallback by lazy {
    Consumer<VideoRecordEvent> { event ->
      when (event) {
        is VideoRecordEvent.Start -> {
          Log.d("PYC", "Start : ${event.recordingStats}")
          binding.videoModeButton.visibility = View.GONE
          binding.imageModeButton.visibility = View.GONE
          binding.takePictureButton.visibility = View.GONE
          binding.startRecordingButton.visibility = View.GONE
          binding.stopRecordingButton.visibility = View.VISIBLE
          binding.toggleButton.visibility = View.GONE
        }
        is VideoRecordEvent.Resume -> Log.d("PYC", "Resume : ${event.recordingStats}")
        is VideoRecordEvent.Pause -> Log.d("PYC", "Pause : ${event.recordingStats}")
        is VideoRecordEvent.Finalize -> {
          binding.videoModeButton.visibility = View.GONE
          binding.imageModeButton.visibility = View.VISIBLE
          binding.startRecordingButton.visibility = View.VISIBLE
          binding.stopRecordingButton.visibility = View.GONE
          binding.toggleButton.visibility = View.VISIBLE
          Log.d("PYC", "Finalize : ${event.outputResults.outputUri}")
          Toast.makeText(this@MainActivity, event.outputResults.outputUri.toString(), Toast.LENGTH_SHORT).show()
        }
        is VideoRecordEvent.Status -> Log.d("PYC", "Status : ${event.recordingStats}")
      }
    }
  }
}