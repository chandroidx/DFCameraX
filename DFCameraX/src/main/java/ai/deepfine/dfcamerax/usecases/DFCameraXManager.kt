package ai.deepfine.dfcamerax.usecases

import ai.deepfine.dfcamerax.utils.CameraMode
import ai.deepfine.dfcamerax.utils.CameraTimer
import ai.deepfine.dfcamerax.utils.OnZoomStateChangedListener
import android.util.Size
import androidx.annotation.IntDef
import androidx.camera.core.CameraSelector
import androidx.camera.core.ZoomState
import androidx.camera.video.Quality
import androidx.lifecycle.LiveData
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-09-06
 * @version 1.0.0
 */
interface DFCameraXManager {
  //================================================================================================
  // Camera
  //================================================================================================
  fun startCamera()
  var cameraMode: Int

  var lensFacing: CameraSelector
  var timer: CameraTimer
  fun setOnTimerCallback(timerCallback: CameraTimer.Callback)
  fun cancelTimer()
  fun isTimerRunning(): Boolean

  @FlashMode
  var flashMode: Int

  fun setZoomRatio(zoomRatio: Float)
  fun setLinearZoom(linearZoom: Float)

  fun getSupportedResolutions(): Map<Quality, Size>

  //================================================================================================
  // Image
  //================================================================================================
  fun takePicture()

  //================================================================================================
  // Video
  //================================================================================================
  fun recordVideo()
  fun stopRecording()


  @IntDef(FLASH_MODE_AUTO, FLASH_MODE_ON, FLASH_MODE_OFF)
  @Retention(RetentionPolicy.SOURCE)
  annotation class FlashMode

  companion object {
    const val FLASH_MODE_AUTO = 0
    const val FLASH_MODE_ON = 1
    const val FLASH_MODE_OFF = 2
  }
}