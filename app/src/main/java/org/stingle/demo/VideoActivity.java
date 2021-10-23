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
import android.widget.TextView;
import android.widget.VideoView;

import org.stingle.imagerecognition.StingleImageRecognition;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener {

    private StingleImageRecognition videoDetector;
    private TextView resultsView;
    private VideoView videoView;
    private ProgressDialog progressDialog;
    private @RawRes int selectedVideoId = R.raw.sample_1;

    private final Executor backgroundExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Executor mainExecutor = new Handler(Looper.getMainLooper())::post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

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
                playVideo(R.raw.sample_1, R.id.sample1);
                break;
            }
            case R.id.sample2: {
                playVideo(R.raw.sample_2,  R.id.sample2);
                break;
            }
            case R.id.sample3: {
                playVideo(R.raw.sample_3,  R.id.sample3);
                break;
            }
            case R.id.sample4: {
                playVideo(R.raw.sample_4,  R.id.sample4);
                break;
            }
            case R.id.sample5: {
                playVideo(R.raw.sample_5,  R.id.sample5);
                break;
            }
        }
    }

    private void setupVideoDetector() {
        videoDetector = new StingleImageRecognition.Builder(this)
                .maxResults(5)
                .modelPath("model.tflite")
                .scoreThreshold(0.3f)
                .build();
    }

    private void setupViews() {
        findViewById(R.id.runBtn).setOnClickListener(this);

        findViewById(R.id.sample1).setOnClickListener(this);
        findViewById(R.id.sample2).setOnClickListener(this);
        findViewById(R.id.sample3).setOnClickListener(this);
        findViewById(R.id.sample4).setOnClickListener(this);
        findViewById(R.id.sample5).setOnClickListener(this);

        resultsView = findViewById(R.id.results);

        videoView = findViewById(R.id.videoView);
        playVideo(R.raw.sample_1, R.id.sample1); // playing sample1 by default

        setupProgressDialog();
    }

    @SuppressLint("ResourceAsColor")
    private void playVideo(@RawRes int videoId, @IdRes int textViewId) {
        videoView.stopPlayback();
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + videoId));
        videoView.start();
        selectedVideoId = videoId;

        resetTextColors();
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

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setMessage("video processing....");
        progressDialog.setCancelable(false);
    }

    private void runVideoDetectionAndShowResults() {
        progressDialog.show();
        backgroundExecutor.execute(() -> {
            try {
                Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + selectedVideoId);
                Set<StingleImageRecognition.DetectionResult> results =
                        videoDetector.runVideoObjectDetection(videoUri, 20_000L, 2_000L);

                StringBuilder sb = new StringBuilder();
                for (StingleImageRecognition.DetectionResult el : results) {
                    sb.append(el.getLabel());
                    sb.append(", ");
                }
                mainExecutor.execute(() -> {
                    progressDialog.dismiss();
                    resultsView.setText(sb.toString());
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