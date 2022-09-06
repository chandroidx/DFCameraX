# DFCameraX  [![](https://jitpack.io/v/yc-park/DFCameraX.svg)](https://jitpack.io/#yc-park/DFCameraX)

### 초기화

```groovy
    allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
    ////////////////////////////////////////////////////////////

    dependencies {
        implementation 'com.github.yc-park:DFCameraX:latest-release'
    }
}

def camerax_version = "1.1.0-beta01"
implementation "androidx.camera:camera-core:${camerax_version}"
implementation "androidx.camera:camera-camera2:${camerax_version}"
implementation "androidx.camera:camera-lifecycle:${camerax_version}"
implementation "androidx.camera:camera-video:${camerax_version}"

implementation "androidx.camera:camera-view:${camerax_version}"
implementation "androidx.camera:camera-extensions:${camerax_version}"
```

```kotlin
cameraManager = DFCameraXCompat.Builder(this, this)
  // Camera Config
  .setCameraMode(CameraMode.Image) // 사진, 비디오 모드 설정
  // Preview Config
  .setPreviewView(binding.previewView) // 프리뷰 설정
  .setPreviewTargetResolution(Size(360, 640)) // 프리뷰 화질 지정
  .setOnPreviewStreamCallback(this, onStreamStateChanged)
  // Image Config
  .setImageCaptureTargetResolution(Size(1080, 1920)) // 이미지 캡쳐 화질 지정
  .setImageOutputDirectory(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()) // 이미지 저장 폴더 설정 (Default: DCIM)
  .setOnImageSavedCallback(onImageSavedCallback) // 이미지 저장 콜백 
  // Video Config
  .setVideoQuality(Quality.FHD, Quality.HD) // 비디오 화질 지정 
                                            // quality: 기본 캡쳐 화질 [Default: FHD] 
                                            // higherQualityOrLowerThan : quality가 적용되지 않는 기기일 경우 적용할 quality[Default: HD]
  .setVideoOutputDirectory(getExternalFilesDir(Environment.DIRECTORY_MOVIES).toString()) // 동영상 저장 폴더 설정 (Default: Movie) 
  .setOnVideoEventListener(onVideoEventListener) // 동영상 이벤트
  .build()


cameraManager.startCamera() // 카메라 실행
```

### 사용법

```kotlin
cameraManager.changeCameraMode(CameraMode.Image or CameraMode.Video) // 사진, 비디오 모드 변경
cameraManager.lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA or CameraSelector.DEFAULT_BACK_CAMERA // 전면, 후면 카메라 설정
cameraManager.timer = CameraTimer.OFF // 타이머 설정
cameraManager.setOnTimerCallback(timerCallback : CameraTimer. Callback) // 카메라 타이머 콜백
cameraManager.flashMode = DFCameraXCompat.FLASH_MODE_ON or DFCameraXCompat.FLASH_MODE_OFF // 플래시 설정
cameraManager.getSupportedResolutions() // 지원하는 비디오 화질 가져옴 (previewStreamState가 Stream일 경우 호출 가능)


cameraManager.takePicture() // 사진 촬영
cameraManager.recordVideo() // 동영상 촬영
cameraManager.stopRecording() // 동영상 촬영 중지
```