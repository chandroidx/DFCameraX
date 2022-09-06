package ai.deepfine.dfcamerax.config

import android.util.Size
import androidx.camera.core.ImageCapture

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-09-06
 * @version 1.0.0
 */
interface DFCameraXImageCompat {
  // 이미지 캡쳐 사이즈 설정
  fun setImageCaptureTargetResolution(targetResolution: Size)

  // 파일 저장 경로
  fun setImageOutputDirectory(path: String)

  // 저장 결과 콜백
  fun setOnImageSavedCallback(callback: ImageCapture.OnImageSavedCallback)

  // 사진 촬영
  fun takePicture()
}