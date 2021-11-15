package org.stingle.demo.data;

import android.graphics.Rect;
import android.net.Uri;

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
