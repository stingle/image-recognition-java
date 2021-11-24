**Stingle Object Recognition**

*Library for detecting objects and recognising faces from org.stingle.ai.image and video using TensorFlow open source library*

![](https://avatars.githubusercontent.com/u/69607920?s=200&v=4)

**How to include?**

```
git clone https://github.com/stingle/org.stingle.ai.image-recognition-java.git
cd org.stingle.ai.image-recognition-java
./gradlew build
```
At first you need to find generated .aar file in the build outputs. The .aar file path is the following:
```
org.stingle.ai.image-recognition-java/imagerecognition/build/outputs/aar/stingle-imagerecognition-1.0.0.aar
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
[there are several already trained models to use](https://tfhub.dev/tensorflow/collections/lite/task-library/object-detector/1)

You can execute this code to get an object recognition instance.

```java
 StingleImageRecognition imageDetector = new StingleImageRecognition.Builder(this)
                .maxResults(5)
                .modelPath("model.tflite")
                .scoreThreshold(0.5f)
                .build();
 
 // runnning object detection on any bitmap and get the results list.
 try {
        List<StingleImageRecognition.DetectionResult> results =
                imageDetector.runObjectDetection(bitmap);
        Log.d(TAG, results.toString());
} catch (Exception e) {
        Log.d(TAG, e.getMessage());
}

// runnning object detection on any bitmap and draw the results on the imageview.
try {
        List<StingleImageRecognition.DetectionResult> results =
                imageDetector.runObjectDetection(bitmap, imageView);
        Log.d(TAG, results.toString());
} catch (Exception e) {
        Log.d(TAG, e.getMessage());
}

// preparing bitmap before passing to org.stingle.ai.image detection library:
Bitmap rotatedBitmap = imageDetector.prepareBitmap(currentPhotoPath); // using best practice sizes by TFLite library
or
Bitmap rotatedBitmap = imageDetector.prepareBitmap(currentPhotoPath, 300, 400); // width and height specification
or
Bitmap rotatedBitmap = imageDetector.prepareBitmap(currentPhotoPath, inputImageView); // retrieving sizes from passed imageview
```

Runing object detection on the video file:

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

For face detection

```java
FaceRecogniser faceRecogniser = new FaceRecogniser();
faceRecogniser.init(context, "facenet.tflite");

// As facerecognizer does not store any data, you need to supply it with previous results.
// It accepts any collection.
Set<Person> personData = ...

FaceRecogniser.Result result = faceRecogniser.recognise(bitmap, personData).get();

// Then store accordingly
personData.addAll(result.personList);
```

***An important point: try to always run the object detection functionns on the background thread to not to block the main/UI thread.***

**Compatibility**

Minimum Android SDK: Stingle org.stingle.ai.image recognition requires a minimum API level of 23.

**Also you can see**

[Demo project](https://github.com/stingle/image-recognition-java/tree/main/app) in github

**License**

Apache 2.0. See the [LICENSE](https://github.com/stingle/image-recognition-java/blob/main/LICENSE). file for details.
