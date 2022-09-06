package ai.deepfine.dfcamerax.config

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
interface DFCameraXHandler {
  fun startCamera()
  fun setPreviewView(previewView: PreviewView)
  fun setCameraMode(cameraMode: CameraMode)
  fun changeCameraMode(cameraMode: CameraMode)

  fun enableAutoRotation(enabled: Boolean)

  // 프리뷰 화질 설정
  fun setPreviewTargetResolution(targetResolution: Size)

  // 이미지 캡쳐 사이즈 설정
  fun setImageCaptureTargetResolution(targetResolution: Size)

  // 동영상 캡쳐 사이즈 설정
  // higherQualityOrLowerThan -> quality가 적용되지 않을 경우 대신 적용되는 변수
  fun setVideoQuality(quality: Quality, higherQualityOrLowerThan: Quality?)

  // 파일 저장 경로
  fun setImageOutputDirectory(path: String)
  fun setVideoOutputDirectory(path: String)

  fun setOnImageSavedCallback(callback: ImageCapture.OnImageSavedCallback)
  fun setOnVideoSavedCallback(callback: Consumer<VideoRecordEvent>)
  fun setOnPreviewStreamStateCallback(lifecycleOwner: LifecycleOwner, onStreamStateChanged: (PreviewView.StreamState) -> Unit)

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

  fun getSupportedResolutions(): Map<Quality, Size>

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

    fun setOnVideoSavedCallback(callback: Consumer<VideoRecordEvent>): Builder {
      handler.setOnVideoSavedCallback(callback)
      return this
    }

    fun setImageOutputDirectory(outputDirectory: String): Builder {
      handler.setImageOutputDirectory(outputDirectory)
      return this
    }

    fun setVideoOutputDirectory(outputDirectory: String): Builder {
      with(File(outputDirectory)) {
        if (!exists())
          mkdir()
      }
      handler.setVideoOutputDirectory(outputDirectory)
      return this
    }

    fun enableAutoRotation(enable: Boolean): Builder {
      handler.enableAutoRotation(enable)
      return this
    }

    fun setPreviewTargetResolution(targetResolution: Size): Builder {
      handler.setPreviewTargetResolution(targetResolution)
      return this
    }

    fun setImageCaptureTargetResolution(targetResolution: Size): Builder {
      handler.setImageCaptureTargetResolution(targetResolution)
      return this
    }

    fun setVideoQuality(quality: Quality, higherQualityOrLowerThan: Quality? = null): Builder {
      handler.setVideoQuality(quality, higherQualityOrLowerThan)
      return this
    }

    fun setOnPreviewStreamCallback(lifecycleOwner: LifecycleOwner, onStreamStateChanged: (PreviewView.StreamState) -> Unit): Builder {
      handler.setOnPreviewStreamStateCallback(lifecycleOwner, onStreamStateChanged)
      return this
    }

    fun build(): DFCameraXHandler = handler
  }
}