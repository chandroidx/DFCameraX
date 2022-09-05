package ai.deepfine.dfcamerax.view

import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraXConfig

/**
 * @Description Implement to Application class
 * @author yc.park (DEEP.FINE)
 * @since 2022-09-05
 * @version 1.0.0
 */
interface DFCameraXConfig : CameraXConfig.Provider {
  /**
   * 기본으로 후면 or 전면 카메라만 사용하는 경우 지정
   *
   * CameraSelector.DEFAULT_BACK_CAMERA
   * CameraSelector.DEFAULT_FRONT_CAMERA
   */
  val availableCamerasLimiter: CameraSelector?

  override fun getCameraXConfig(): CameraXConfig = CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig()).apply {
    availableCamerasLimiter?.let(::setAvailableCamerasLimiter)
    setMinimumLoggingLevel(Log.ERROR).build()
  }.build()
}