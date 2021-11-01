package org.stingle.demo.facerecognition.data;

import android.graphics.Rect;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MockImageDatabase {
	private final Map<Uri, Map<UUID, Rect>> data;

	public MockImageDatabase() {
		data = new HashMap<>();
	}

	public Map<UUID, Rect> getImageData(Uri imageUri) {
		return data.get(imageUri);
	}
}
