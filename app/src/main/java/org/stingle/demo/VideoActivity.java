package org.stingle.demo;

import androidx.appcompat.app.AppCompatActivity;

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
    private ProgressDialog progressDialog;

    private final Executor backgroundExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Executor mainExecutor = new Handler(Looper.getMainLooper())::post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        setupViews();
        setupVideoDetector();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.runBtn) {
            runVideoDetectionAndShowResults();
        }
    }

    private void setupViews() {
        findViewById(R.id.runBtn).setOnClickListener(this);

        resultsView = findViewById(R.id.results);
        VideoView videoView = findViewById(R.id.videoView);

        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sample));
        videoView.start();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("please wait");
        progressDialog.setTitle("video processing....");
        progressDialog.setCancelable(false);
    }

    private void setupVideoDetector() {
        videoDetector = new StingleImageRecognition.Builder(this)
                .maxResults(5)
                .modelPath("model.tflite")
                .scoreThreshold(0.3f)
                .build();
    }


    private void runVideoDetectionAndShowResults() {
        progressDialog.show();
        backgroundExecutor.execute(() -> {
            try {
                Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sample);
                Set<StingleImageRecognition.DetectionResult> results =
                        videoDetector.runVideoObjectDetection(videoUri, 50_000L, 3_000L);
                System.out.println("results.size() = " + results.size());
                StringBuilder sb = new StringBuilder();
                for (StingleImageRecognition.DetectionResult el : results) {
                    sb.append(el.label);
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