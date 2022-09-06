package ai.deepfine.dfcamerax.config

import android.util.Size
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-09-06
 * @version 1.0.0
 */
interface DFCameraXPreviewCompat {
  fun setPreviewView(previewView: PreviewView)

  // 프리뷰 화질 설정
  fun setPreviewTargetResolution(targetResolution: Size)

  // 프리뷰 상태 콜백
  fun setOnPreviewStreamStateCallback(lifecycleOwner: LifecycleOwner, onStreamStateChanged: (PreviewView.StreamState) -> Unit)
}