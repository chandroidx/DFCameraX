package ai.deepfine.dfcamerax.config

import ai.deepfine.dfcamerax.utils.CameraTimer
import android.content.Context
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
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

  fun enableAutoRotation(enabled: Boolean)

  // 화질 설정
  fun setTargetResolution(size: Size)

  // 파일 저장 경로
  fun setOutputDirectory(path: String)
  fun setOnImageSavedCallback(callback: ImageCapture.OnImageSavedCallback)

  // 전면, 후면 카메라 설정
  var lensFacing: CameraSelector

  // 플래시 설정
  var flashMode: Int

  // 타이머 설정
  fun setTimer(timer: CameraTimer, callback: CameraTimer.Callback? = null)
  fun takePicture()

  class Builder(lifecycleOwner: LifecycleOwner, context: Context) {
    private val handler: DFCameraXHandler = DFCameraXHandlerImpl(lifecycleOwner, context)

    fun setPreviewView(previewView: PreviewView): Builder {
      handler.setPreviewView(previewView)
      return this
    }

    fun setOnImageSavedCallback(callback: ImageCapture.OnImageSavedCallback): Builder {
      handler.setOnImageSavedCallback(callback)
      return this
    }

    fun setOutputDirectory(outputDirectory: String): Builder {
      handler.setOutputDirectory(outputDirectory)
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