package ai.deepfine.dfcamerax.config

import androidx.camera.video.Quality
import androidx.camera.video.VideoRecordEvent
import androidx.core.util.Consumer

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-09-06
 * @version 1.0.0
 */
interface DFCameraXVideoCompat {
  // 동영상 캡쳐 사이즈 설정
  // higherQualityOrLowerThan -> quality가 적용되지 않을 경우 대신 적용되는 변수
  fun setVideoQuality(quality: Quality, higherQualityOrLowerThan: Quality?)

  // 파일 저장 경로
  fun setVideoOutputDirectory(path: String)

  // 비디오 촬영 콜백 (start, pause, resume, etc...)
  fun setOnVideoRecordEventListener(listener: Consumer<VideoRecordEvent>)

  // 비디오 촬영 시작
  fun recordVideo()

  // 비디오 촬영 종료
  fun stopRecording()
}