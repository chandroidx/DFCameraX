package ai.deepfine.dfcamerax.config

import ai.deepfine.dfcamerax.utils.CameraMode
import ai.deepfine.dfcamerax.utils.CameraTimer
import android.content.Context
import android.os.Build
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.VideoCapture
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-09-05
 * @version 1.0.0
 */
interface DFCameraXHandler {
  fun startCamera()
  fun setPreviewView(previewView: PreviewView)
  fun setCameraMode(cameraMode: CameraMode)
  fun changeCameraMode(cameraMode: CameraMode)

  fun enableAutoRotation(enabled: Boolean)

  // 화질 설정
  fun setTargetResolution(size: Size)

  // 파일 저장 경로
  fun setImageOutputDirectory(path: String)
  fun setVideoOutputDirectory(path: String)

  fun setOnImageSavedCallback(callback: ImageCapture.OnImageSavedCallback)
  fun setOnVideoSavedCallback(callback: VideoCapture.OnVideoSavedCallback)

  // 전면, 후면 카메라 설정
  var lensFacing: CameraSelector

  // 플래시 설정
  var flashMode: Int

  // 타이머 설정
  var timer: CameraTimer
  fun setOnTimerCallback(callback: CameraTimer.Callback)
  fun takePicture()
  fun recordVideo()
  fun stopRecording()

  class Builder(lifecycleOwner: LifecycleOwner, context: Context) {
    private val handler: DFCameraXHandler = DFCameraXHandlerImpl(lifecycleOwner, context)

    fun setCameraMode(cameraMode: CameraMode): Builder {
      handler.setCameraMode(cameraMode)
      return this
    }

    fun setPreviewView(previewView: PreviewView): Builder {
      handler.setPreviewView(previewView)
      return this
    }

    fun setOnImageSavedCallback(callback: ImageCapture.OnImageSavedCallback): Builder {
      handler.setOnImageSavedCallback(callback)
      return this
    }

    fun setOnVideoSavedCallback(callback: VideoCapture.OnVideoSavedCallback): Builder {
      handler.setOnVideoSavedCallback(callback)
      return this
    }

    fun setImageOutputDirectory(outputDirectory: String): Builder {
      handler.setImageOutputDirectory(outputDirectory)
      return this
    }

    fun setVideoOutputDirectory(outputDirectory: String): Builder {
      handler.setImageOutputDirectory(outputDirectory)
      return this
    }

    fun enableAutoRotation(enable: Boolean): Builder {
      handler.enableAutoRotation(enable)
      return this
    }

    fun setTargetResolution(size: Size): Builder {
      handler.setTargetResolution(size)
      return this
    }


    fun build(): DFCameraXHandler = handler
  }
}