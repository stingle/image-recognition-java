package org.stingle.imagerecognition;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.widget.ImageView;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StingleImageRecognition {

    private static final String TAG = "Stingle - ODT";
    private static final float MAX_FONT_SIZE = 96F;

    private final Context context;
    private final String modelPath;
    private final int maxResults;
    private final float scoreThreshold;
    private final int numThreads;

    private ObjectDetector detector;

    private StingleImageRecognition(final Context context,
                                    final String modelPath,
                                    final int maxResults,
                                    final float scoreThreshold,
                                    final int numThreads) {
        this.context = context;
        this.modelPath = modelPath;
        this.maxResults = maxResults;
        this.scoreThreshold = scoreThreshold;
        this.numThreads = numThreads;

        setupDetection();
    }

    private void setupDetection() {
        try {
            ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder()
                    .setMaxResults(maxResults)
                    .setScoreThreshold(scoreThreshold)
                    .setNumThreads(numThreads)
                    .build();

            detector = ObjectDetector.createFromFileAndOptions(
                    context,
                    modelPath,
                    options
            );
        } catch (Exception e) {
            Log.d(TAG, "failed to create TF detection object");
        }
    }

    public static class Builder {

        private final Context context;
        private String modelPath = "model.tflite";
        private int maxResults = 5;
        private float scoreThreshold = 0.5f;
        private int numThreads = -1;

        public Builder(final Context context) {
            this.context = context;
        }

        public Builder modelPath(final String modelPath) {
            this.modelPath = modelPath;
            return this;
        }

        public Builder maxResults(final int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public Builder scoreThreshold(final float scoreThreshold) {
            this.scoreThreshold = scoreThreshold;
            return this;
        }

        public Builder numThreads(final int numThreads) {
            this.numThreads = numThreads;
            return this;
        }

        public StingleImageRecognition build() {
            return new StingleImageRecognition(context, modelPath, maxResults, scoreThreshold, numThreads);
        }
    }

    /**
     * Runs object detection on bitmap and returns detected objects list
     *
     * @param bitmap bitmap image to run object detection on
     * @return detected objects list
     * @throws IOException when accessing corrupted or TF model file or not finding the model file path
     */
    public List<Detection> runObjectDetection(Bitmap bitmap) throws IOException {
        TensorImage image = TensorImage.fromBitmap(bitmap);

        if (detector == null) {
            throw new IOException("failed to access TF model file");
        }

        List<Detection> results = detector.detect(image);
        debugPrint(results);
        return results;
    }

    /**
     * Runs object detection on bitmap and draw the output on provided image view
     *
     * @param bitmap         bitmap image to run object detection on
     * @param inputImageView imageview reference to draw detected objects bounding boxes into it
     * @return detected objects list
     * @throws IOException when accessing corrupted or TF model file or not finding the model file path
     */
    public List<Detection> runObjectDetection(Bitmap bitmap, ImageView inputImageView) throws IOException {
        TensorImage image = TensorImage.fromBitmap(bitmap);

        if (detector == null) {
            throw new IOException("failed to access TF model file");
        }
        // Step 3: Feed given image to the detector
        List<Detection> results = detector.detect(image);

        // Step 4: Parse the detection result and show it

        List<DetectionResult> finalResultsToDisplay = new ArrayList<>(results.size());

        for (Detection detection : results) {
            // Get the top-1 category and craft the display text
            Category category = detection.getCategories().get(0);
            String text = category.getLabel() + " " + (int) (category.getScore() * 100);

            // Create a data object to display the detection result
            finalResultsToDisplay.add(new DetectionResult(detection.getBoundingBox(), text));
        }

        // Draw the detection result on the bitmap and show it.
        Bitmap imgWithResult = drawDetectionResult(bitmap, finalResultsToDisplay);
        inputImageView.setImageBitmap(imgWithResult);

        return results;
    }

    /* Helper Methods */

    private void debugPrint(List<Detection> results) {
        for (int i = 0; i < results.size(); ++i) {
            RectF box = results.get(i).getBoundingBox();

            Log.d(TAG, "Detected object: " + i);
            Log.d(TAG, String.format("  boundingBox: (%.2f, %.2f) - (%.2f, %.2f)",
                    box.left, box.top, box.right, box.bottom));

            for (int j = 0; j < results.get(i).getCategories().size(); ++j) {
                Category category = results.get(i).getCategories().get(j);
                Log.d(TAG, String.format("    Label %d: %s", j, category.getLabel()));
                int confidence = (int) (category.getScore() * 100);
                Log.d(TAG, String.format("    Confidence: %d percentage", confidence));
            }
        }
    }

    private Bitmap drawDetectionResult(
            Bitmap bitmap,
            List<DetectionResult> detectionResults) {
        Bitmap outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(outputBitmap);
        Paint pen = new Paint();
        pen.setTextAlign(Paint.Align.LEFT);

        for (DetectionResult r : detectionResults) {
            // draw bounding box
            pen.setColor(Color.RED);
            pen.setStrokeWidth(8F);
            pen.setStyle(Paint.Style.STROKE);
            RectF box = r.boundingBox;
            canvas.drawRect(box, pen);

            // calculate the right font size
            Rect tagSize = new Rect(0, 0, 0, 0);
            pen.setStyle(Paint.Style.FILL_AND_STROKE);
            pen.setColor(Color.YELLOW);
            pen.setStrokeWidth(2F);

            pen.setTextSize(MAX_FONT_SIZE);
            pen.getTextBounds(r.text, 0, r.text.length(), tagSize);
            float fontSize = pen.getTextSize() * box.width() / tagSize.width();

            // adjust the font size so texts are inside the bounding box
            if (fontSize < pen.getTextSize()) pen.setTextSize(fontSize);

            float margin = (box.width() - tagSize.width()) / 2.0F;
            if (margin < 0F) margin = 0F;
            canvas.drawText(
                    r.text, box.left + margin,
                    box.top + tagSize.height() * 1F, pen
            );
        }

        return outputBitmap;
    }

    private static class DetectionResult {

        final RectF boundingBox;
        final String text;

        DetectionResult(final RectF boundingBox, final String text) {
            this.boundingBox = boundingBox;
            this.text = text;
        }
    }
}
