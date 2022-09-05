package ai.deepfine.dfcamerax.config

import ai.deepfine.dfcamerax.utils.CameraTimer
import ai.deepfine.dfcamerax.utils.MainExecutor
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutionException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-09-05
 * @version 1.0.0
 */
class DFCameraXHandlerImpl(private val lifecycleOwner: LifecycleOwner, private val context: Context) : DFCameraXHandler {
  companion object {
    private const val TAG = "DFCameraX"

    private const val RATIO_4_3_VALUE = 4.0 / 3.0 // aspect ratio 4x3
    private const val RATIO_16_9_VALUE = 16.0 / 9.0 // aspect ratio 16x9
  }

  private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
  private lateinit var cameraProvider: ProcessCameraProvider
  private lateinit var preview: Preview
  private lateinit var camera: Camera

  private lateinit var previewView: PreviewView
  private lateinit var imageCapture: ImageCapture
  private lateinit var imageAnalyzer: ImageAnalysis

  private var _lensFacing: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
  private var _timer: CameraTimer = CameraTimer.OFF
  private var timerCallback: CameraTimer.Callback? = null
  private var targetResolution: Size? = null

  private var outputDirectory: String? = null


  override fun startCamera() {
    cameraProviderFuture.addListener({
      fetchCameraProvider {
        return@fetchCameraProvider
      }
      configurePreview()
      configureImageCapture()
      configureImageAnalysis()

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

  private fun configurePreview() {
    preview = Preview.Builder().apply {
      setTargetRotation(getRotation())
      targetResolution?.let {
        setTargetResolution(it)
      } ?: setTargetAspectRatio(getAspectRatio())
    }.build()
  }

  private fun configureImageCapture() {
    imageCapture = ImageCapture.Builder().apply {
      setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
      setFlashMode(_flashMode)
      targetResolution?.let {
        setTargetResolution(it)
      } ?: setTargetAspectRatio(getAspectRatio())
      setTargetRotation(getRotation())
    }.build()
  }

  private fun configureImageAnalysis() {
    imageAnalyzer = ImageAnalysis.Builder().apply {
      setTargetRotation(getRotation())
      targetResolution?.let {
        setTargetResolution(it)
      } ?: setTargetAspectRatio(getAspectRatio())

      setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    }.build()
  }

  private fun bindToLifecycle() {
    try {
      camera = cameraProvider.bindToLifecycle(
        lifecycleOwner,
        lensFacing,
        preview,
        imageCapture,
        imageAnalyzer
      )

      preview.setSurfaceProvider(previewView.surfaceProvider)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to bind use cases : $e")
    }
  }

  private fun getAspectRatio(): Int {
    val metrics = DisplayMetrics().also {
      previewView.display.getRealMetrics(it)
    }

    val width = metrics.widthPixels
    val height = metrics.heightPixels

    val previewRatio = max(width, height).toDouble() / min(width, height)
    if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
      return AspectRatio.RATIO_4_3
    }
    return AspectRatio.RATIO_16_9
  }

  private fun getRotation(): Int = previewView.display.rotation


  private val orientationEventListener = object : OrientationEventListener(context) {
    override fun onOrientationChanged(orientation: Int) {
      val rotation = when (orientation) {
        in 45..134 -> Surface.ROTATION_270
        in 135..224 -> Surface.ROTATION_180
        in 225..314 -> Surface.ROTATION_90
        else -> Surface.ROTATION_0
      }

      imageCapture.targetRotation = rotation
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


//  override fun setLensFacing(cameraSelector: CameraSelector) {
//    this.lensFacing = cameraSelector
//    startCamera()
//  }

  private var _flashMode: Int = ImageCapture.FLASH_MODE_OFF
  override var flashMode: Int = _flashMode
    get() = _flashMode
    set(value) {
      _flashMode = value
      if (::imageCapture.isInitialized) {
        imageCapture.flashMode = value
      }

      if (::camera.isInitialized) {
        camera.cameraControl.enableTorch(value != ImageCapture.FLASH_MODE_OFF)
      }
      field = value
    }

  override fun setTargetResolution(size: Size) {
    this.targetResolution = size

    startCamera()
  }

  override fun setOutputDirectory(path: String) {
    this.outputDirectory = path
  }

  override var timer: CameraTimer = _timer
    get() = _timer
    set(value) {
      _timer = timer
      field = value
    }

  override fun setOnTimerCallback(callback: CameraTimer.Callback) {
    this.timerCallback = callback
  }

  override fun takePicture() {
    lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
      when (timer) {
        CameraTimer.OFF -> captureImage()
        else -> {
          for (i in timer.seconds downTo 1) {
            timerCallback?.onTimerChanged(i)
            delay(1000)
          }
          timerCallback?.onTimerChanged(0)
          captureImage()
        }
      }
    }
  }

  private fun captureImage() {
    val outputOptions = if (outputDirectory == null) {
      optionsOnGallery()
    } else {
      optionsOnSpecificDirectory(outputDirectory!!)
    }.build()


    imageCapture.takePicture(
      outputOptions,
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) context.mainExecutor else MainExecutor(),
      callback
    )
  }

  private fun optionsOnGallery(): ImageCapture.OutputFileOptions.Builder {
    val directory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      Environment.DIRECTORY_DCIM
    } else {
      "${context.getExternalFilesDir(Environment.DIRECTORY_DCIM)}"
    }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis())
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
      }

      val contentResolver = context.contentResolver

      val contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

      ImageCapture.OutputFileOptions.Builder(contentResolver, contentUri, contentValues)
    } else {
      optionsOnSpecificDirectory(directory)
    }
  }

  private fun optionsOnSpecificDirectory(directory: String): ImageCapture.OutputFileOptions.Builder {
    File(directory).mkdirs()
    val file = File(directory, "${System.currentTimeMillis()}.png")
    return ImageCapture.OutputFileOptions.Builder(file)
  }

  private lateinit var callback: ImageCapture.OnImageSavedCallback
  override fun setOnImageSavedCallback(callback: ImageCapture.OnImageSavedCallback) {
    this.callback = callback
  }
}