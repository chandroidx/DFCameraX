# DFCameraX  [![](https://jitpack.io/v/yc-park/DFCameraX.svg)](https://jitpack.io/#yc-park/DFCameraX)

### Initialize

```groovy
    compileSdkVersion 33

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

def camerax_version = "1.2.0-beta02"
implementation "androidx.camera:camera-core:${camerax_version}"
implementation "androidx.camera:camera-camera2:${camerax_version}"
implementation "androidx.camera:camera-lifecycle:${camerax_version}"
implementation "androidx.camera:camera-video:${camerax_version}"

implementation "androidx.camera:camera-view:${camerax_version}"
implementation "androidx.camera:camera-extensions:${camerax_version}"
```

### Configuration

```kotlin
cameraManager = DFCameraXCompat.Builder(this, this)
  // Camera Config
  .setCameraMode(CameraMode.Image) // Select the mode for capture "Image" / "Camera"
  .setOnZoomStateChangedListener(onZoomStateChangedListener) // Listener for zoom state
  // Preview Config
  .setPreviewView(binding.previewView) // Set preview
  .setPreviewTargetResolution(Size(360, 640)) // Set preview's resolution
  .setOnPreviewStreamCallback(this, onStreamStateChanged)
  // Image Config
  .setImageCaptureTargetResolution(Size(1080, 1920)) // Set resolution for image capture
  .setImageOutputDirectory(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()) // Set output directory for image capture (Default: DCIM)
  .setOnImageSavedCallback(onImageSavedCallback) // Set image save callback 
  // Video Config
  .setVideoQuality(Quality.FHD, Quality.HD) // Set resolution for image capture 
                                            // quality: target resolution [Default: FHD] 
                                            // higherQualityOrLowerThan : [Default: HD]
  .setVideoOutputDirectory(getExternalFilesDir(Environment.DIRECTORY_MOVIES).toString()) // Set ouput directory for video capture (Default: Movie) 
  .setOnVideoEventListener(onVideoEventListener) // Set video event callback
  .build()


cameraManager.startCamera() // start camera
```

### HOW TO USE

```kotlin
cameraManager.changeCameraMode(CameraMode.Image or CameraMode.Video) // Switch the mode for capture
cameraManager.lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA or CameraSelector.DEFAULT_BACK_CAMERA // Switch lens facing
cameraManager.timer = CameraTimer.OFF // Set timer
cameraManager.setOnTimerCallback(timerCallback : CameraTimer.Callback) // Set timer callback
cameraManager.flashMode = DFCameraXCompat.FLASH_MODE_ON or DFCameraXCompat.FLASH_MODE_OFF // Set flash
cameraManager.getSupportedResolutions() // Get supported resolutions for video capture (executable when previewStreamState == Stream)
cameraManager.setZoomRatio(zoomRatio : Float) // Set zoom ratio (minZoomRatio ~ maxZoomRatio)
cameraManager.setLinearZoom(linearZoom : Float) // Set linear zoom (0F ~ 1F)


cameraManager.takePicture() 
cameraManager.recordVideo() 
cameraManager.stopRecording()
```