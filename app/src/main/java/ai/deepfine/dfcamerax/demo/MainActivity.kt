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

  //================================================================================================
  // Lifecycle
  //================================================================================================
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

    cameraManager.startCamera()

    binding.isImageMode = true
    binding.isCapturing = false
    binding.flashMode = DFCameraXManager.FLASH_MODE_OFF
    binding.timer = CameraTimer.OFF
    binding.timerCount = 0
  }

  override fun onPause() {
    super.onPause()
    cameraManager.cancelTimer()
    binding.timerCount = 0
    binding.isCapturing = false
  }

  //================================================================================================
  // Overrides
  //================================================================================================
  override fun onBackPressed() {
    if (cameraManager.isTimerRunning()) {
      cameraManager.cancelTimer()
      binding.timerCount = 0
      binding.isCapturing = false
    } else super.onBackPressed()
  }


  //================================================================================================
  // onClick
  //================================================================================================
  fun controlTimer() {
    cameraManager.setOnTimerCallback { leftSeconds ->
      binding.timerCount = leftSeconds
      binding.timerTextView.text = leftSeconds.toString()
      Log.d(TAG, "$leftSeconds seconds left.")
    }

    val timer = when (cameraManager.timer) {
      CameraTimer.OFF -> CameraTimer.S3
      CameraTimer.S3 -> CameraTimer.S10
      CameraTimer.S5 -> throw NotImplementedError()
      CameraTimer.S10 -> CameraTimer.OFF
    }

    cameraManager.timer = timer
    binding.timer = timer
  }

  fun controlFlash() {
    if (binding.isCapturing == true) return

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

  fun takePicture() {
    if (binding.isCapturing == true) return

    binding.isCapturing = true
    cameraManager.takePicture()
  }

  fun startRecording() {
    if (binding.isCapturing == true) return
    cameraManager.recordVideo()
  }

  fun stopRecording() {
    cameraManager.stopRecording()
  }

  fun switchCamera() {
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
        binding.isCapturing = false
      }

      override fun onError(exception: ImageCaptureException) {
        Toast.makeText(this@MainActivity, exception.toString(), Toast.LENGTH_SHORT).show()
        binding.isCapturing = false
      }
    }
  }

  private val onVideoEventListener by lazy {
    Consumer<VideoRecordEvent> { event ->
      when (event) {
        is VideoRecordEvent.Start -> {
          binding.isCapturing = true
        }
        is VideoRecordEvent.Resume -> binding.isCapturing = true
        is VideoRecordEvent.Pause -> binding.isCapturing = false
        is VideoRecordEvent.Finalize -> {
          binding.isCapturing = false
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