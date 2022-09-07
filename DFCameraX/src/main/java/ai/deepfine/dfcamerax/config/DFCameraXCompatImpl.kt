package ai.deepfine.dfcamerax.config

import ai.deepfine.dfcamerax.utils.CameraMode
import ai.deepfine.dfcamerax.utils.CameraTimer
import android.content.Context
import android.util.Log
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutionException

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-09-05
 * @version 1.0.0
 */
internal class DFCameraXCompatImpl(private val lifecycleOwner: LifecycleOwner, private val context: Context) : DFCameraXCompat {
  companion object {
    private const val TAG = "DFCameraX"
  }

  private lateinit var cameraMode: CameraMode
  private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
  private lateinit var cameraProvider: ProcessCameraProvider
  private lateinit var preview: Preview
  private lateinit var camera: Camera

  private lateinit var previewView: PreviewView

  private var _lensFacing: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
  private var _timer: CameraTimer = CameraTimer.OFF
  private var timerCallback: CameraTimer.Callback? = null

  private var _previewTargetResolution: Size? = null
  private var _imageCaptureTargetResolution: Size? = null
  private var _quality: Quality? = null
  private var _higherQualityOrLowerThan: Quality? = null

  private var imageOutputDirectory: String? = null
  private var videoOutputDirectory: String? = null

  private var runningTimer: Job? = null

  override fun startCamera() {
    cameraProviderFuture.addListener({
      fetchCameraProvider {
        return@fetchCameraProvider
      }

      preview = cameraMode.createPreview(previewView, _previewTargetResolution)

      cameraProvider.unbindAll()

      bindToLifecycle()
    }, ContextCompat.getMainExecutor(context))
  }

  private fun fetchCameraProvider(onErrorCaught: () -> Unit) {
    try {
      cameraProvider = cameraProviderFuture.get()
    } catch (e: InterruptedException) {
      Log.e(TAG, "Error starting camera : $e")
      onErrorCaught()
    } catch (e: ExecutionException) {
      Log.e(TAG, "Error starting camera : $e")
      onErrorCaught()
    }
  }

  override fun setCameraMode(cameraMode: CameraMode) {
    this.cameraMode = cameraMode
  }

  override fun changeCameraMode(cameraMode: CameraMode) {
    this.cameraMode = cameraMode
    flashMode = ImageCapture.FLASH_MODE_OFF
    startCamera()
  }

  private fun bindToLifecycle() {
    try {
      camera = cameraProvider.bindToLifecycle(
        lifecycleOwner,
        lensFacing,
        preview,
        *createUseCases(),
      )

      preview.setSurfaceProvider(previewView.surfaceProvider)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to bind use cases : $e")
    }
  }

  private fun createUseCases(): Array<UseCase> = when (cameraMode) {
    CameraMode.Image -> (cameraMode as CameraMode.Image).createUseCases(previewView, _imageCaptureTargetResolution)
    CameraMode.Video -> (cameraMode as CameraMode.Video).createUseCases(_quality, _higherQualityOrLowerThan)
  }.toTypedArray()


  private val orientationEventListener = object : OrientationEventListener(context) {
    override fun onOrientationChanged(orientation: Int) {
      val rotation = when (orientation) {
        in 45..134 -> Surface.ROTATION_270
        in 135..224 -> Surface.ROTATION_180
        in 225..314 -> Surface.ROTATION_90
        else -> Surface.ROTATION_0
      }

      cameraMode.setTargetRotation(rotation)
    }
  }

  override fun setPreviewView(previewView: PreviewView) {
    this.previewView = previewView
  }

  override fun enableAutoRotation(enabled: Boolean) {
    when (enabled) {
      true -> orientationEventListener.enable()
      false -> orientationEventListener.disable()
    }
  }

  override var lensFacing: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    get() = _lensFacing
    set(value) {
      _lensFacing = value
      field = value
      startCamera()
    }

  private var _flashMode: Int = ImageCapture.FLASH_MODE_OFF
  override var flashMode: Int = _flashMode
    get() = _flashMode
    set(value) {
      _flashMode = value

      if (::camera.isInitialized) {
        camera.cameraControl.enableTorch(value != ImageCapture.FLASH_MODE_OFF)
      }
      field = value
    }

  override fun setPreviewTargetResolution(targetResolution: Size) {
    _previewTargetResolution = targetResolution
  }

  override fun setImageCaptureTargetResolution(targetResolution: Size) {
    _imageCaptureTargetResolution = targetResolution
  }

  override fun setVideoQuality(quality: Quality, higherQualityOrLowerThan: Quality?) {
    _quality = quality
    _higherQualityOrLowerThan = higherQualityOrLowerThan
  }

  override fun setImageOutputDirectory(path: String) {
    this.imageOutputDirectory = path
  }

  override fun setVideoOutputDirectory(path: String) {
    this.videoOutputDirectory = path
  }

  override var timer: CameraTimer = _timer
    get() = _timer
    set(value) {
      _timer = value
      field = value
    }

  override fun setOnTimerCallback(callback: CameraTimer.Callback) {
    this.timerCallback = callback
  }

  override fun cancelTimer() {
    if (runningTimer?.isActive == true) {
      runningTimer?.cancel()
      runningTimer = null
    }
  }

  override fun isTimerRunning(): Boolean = runningTimer?.isActive ?: false

  override fun takePicture() {
    runTimer {
      (cameraMode as? CameraMode.Image)?.takePicture(context, imageOutputDirectory, imageSavedCallback)
    }
  }

  private lateinit var imageSavedCallback: ImageCapture.OnImageSavedCallback
  override fun setOnImageSavedCallback(callback: ImageCapture.OnImageSavedCallback) {
    this.imageSavedCallback = callback
  }

  override fun recordVideo() {
    runTimer {
      (cameraMode as? CameraMode.Video)?.recordVideo(context, videoOutputDirectory, videoSavedCallback)
    }
  }

  override fun stopRecording() {
    (cameraMode as? CameraMode.Video)?.stopRecording()
  }

  private lateinit var videoSavedCallback: Consumer<VideoRecordEvent>
  override fun setOnVideoRecordEventListener(listener: Consumer<VideoRecordEvent>) {
    this.videoSavedCallback = listener
  }

  override fun setOnPreviewStreamStateCallback(lifecycleOwner: LifecycleOwner, onStreamStateChanged: (PreviewView.StreamState) -> Unit) {
    previewView.previewStreamState.observe(lifecycleOwner) {
      onStreamStateChanged(it)
    }
  }

  override fun getSupportedResolutions(): Map<Quality, Size> = try {
    _lensFacing.filter(cameraProvider.availableCameraInfos).firstOrNull()?.let { cameraInfo ->
      QualitySelector.getSupportedQualities(cameraInfo).associateWith { quality ->
        QualitySelector.getResolution(cameraInfo, quality)!!
      }
    } ?: emptyMap()
  } catch (e: Exception) {
    Log.e(TAG, "You must initialize camera before get supported resolutions.")
    emptyMap()
  }

  private fun runTimer(block: () -> Unit) {
    runningTimer = lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
      when (timer) {
        CameraTimer.OFF -> block()
        else -> {
          for (i in timer.seconds downTo 1) {
            timerCallback?.onTimerChanged(i)
            delay(1000)
          }
          timerCallback?.onTimerChanged(0)
          block()
        }
      }
    }
  }
}