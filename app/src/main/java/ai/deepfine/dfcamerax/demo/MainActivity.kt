package ai.deepfine.dfcamerax.demo

import ai.deepfine.dfcamerax.demo.databinding.ActivityMainBinding
import ai.deepfine.dfcamerax.utils.CameraTimer
import ai.deepfine.dfcamerax.config.DFCameraXCompat
import ai.deepfine.dfcamerax.usecases.DFCameraXManager
import ai.deepfine.dfcamerax.utils.CameraMode
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.video.Quality
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.camera.view.PreviewView.StreamState.*
import androidx.core.util.Consumer
import androidx.databinding.DataBindingUtil

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private lateinit var cameraManager: DFCameraXManager
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    binding.view = this
    cameraManager = DFCameraXCompat.Builder(this, this)
      // Camera Config
      .setCameraMode(CameraMode.Image)
      // Preview Config
      .setPreviewView(binding.previewView)
      .setPreviewTargetResolution(Size(360, 640))
      .setOnPreviewStreamCallback(this, onStreamStateChanged)
      // Image Config
//      .setImageCaptureTargetResolution(Size(1080, 1920))
//      .setImageOutputDirectory(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString())
      .setOnImageSavedCallback(onImageSavedCallback)
      // Video Config
      .setVideoQuality(Quality.FHD, Quality.HD)
//      .setVideoOutputDirectory(getExternalFilesDir(Environment.DIRECTORY_MOVIES).toString())
      .setOnVideoEventListener(onVideoEventListener)
      .build()

    setTimer()
    cameraManager.startCamera()

    binding.isImageMode = true
    binding.isRecording = false
    binding.flashMode = DFCameraXManager.FLASH_MODE_OFF
  }

  override fun onPause() {
    super.onPause()
    cameraManager.cancelTimer()
  }

  override fun onBackPressed() {
    if (cameraManager.isTimerRunning()) cameraManager.cancelTimer()
    else super.onBackPressed()
  }

  private fun setTimer() {
    cameraManager.timer = CameraTimer.OFF
    cameraManager.setOnTimerCallback { leftSeconds -> Log.d(TAG, "$leftSeconds seconds left.") }
  }

  fun controlFlash() {
    val toFlashMode = when (cameraManager.cameraMode) {
      CameraMode.Image -> {
        when (cameraManager.flashMode) {
          DFCameraXManager.FLASH_MODE_OFF -> DFCameraXManager.FLASH_MODE_ON
          DFCameraXManager.FLASH_MODE_ON -> DFCameraXManager.FLASH_MODE_AUTO
          DFCameraXManager.FLASH_MODE_AUTO -> DFCameraXManager.FLASH_MODE_OFF
          else -> throw NotImplementedError()
        }
      }
      CameraMode.Video -> when (cameraManager.flashMode) {
        DFCameraXManager.FLASH_MODE_OFF -> DFCameraXManager.FLASH_MODE_ON
        DFCameraXManager.FLASH_MODE_ON -> DFCameraXManager.FLASH_MODE_OFF
        else -> throw NotImplementedError()
      }
    }

    cameraManager.flashMode = toFlashMode
    binding.flashMode = toFlashMode
  }

  fun capture() {
    cameraManager.takePicture()
  }

  fun startRecording() {
    cameraManager.recordVideo()
  }

  fun stopRecording() {
    cameraManager.stopRecording()
  }

  fun toggle() {
    when (cameraManager.lensFacing) {
      CameraSelector.DEFAULT_BACK_CAMERA -> cameraManager.lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA
      CameraSelector.DEFAULT_FRONT_CAMERA -> cameraManager.lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    }
  }

  fun toggleCameraMode() {
    when (cameraManager.cameraMode) {
      CameraMode.Image -> {
        cameraManager.cameraMode = CameraMode.Video
        binding.isImageMode = false
      }
      CameraMode.Video -> {
        cameraManager.cameraMode = CameraMode.Image
        binding.isImageMode = true
      }
    }
    binding.flashMode = DFCameraXManager.FLASH_MODE_OFF
  }

  private val onStreamStateChanged: (PreviewView.StreamState) -> Unit by lazy {
    { streamState ->
      when (streamState) {
        IDLE -> Log.d(TAG, "StreamState : IDLE")
        STREAMING -> Log.d(TAG, "Supported Resolutions : ${cameraManager.getSupportedResolutions()}")
      }
    }
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

  private val onVideoEventListener by lazy {
    Consumer<VideoRecordEvent> { event ->
      when (event) {
        is VideoRecordEvent.Start -> {
          binding.isRecording = true
        }
        is VideoRecordEvent.Resume -> binding.isRecording = true
        is VideoRecordEvent.Pause -> binding.isRecording = false
        is VideoRecordEvent.Finalize -> {
          binding.isRecording = false
          Toast.makeText(this@MainActivity, event.outputResults.outputUri.toString(), Toast.LENGTH_SHORT).show()
        }
        is VideoRecordEvent.Status -> Log.d(TAG, "Status : ${event.recordingStats}")
      }
    }
  }

  companion object {
    private const val TAG = "DFCameraXDemo"
  }
}