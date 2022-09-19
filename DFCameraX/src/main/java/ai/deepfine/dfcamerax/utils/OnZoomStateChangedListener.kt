package ai.deepfine.dfcamerax.utils

import androidx.camera.core.ZoomState

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-09-19
 * @version 1.0.0
 */
fun interface OnZoomStateChangedListener {
  fun onZoomStateChanged(zoomState: ZoomState)
}