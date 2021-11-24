package org.stingle.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.btn_image_tagging).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, DemoActivity.class);
				startActivity(intent);
			}
		});

		findViewById(R.id.btn_face_recognition1).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, FaceRecognitionActivity1.class);
				startActivity(intent);
			}
		});

		findViewById(R.id.btn_face_recognition2).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, FaceRecognitionActivity2.class);
				startActivity(intent);
			}
		});
	}
}