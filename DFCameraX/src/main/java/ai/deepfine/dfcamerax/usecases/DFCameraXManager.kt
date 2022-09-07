package ai.deepfine.dfcamerax.usecases

import ai.deepfine.dfcamerax.utils.CameraMode
import ai.deepfine.dfcamerax.utils.CameraTimer
import android.util.Size
import androidx.annotation.IntDef
import androidx.camera.core.CameraSelector
import androidx.camera.video.Quality
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
  var cameraMode : CameraMode

  var lensFacing: CameraSelector
  var timer: CameraTimer
  fun setOnTimerCallback(timerCallback: CameraTimer.Callback)
  fun cancelTimer()
  fun isTimerRunning(): Boolean

  @FlashMode
  var flashMode: Int

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