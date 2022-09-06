# DFCameraX

### 초기화

```groovy
def camerax_version = "1.1.0-beta01"
implementation "androidx.camera:camera-core:${camerax_version}"
implementation "androidx.camera:camera-camera2:${camerax_version}"
implementation "androidx.camera:camera-lifecycle:${camerax_version}"
implementation "androidx.camera:camera-video:${camerax_version}"

implementation "androidx.camera:camera-view:${camerax_version}"
implementation "androidx.camera:camera-extensions:${camerax_version}"
```

```kotlin
cameraHandler = DFCameraXHandler.Builder(this, this)
  .setCameraMode(CameraMode.Image) // 사진, 비디오 모드 설정
  .setPreviewView(binding.previewView) // 프리뷰 설정
  .setImageOutputDirectory(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()) // 이미지 저장 폴더 설정 (Default: DCIM)
  .setVideoOutputDirectory(getExternalFilesDir(Environment.DIRECTORY_MOVIES).toString()) // 동영상 저장 폴더 설정 (Default: Movie) 
  .setOnImageSavedCallback(onImageSavedCallback) // 이미지 저장 콜백 
  .setOnVideoSavedCallback(onVideoSavedCallback) // 동영상 저장 콜백
  .setPreviewTargetResolution(Size(width, height)) // 프리뷰 화질 지정
  .setImageCaptureTargetResolution(Size(width, height)) // 이미지 캡쳐 화질 지정
  .setVideoQuality(quality, higherQualityOrLowerThan) // 비디오 화질 지정 
                                                      // quality: 기본 캡쳐 화질 [Default: FHD] 
                                                      // higherQualityOrLowerThan : quality가 적용되지 않는 기기일 경우 적용할 quality[Default: HD]
  .setOnPreviewStreamCallback(lifecycleOwner, onStreamStateChanged) // 프리뷰 상태 (STREAMING, IDLE)
  .build()

cameraHandler.startCamera() // 카메라 실행
```

### 사용법

```kotlin
cameraHandler.changeCameraMode(CameraMode.Image or CameraMode.Video) // 사진, 비디오 모드 변경

cameraHandler.getSupportedResolutions() // 지원하는 비디오 화질 가져옴 (previewStreamState가 Stream일 경우 호출 가능)

cameraHandler.timer = CameraTimer.OFF // 타이머 설정

cameraHandler.flashMode = ImageCapture.FLASH_MODE_ON or ImageCapture.FLASH_MODE_OFF // 플래시 설정
cameraHandler.lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA or CameraSelector.DEFAULT_BACK_CAMERA // 전면, 후면 카메라 설정


cameraHandler.takePicture() // 사진 촬영
cameraHandler.recordVideo() // 동영상 촬영
cameraHandler.stopRecording() // 동영상 촬영 중지
```