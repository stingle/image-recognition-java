package org.stingle.demo.facerecognition.data;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Image {
	public final Uri uri;
	public final Map<UUID, Rect> personRectMap;

	public Image(Uri uri, Map<UUID, Rect> personRectMap) {
		this.uri = uri;
		this.personRectMap = personRectMap;
	}
}
