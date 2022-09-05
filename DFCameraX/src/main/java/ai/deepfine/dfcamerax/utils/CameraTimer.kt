package ai.deepfine.dfcamerax.utils

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-09-05
 * @version 1.0.0
 */
enum class CameraTimer(val seconds: Int) {
  OFF(0),
  S3(3),
  S5(5),
  S10(10);

  fun interface Callback {
    fun onTimerChanged(leftSeconds: Int)
  }
}