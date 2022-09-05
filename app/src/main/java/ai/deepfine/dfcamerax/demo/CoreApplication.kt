package ai.deepfine.dfcamerax.demo

import ai.deepfine.dfcamerax.view.DFCameraXConfig
import android.app.Application
import androidx.camera.core.CameraSelector

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-09-05
 * @version 1.0.0
 */
class CoreApplication : Application(), DFCameraXConfig {
  override val availableCamerasLimiter: CameraSelector? = null
}