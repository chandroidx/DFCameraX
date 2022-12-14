package ai.deepfine.dfcamerax.usecases

import ai.deepfine.dfcamerax.config.DFCameraXCompat
import ai.deepfine.dfcamerax.utils.CameraMode
import ai.deepfine.dfcamerax.utils.CameraTimer
import ai.deepfine.dfcamerax.utils.OnZoomStateChangedListener
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ZoomState
import androidx.camera.video.Quality
import androidx.lifecycle.LiveData

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-09-06
 * @version 1.0.0
 */
internal class DFCameraXManagerImpl(private val dfCameraXCompat: DFCameraXCompat) : DFCameraXManager {
  override fun startCamera() {
    dfCameraXCompat.startCamera()
  }

  override fun stopCamera() {
    dfCameraXCompat.stopCamera()
  }

  override var cameraMode: Int = dfCameraXCompat.cameraMode
    get() = dfCameraXCompat.cameraMode
    set(value) {
      dfCameraXCompat.cameraMode = value
      field = value
    }

  override var lensFacing: CameraSelector = dfCameraXCompat.lensFacing
    get() = dfCameraXCompat.lensFacing
    set(value) {
      dfCameraXCompat.lensFacing = value
      field = value
    }

  override var timer: CameraTimer = dfCameraXCompat.timer
    get() = dfCameraXCompat.timer
    set(value) {
      dfCameraXCompat.timer = value
      field = value
    }

  override fun setOnTimerCallback(timerCallback: CameraTimer.Callback) {
    dfCameraXCompat.setOnTimerCallback(timerCallback)
  }

  override fun cancelTimer() {
    dfCameraXCompat.cancelTimer()
  }

  override fun isTimerRunning(): Boolean =
    dfCameraXCompat.isTimerRunning()

  override var flashMode: Int = dfCameraXCompat.flashMode
    get() = dfCameraXCompat.flashMode
    set(value) {
      dfCameraXCompat.flashMode = value
      field = value
    }


  override fun getSupportedResolutions(): Map<Quality, Size> = dfCameraXCompat.getSupportedResolutions()

  override fun setZoomRatio(zoomRatio: Float) {
    dfCameraXCompat.setZoomRatio(zoomRatio)
  }

  override fun setLinearZoom(linearZoom: Float) {
    dfCameraXCompat.setLinearZoom(linearZoom)
  }

  override fun takePicture() {
    dfCameraXCompat.takePicture()
  }

  override fun recordVideo() {
    dfCameraXCompat.recordVideo()
  }

  override fun stopRecording() {
    dfCameraXCompat.stopRecording()
  }
}