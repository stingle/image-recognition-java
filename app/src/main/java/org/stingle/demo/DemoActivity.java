package org.stingle.demo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import ai.image.StingleImageRecognition;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DemoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Stingle - Demo";

    private ImageView inputImageView;
    private TextView tvPlaceholder;

    private String currentPhotoPath;
    private StingleImageRecognition imageDetector;

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        // prepare bitmap function scales and rotates the bitmap
                        setViewAndDetect(imageDetector.prepareBitmap(currentPhotoPath));
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            });

    private final ActivityResultLauncher<String> requestReadStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Enable read storage permission", Toast.LENGTH_LONG).show();
                } else {
                    dispatchGalleryPictureIntent();
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        if (result.getData() != null) {
                            currentPhotoPath = getRealPathFromURI(this, result.getData().getData());
                            // prepare bitmap function scales and rotates the bitmap
                            setViewAndDetect(imageDetector.prepareBitmap(currentPhotoPath, inputImageView));
                        }
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        setupViews();
        setupImageDetector();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.captureImageFab: {
                try {
                    dispatchTakePictureIntent();
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, e.getMessage());
                }
                break;
            }
            case R.id.chooseImageFab: {
                requestReadStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                break;
            }
            case R.id.imgSampleOne: {
                setViewAndDetect(getSampleImage(R.drawable.img_one));
                break;
            }
            case R.id.imgSampleTwo: {
                setViewAndDetect(getSampleImage(R.drawable.img_two));
                break;
            }
            case R.id.imgSampleThree: {
                setViewAndDetect(getSampleImage(R.drawable.img_three));
                break;
            }
            case R.id.video: {
                startActivity(new Intent(DemoActivity.this, VideoActivity.class));
                break;
            }
            case R.id.gif: {
                startActivity(new Intent(DemoActivity.this, GifActivity.class));
                break;
            }
        }
    }

    private void setupViews() {
        findViewById(R.id.imgSampleOne).setOnClickListener(this);
        findViewById(R.id.imgSampleTwo).setOnClickListener(this);
        findViewById(R.id.imgSampleThree).setOnClickListener(this);
        findViewById(R.id.captureImageFab).setOnClickListener(this);
        findViewById(R.id.chooseImageFab).setOnClickListener(this);
        findViewById(R.id.video).setOnClickListener(this);
        findViewById(R.id.gif).setOnClickListener(this);

        inputImageView = findViewById(R.id.imageView);
        tvPlaceholder = findViewById(R.id.tvPlaceholder);
    }

    private void setupImageDetector() {
        imageDetector = new StingleImageRecognition.Builder(this)
                .maxResults(5)
                .modelPath("model.tflite")
                .scoreThreshold(0.3f)
                .build();
    }

    private void setViewAndDetect(Bitmap bitmap) {
        // Display capture org.stingle.ai.image
        inputImageView.setImageBitmap(bitmap);
        tvPlaceholder.setVisibility(View.INVISIBLE);

        // Run ODT and display result
        // Note that need to run this in the background thread to avoid blocking the app UI because
        // TFLite object detection is a synchronised process.

        // TODO - run on background
        try {
            List<StingleImageRecognition.DetectionResult> results =
                    imageDetector.runObjectDetection(bitmap, inputImageView);
            Log.d(TAG, results.toString());
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private Bitmap getSampleImage(int drawable) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        return BitmapFactory.decodeResource(getResources(), drawable, options);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            cameraActivityResultLauncher.launch(takePictureIntent);
        }
    }

    private void dispatchGalleryPictureIntent() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("org/stingle/ai/image/*");
        galleryActivityResultLauncher.launch(photoPickerIntent);
    }

    private File createImageFile() throws IOException {
        // Create an org.stingle.ai.image file name
        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(
                "JPEG_" + timeStamp, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
        currentPhotoPath = file.getAbsolutePath();

        return file;
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}