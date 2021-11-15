package org.stingle.demo.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import org.stingle.ai.face.Callback;
import org.stingle.demo.data.MockDatabase;

import java.util.Map;
import java.util.UUID;

public class FaceView extends View {
	private Map<UUID, Rect> rectMap;

	private Matrix imageTransform;
	private Matrix invertedImageTransform;
	private Bitmap image;

	private float[] touchPoints;

	private Paint imagePaint;
	private Paint faceRectPaint;
	private Paint faceNameStrokePaint;
	private Paint faceNameFillPaint;

	private Rect textBounds;

	private RectF mappedRect;

	private Callback<UUID> faceClickedCallback;

	public FaceView(Context context) {
		this(context, null);
	}

	public FaceView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public FaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);

		imageTransform = new Matrix();
		invertedImageTransform = new Matrix();

		touchPoints = new float[2];

		textBounds = new Rect();
		mappedRect = new RectF();

		imagePaint = new Paint(Paint.FILTER_BITMAP_FLAG);

		faceRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		faceRectPaint.setStyle(Paint.Style.STROKE);
		faceRectPaint.setColor(Color.MAGENTA);
		faceRectPaint.setStrokeWidth(5);

		faceNameFillPaint = new Paint();
		faceNameFillPaint.setTextSize(50);
		faceNameFillPaint.setColor(Color.WHITE);

		faceNameStrokePaint = new Paint();
		faceNameStrokePaint.setStyle(Paint.Style.STROKE);
		faceNameStrokePaint.setTextSize(50);
		faceNameStrokePaint.setColor(Color.BLACK);
	}

	public Bitmap getImage() {
		return image;
	}

	public void setFaceClickedCallback(Callback<UUID> faceClickedCallback) {
		this.faceClickedCallback = faceClickedCallback;
	}

	public void setImage(Bitmap image) {
		this.image = image;

		if (getWidth() > 0 && getHeight() > 0) {
			updateTransform();
		}

		postInvalidate();
	}

	public void setRectMap(Map<UUID, Rect> rectMap) {
		this.rectMap = rectMap;

		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (image != null) {
			updateTransform();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (image != null) {
			canvas.drawBitmap(image, imageTransform, imagePaint);
		}

		if (rectMap != null) {
			for (Map.Entry<UUID, Rect> entry : rectMap.entrySet()) {
				mappedRect.set(entry.getValue());
				imageTransform.mapRect(mappedRect);

				String name = MockDatabase.getInstance().getPersonName(entry.getKey());

				faceNameStrokePaint.getTextBounds(name, 0, name.length(), textBounds);

				canvas.drawRect(mappedRect, faceRectPaint);

				canvas.drawText(name, mappedRect.left, mappedRect.top - textBounds.bottom, faceNameStrokePaint);
				canvas.drawText(name, mappedRect.left, mappedRect.top - textBounds.bottom, faceNameFillPaint);
			}
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_UP: {
				touchPoints[0] = event.getX();
				touchPoints[1] = event.getY();

				imageTransform.invert(invertedImageTransform);
				invertedImageTransform.mapPoints(touchPoints);

				if (rectMap != null) {
					for (Map.Entry<UUID, Rect> entry : rectMap.entrySet()) {
						if (entry.getValue().contains((int) touchPoints[0], (int) touchPoints[1])) {
							if (faceClickedCallback != null) {
								faceClickedCallback.call(entry.getKey());
							}

							break;
						}
					}
				}
				break;
			}
		}

		return true;
	}

	private void updateTransform() {
		RectF imageRect = new RectF(0f, 0f, image.getWidth(), image.getHeight());
		RectF screenRect = new RectF(0f, 0f, getWidth(), getHeight());

		imageTransform.setRectToRect(imageRect, screenRect, Matrix.ScaleToFit.CENTER);
	}
}
