package ai.deepfine.dfcamerax.utils

import androidx.camera.core.ZoomState

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-09-19
 * @version 1.0.0
 */
data class DFZoomState(
  val zoomRatio: Float,
  val maxZoomRatio: Float,
  val minZoomRatio: Float,
  val linearZoom: Float
) {
  companion object {
    fun fromZoomState(zoomState: ZoomState) = DFZoomState(
      zoomState.zoomRatio,
      zoomState.maxZoomRatio,
      zoomState.minZoomRatio,
      zoomState.linearZoom
    )
  }
}

fun interface OnZoomStateChangedListener {
  fun onZoomStateChanged(zoomState: DFZoomState)
}