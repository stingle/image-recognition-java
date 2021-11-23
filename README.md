**Stingle Face Recognition**

*A library for recognising similar faces from supplied images using TensorFlow open source library*

![](https://avatars.githubusercontent.com/u/69607920?s=200&v=4)

**How to include?**

```
git clone https://github.com/stingle/image-recognition-java.git
cd image-recognition-java
./gradlew build
```
At first you need to find generated .aar file in the build outputs. The .aar file path is the following:
```
image-recognition-java/imagerecognition/build/outputs/aar/stingle-imagerecognition-1.0.0.aar
```
***Add your .aar as a dependency using Android Studio.***

- put the .aar file into app/libs folder
- add the dependency in file->project struture->dependencies section

If you need help, please follow this link:
[Add your AAR or JAR as a dependency](https://developer.android.com/studio/projects/android-library#psd-add-aar-jar-dependency)

After adding the dependency you will find the module dependency in your app level build.gradle file.
```
 implementation files('libs/stingle-imagerecognition-1.0.0.aar')
```

**How do I use?**

Find or train the best TFLite model file for you and add into your app under assets folder.
[there are several already trained models to use](https://tfhub.dev/s?module-type=image-feature-vector)

You can execute this code to get an object recognition instance.

```java
// getting detected objects from the video file located on the raw folder.
try {
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sample_1);
        Set<StingleImageRecognition.DetectionResult> results =
                      imageDetector.runVideoObjectDetection(videoUri, 2_000L);
} catch (Exception e) {
        Log.d(TAG, e.getMessage());
}

// getting detected objects from the specific video file path or media uri.
try {
        Set<StingleImageRecognition.DetectionResult> results =
                      imageDetector.runVideoObjectDetection(videoFilePath, 1_000L);
} catch (Exception e) {
        Log.d(TAG, e.getMessage());
}

// getting detected objects from the specific video file path or media uri with the factor of skipping video frames.
try {
        Set<StingleImageRecognition.DetectionResult> results =
                      imageDetector.runVideoObjectDetection(videoFilePath, 0.5f);
} catch (Exception e) {
        Log.d(TAG, e.getMessage());
}

```
Runing object detection on the gif file:
```java
// getting detected objects from the specific gif file with the factor of skipping gif frames.
try {
        Uri gifUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.gif_sample_1);
        imageDetector.runGifObjectDetection(gifUri, 0.5f, results -> {
            Log.d(TAG, results);
        });
} catch (Exception e) {
        Log.d(TAG, e.getMessage());
}
```

***An important point: try to always run the object detection functions on the background thread to not to block the main/UI thread.***

**Compatibility**

Minimum Android SDK: Stingle image recognition requires a minimum API level of 23.

**Also you can see**

[Demo project](https://github.com/stingle/image-recognition-java/tree/main/app) in github

**License**

Apache 2.0. See the [LICENSE](https://github.com/stingle/image-recognition-java/blob/main/LICENSE). file for details.
