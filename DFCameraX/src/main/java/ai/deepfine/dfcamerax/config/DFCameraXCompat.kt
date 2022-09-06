package ai.deepfine.dfcamerax.config

import ai.deepfine.dfcamerax.usecases.DFCameraXManager
import ai.deepfine.dfcamerax.usecases.DFCameraXManagerImpl
import ai.deepfine.dfcamerax.utils.CameraMode
import ai.deepfine.dfcamerax.utils.CameraTimer
import android.content.Context
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.video.Quality
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import java.io.File

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-09-05
 * @version 1.0.0
 */
interface DFCameraXCompat : DFCameraXPreviewCompat, DFCameraXImageCompat, DFCameraXVideoCompat {
  fun startCamera()
  fun setCameraMode(cameraMode: CameraMode)
  fun changeCameraMode(cameraMode: CameraMode)
  fun enableAutoRotation(enabled: Boolean)

  // 전면, 후면 카메라 설정
  var lensFacing: CameraSelector

  // 플래시 설정
  var flashMode: Int

  // 타이머 설정
  var timer: CameraTimer
  fun setOnTimerCallback(callback: CameraTimer.Callback)

  fun getSupportedResolutions(): Map<Quality, Size>

  //================================================================================================
  // Builder
  //================================================================================================
  class Builder(lifecycleOwner: LifecycleOwner, context: Context) {
    private val compat: DFCameraXCompat = DFCameraXCompatImpl(lifecycleOwner, context)

    fun setCameraMode(cameraMode: CameraMode): Builder {
      compat.setCameraMode(cameraMode)
      return this
    }

    fun setPreviewView(previewView: PreviewView): Builder {
      compat.setPreviewView(previewView)
      return this
    }

    fun setOnImageSavedCallback(callback: ImageCapture.OnImageSavedCallback): Builder {
      compat.setOnImageSavedCallback(callback)
      return this
    }

    fun setOnVideoEventListener(callback: Consumer<VideoRecordEvent>): Builder {
      compat.setOnVideoRecordEventListener(callback)
      return this
    }

    fun setImageOutputDirectory(outputDirectory: String): Builder {
      compat.setImageOutputDirectory(outputDirectory)
      return this
    }

    fun setVideoOutputDirectory(outputDirectory: String): Builder {
      with(File(outputDirectory)) {
        if (!exists())
          mkdir()
      }
      compat.setVideoOutputDirectory(outputDirectory)
      return this
    }

    fun enableAutoRotation(enable: Boolean): Builder {
      compat.enableAutoRotation(enable)
      return this
    }

    fun setPreviewTargetResolution(targetResolution: Size): Builder {
      compat.setPreviewTargetResolution(targetResolution)
      return this
    }

    fun setImageCaptureTargetResolution(targetResolution: Size): Builder {
      compat.setImageCaptureTargetResolution(targetResolution)
      return this
    }

    fun setVideoQuality(quality: Quality, higherQualityOrLowerThan: Quality? = null): Builder {
      compat.setVideoQuality(quality, higherQualityOrLowerThan)
      return this
    }

    fun setOnPreviewStreamCallback(lifecycleOwner: LifecycleOwner, onStreamStateChanged: (PreviewView.StreamState) -> Unit): Builder {
      compat.setOnPreviewStreamStateCallback(lifecycleOwner, onStreamStateChanged)
      return this
    }

    fun build(): DFCameraXManager = DFCameraXManagerImpl(compat)
  }
}