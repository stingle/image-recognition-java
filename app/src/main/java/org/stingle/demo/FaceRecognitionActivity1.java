package org.stingle.demo;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import ai.face.FaceRecogniser;
import org.stingle.demo.adapters.ImageListAdapter;
import org.stingle.demo.adapters.PersonListAdapter;
import org.stingle.demo.data.Image;
import org.stingle.demo.data.MockDatabase;
import org.stingle.demo.data.PersonAndImages;
import org.stingle.demo.util.AndroidUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceRecognitionActivity1 extends AppCompatActivity {
	private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());

	private View progressIndicator;

	private ImageListAdapter imageListAdapter;
	private PersonListAdapter personListAdapter;

	private FaceRecogniser faceRecogniser;

	private ExecutorService backgroundExecutor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_face_recognition1);

		backgroundExecutor = Executors.newSingleThreadExecutor();

		imageListAdapter = new ImageListAdapter(this);

		personListAdapter = new PersonListAdapter(this);
		personListAdapter.setFaceSelectListener(new PersonListAdapter.FaceSelectListener() {
			@Override
			public void onPersonSelected(PersonAndImages person) {
				imageListAdapter.submitList(person.imageList);
			}
		});

		RecyclerView faceListView = findViewById(R.id.face_list);
		faceListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
		faceListView.setAdapter(personListAdapter);
		RecyclerView.ItemAnimator itemAnimator = faceListView.getItemAnimator();
		if (itemAnimator instanceof SimpleItemAnimator) {
			SimpleItemAnimator simpleItemAnimator = (SimpleItemAnimator) itemAnimator;
			simpleItemAnimator.setSupportsChangeAnimations(false);
		}

		RecyclerView imageListView = findViewById(R.id.image_list);
		imageListView.setLayoutManager(new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false));
		imageListView.setAdapter(imageListAdapter);

		progressIndicator = findViewById(R.id.progress);
		progressIndicator.setVisibility(View.GONE);

		findViewById(R.id.btn_add_image).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0);
			}
		});

		faceRecogniser = new FaceRecogniser();
		faceRecogniser.init(FaceRecognitionActivity1.this, "facenet.tflite");

		updateAdapters();
	}

	private void updateAdapters() {
		uiThreadHandler.post(() -> {
			List<PersonAndImages> personAndImagesList = MockDatabase.getInstance().getPersonAndImagesList();

			personListAdapter.submitList(personAndImagesList, new Runnable() {
				@Override
				public void run() {
					PersonAndImages selectedFace = personListAdapter.getSelectedFace();
					if (selectedFace != null) {
						imageListAdapter.submitList(selectedFace.imageList);
					}
				}
			});
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
			Uri uriData = data.getData();
			ClipData clipData = data.getClipData();

			List<Uri> imageUriList = new ArrayList<>();

			if (clipData != null) {
				int count = clipData.getItemCount();

				for (int i = 0; i < count; i++) {
					imageUriList.add(clipData.getItemAt(i).getUri());
				}
			} else if (uriData != null) {
				imageUriList.add(uriData);
			}

			processGalleryImageList(imageUriList);
		}
	}

	private void processGalleryImageList(List<Uri> imageUriList) {
		progressIndicator.setVisibility(View.VISIBLE);

		backgroundExecutor.execute(() -> {
			for (Uri imageUri : imageUriList) {
				try {
					FaceRecogniser.Result result = faceRecogniser.recognise(AndroidUtils.loadImage(this, imageUri),
							MockDatabase.getInstance().getPersonCollection()).get();

					MockDatabase.getInstance().addPersonList(result.personList);
					MockDatabase.getInstance().addImage(new Image(imageUri, result.identifiedRectangleMap));
				} catch (ExecutionException | InterruptedException e) {
					e.printStackTrace();
				}
			}

			uiThreadHandler.post(() -> {
				updateAdapters();
				progressIndicator.setVisibility(View.GONE);
			});
		});
	}
}