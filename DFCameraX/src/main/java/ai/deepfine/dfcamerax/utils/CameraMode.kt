package ai.deepfine.dfcamerax.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-09-05
 * @version 1.0.0
 */
@SuppressLint("RestrictedApi")
sealed interface CameraMode {
  fun createPreview(previewView: PreviewView, targetResolution: Size?) = Preview.Builder().apply {
    setTargetRotation(getRotation(previewView))
    targetResolution?.let { resolution ->
      setTargetResolution(resolution)
    } ?: setTargetAspectRatio(getAspectRatio(previewView))
  }.build()

  fun setTargetRotation(rotation: Int)

  //================================================================================================
  // Image
  //================================================================================================
  object Image : CameraMode {
    lateinit var imageCapture: ImageCapture
    lateinit var imageAnalysis: ImageAnalysis

    fun createUseCases(previewView: PreviewView, targetResolution: Size?): List<UseCase> {
      return listOf(
        createImageCapture(previewView, targetResolution),
        createImageAnalysis(previewView, targetResolution)
      )
    }

    private fun createImageCapture(previewView: PreviewView, targetResolution: Size?): ImageCapture {
      imageCapture = ImageCapture.Builder().apply {
        setTargetRotation(getRotation(previewView))
        setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
        targetResolution?.let { resolution ->
          setTargetResolution(resolution)
        } ?: setTargetAspectRatio(getAspectRatio(previewView))
      }.build()

      return imageCapture
    }

    private fun createImageAnalysis(previewView: PreviewView, targetResolution: Size?): ImageAnalysis {
      imageAnalysis = ImageAnalysis.Builder().apply {
        setTargetRotation(getRotation(previewView))
        targetResolution?.let {
          setTargetResolution(it)
        } ?: setTargetAspectRatio(getAspectRatio(previewView))

        setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
      }.build()

      return imageAnalysis
    }

    override fun setTargetRotation(rotation: Int) {
      imageCapture.targetRotation = rotation
    }

    fun takePicture(context: Context, outputDirectory: String?, callback: ImageCapture.OnImageSavedCallback) {
      imageCapture.takePicture(
        getOutputFileOptions(context, outputDirectory),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) context.mainExecutor else MainExecutor(),
        callback
      )
    }

    private fun getOutputFileOptions(context: Context, outputDirectory: String?): ImageCapture.OutputFileOptions {
      return when (outputDirectory == null) {
        true -> optionsImageOnGallery(context)
        false -> optionsImageOnSpecificDirectory(outputDirectory)
      }.build()
    }

    private fun optionsImageOnGallery(context: Context): ImageCapture.OutputFileOptions.Builder {
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
        optionsImageOnSpecificDirectory(directory)
      }
    }

    private fun optionsImageOnSpecificDirectory(directory: String): ImageCapture.OutputFileOptions.Builder {
      File(directory).mkdirs()
      val file = File(directory, "${System.currentTimeMillis()}.png")
      return ImageCapture.OutputFileOptions.Builder(file)
    }
  }

  //================================================================================================
  // Video
  //================================================================================================
  object Video : CameraMode {
    lateinit var videoCapture: VideoCapture<Recorder>
    private var recording: Recording? = null

    fun createUseCases(quality: Quality?, higherQualityOrLowerThan: Quality?): List<UseCase> {
      return listOf(createVideoCapture(quality, higherQualityOrLowerThan))
    }

    @SuppressLint("RestrictedApi")
    private fun createVideoCapture(quality: Quality?, higherQualityOrLowerThan: Quality?): VideoCapture<Recorder> {
      videoCapture = VideoCapture.withOutput(createRecorder(quality, higherQualityOrLowerThan))

      return videoCapture
    }

    private fun createRecorder(quality: Quality?, higherQualityOrLowerThan: Quality?): Recorder {
      return Recorder.Builder()
        .setQualitySelector(createQualitySelector(quality, higherQualityOrLowerThan))
        .build()
    }

    private fun createQualitySelector(quality: Quality?, higherQualityOrLowerThan: Quality?): QualitySelector {
      return QualitySelector.from(
        quality ?: Quality.FHD,
        FallbackStrategy.higherQualityOrLowerThan(higherQualityOrLowerThan ?: Quality.HD)
      )
    }

    @SuppressLint("RestrictedApi")
    override fun setTargetRotation(rotation: Int) {
      videoCapture.targetRotation = rotation
    }

    @SuppressLint("MissingPermission")
    fun recordVideo(context: Context, outputDirectory: String?, callback: Consumer<VideoRecordEvent>) {
      recording = when (outputDirectory == null) {
        true -> {
          videoCapture.output
            .prepareRecording(context, optionsVideoOnGallery(context))
        }
        false -> videoCapture.output
          .prepareRecording(context, optionsVideoOnSpecificDirectory(outputDirectory))
      }.withAudioEnabled()
        .start(ContextCompat.getMainExecutor(context), callback)
    }

    fun stopRecording() {
      recording?.stop()
      recording = null
    }

    private fun optionsVideoOnGallery(context: Context): MediaStoreOutputOptions {
      val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis())
        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
      }

      val contentResolver = context.contentResolver

      val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

      return MediaStoreOutputOptions.Builder(contentResolver, contentUri).apply {
        setContentValues(contentValues)
      }.build()
    }

    private fun optionsVideoOnSpecificDirectory(directory: String): FileOutputOptions {
      val file = File(directory, "${System.currentTimeMillis()}.mp4")
      return FileOutputOptions.Builder(file).build()
    }
  }

  //================================================================================================
  // Functions
  //================================================================================================
  fun getRotation(previewView: PreviewView) = previewView.display.rotation

  fun getAspectRatio(previewView: PreviewView): Int {

    val width = previewView.width
    val height = previewView.height

    val previewRatio = max(width, height).toDouble() / min(width, height)
    if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
      return AspectRatio.RATIO_4_3
    }
    return AspectRatio.RATIO_16_9
  }

  companion object {
    private const val RATIO_4_3_VALUE = 4.0 / 3.0 // aspect ratio 4x3
    private const val RATIO_16_9_VALUE = 16.0 / 9.0 // aspect ratio 16x9
  }
}