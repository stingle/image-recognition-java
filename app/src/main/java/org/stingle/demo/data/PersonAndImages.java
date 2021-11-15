package org.stingle.demo.data;

import android.content.Context;
import android.graphics.Bitmap;

import org.stingle.ai.face.Person;
import org.stingle.demo.util.AndroidUtils;

import java.util.List;

public class PersonAndImages {
	public final Person person;
	public final List<Image> imageList;

	private Bitmap cachedThumbnail;

	public PersonAndImages(Person person, List<Image> imageList) {
		this.person = person;
		this.imageList = imageList;
	}

	public Bitmap getThumbnail(Context context) {
		if (cachedThumbnail == null) {
			Image firstImage = imageList.get(0);
			if (firstImage != null) {
				cachedThumbnail = AndroidUtils.loadImageAndCrop(context, firstImage.uri, firstImage.personRectMap.get(person.id));
			}
		}

		return cachedThumbnail;
	}
}
