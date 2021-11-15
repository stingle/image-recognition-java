package org.stingle.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.stingle.ai.face.FaceRecogniser;
import org.stingle.demo.data.Image;
import org.stingle.demo.data.MockDatabase;
import org.stingle.demo.util.AndroidUtils;
import org.stingle.demo.views.FaceView;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceRecognitionActivity2 extends AppCompatActivity {
	private FaceView faceView;

	private FaceRecogniser faceRecogniser;

	private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());
	private ExecutorService backgroundExecutor;

	private View progressIndicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_face_recognition2);

		backgroundExecutor = Executors.newSingleThreadExecutor();

		faceView = findViewById(R.id.face);
		faceView.setFaceClickedCallback(this::showTextEditDialog);

		progressIndicator = findViewById(R.id.progress);
		progressIndicator.setVisibility(View.GONE);

		findViewById(R.id.btn_pick_image).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0);
			}
		});

		faceRecogniser = new FaceRecogniser();
		faceRecogniser.init(this, "facenet.tflite");
	}

	private void showTextEditDialog(UUID personId) {
		View dialogView = LayoutInflater.from(FaceRecognitionActivity2.this).inflate(R.layout.dialog_edit_name, null);

		AlertDialog.Builder builder = new AlertDialog.Builder(FaceRecognitionActivity2.this);
		builder.setTitle("Change name");
		builder.setView(dialogView);
		builder.setPositiveButton("OK", (dialog, which) -> {
			String name = ((EditText) dialogView.findViewById(R.id.input)).getText().toString();

			MockDatabase.getInstance().setPersonName(personId, name);

			faceView.invalidate();
		});
		builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
		builder.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
			processGalleryImage(data.getData());
		}
	}

	private void processGalleryImage(Uri imageUri) {
		progressIndicator.setVisibility(View.VISIBLE);

		backgroundExecutor.execute(() -> {
			try {
				Bitmap image = AndroidUtils.loadImage(this, imageUri);

				FaceRecogniser.Result result = faceRecogniser.recognise(image,
						MockDatabase.getInstance().getPersonCollection()).get();

				MockDatabase.getInstance().addPersonList(result.personList);
				MockDatabase.getInstance().addImage(new Image(imageUri, result.identifiedRectangleMap));

				uiThreadHandler.post(() -> {
					faceView.setImage(image);
					faceView.setRectMap(result.identifiedRectangleMap);

					progressIndicator.setVisibility(View.GONE);
				});
			} catch (ExecutionException | InterruptedException e) {
				e.printStackTrace();
			}
		});
	}
}