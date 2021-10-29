package org.stingle.demo;

import androidx.annotation.IdRes;
import androidx.annotation.RawRes;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.stingle.imagerecognition.StingleImageRecognition;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GifActivity extends AppCompatActivity implements View.OnClickListener {

    private StingleImageRecognition imageDetector;
    private TextView resultsView;
    private ImageView gifImageView;
    private ProgressDialog progressDialog;
    private @RawRes
    int selectedGifId = R.raw.sample_1;

    private final Executor backgroundExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Executor mainExecutor = new Handler(Looper.getMainLooper())::post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giif);

        setupViews();
        setupVideoDetector();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.runBtn: {
                runVideoDetectionAndShowResults();
                break;
            }
            case R.id.sample1: {
                loadGif(R.raw.gif_sample_1, R.id.sample1);
                break;
            }
            case R.id.sample2: {
                loadGif(R.raw.gif_sample_2, R.id.sample2);
                break;
            }
            case R.id.sample3: {
                loadGif(R.raw.gif_sample_3, R.id.sample3);
                break;
            }
            case R.id.sample4: {
                loadGif(R.raw.gif_sample_4, R.id.sample4);
                break;
            }
            case R.id.sample5: {
                loadGif(R.raw.gif_sample_5, R.id.sample5);
                break;
            }
        }
    }

    private void setupViews() {
        findViewById(R.id.runBtn).setOnClickListener(this);

        findViewById(R.id.sample1).setOnClickListener(this);
        findViewById(R.id.sample2).setOnClickListener(this);
        findViewById(R.id.sample3).setOnClickListener(this);
        findViewById(R.id.sample4).setOnClickListener(this);
        findViewById(R.id.sample5).setOnClickListener(this);

        resultsView = findViewById(R.id.results);

        gifImageView = findViewById(R.id.gifView);
        loadGif(R.raw.gif_sample_1, R.id.sample1); // playing sample1 by default

        setupProgressDialog();
    }

    private void setupVideoDetector() {
        imageDetector = new StingleImageRecognition.Builder(this)
                .maxResults(5)
                .modelPath("model.tflite")
                .scoreThreshold(0.3f)
                .build();
    }

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setMessage("video processing....");
        progressDialog.setCancelable(false);
    }

    @SuppressLint("ResourceAsColor")
    private void loadGif(@RawRes int gifId, @IdRes int textViewId) {
        Glide.with(this).asGif().load(gifId).into(gifImageView);

        selectedGifId = gifId;

        resetTextColors();
        resultsView.setText("");
        TextView selected = findViewById(textViewId);
        selected.setTextColor(R.color.purple_500);
    }

    @SuppressLint("ResourceAsColor")
    private void resetTextColors() {
        TextView t1 = findViewById(R.id.sample1);
        t1.setTextColor(R.color.black);

        TextView t2 = findViewById(R.id.sample2);
        t2.setTextColor(R.color.black);

        TextView t3 = findViewById(R.id.sample3);
        t3.setTextColor(R.color.black);

        TextView t4 = findViewById(R.id.sample4);
        t4.setTextColor(R.color.black);

        TextView t5 = findViewById(R.id.sample5);
        t5.setTextColor(R.color.black);
    }

    private void runVideoDetectionAndShowResults() {
        progressDialog.show();
        backgroundExecutor.execute(() -> {
            try {
                Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + selectedGifId);
                imageDetector.runGifObjectDetection(videoUri, 0.5f, results -> {
                    StringBuilder sb = new StringBuilder();
                    for (StingleImageRecognition.DetectionResult el : results) {
                        sb.append(el.getLabel());
                        sb.append(", ");
                    }
                    mainExecutor.execute(() -> {
                        progressDialog.dismiss();
                        resultsView.setText(sb.toString());
                    });
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainExecutor.execute(() -> {
                    progressDialog.dismiss();
                    resultsView.setText(e.getMessage());
                });
            }
        });
    }
}