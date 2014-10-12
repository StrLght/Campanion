package me.strlght.campanion.app.view;

import android.content.Context;
import android.graphics.*;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by starlight on 10/12/14.
 */
public class RegionCameraView extends ImageView {

	private Paint mPaint;
	private Camera.Size mCameraSize;

	public RegionCameraView(Context context) {
		super(context);
		init();
	}

	public RegionCameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RegionCameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setColor(Color.parseColor("#00000000"));
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		final int size = width + height;

		setMeasuredDimension(size, size);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (mCameraSize != null) {
			setSize(mCameraSize);
		}
	}

	public void setSize(Camera.Size size) {
		requestLayout();
		final int wph = getWidth() + getHeight();
		final int length = Math.min(2048, wph);
		mCameraSize = size;

		if (length > 0) {
			Bitmap region = Bitmap.createBitmap(length, length, Bitmap.Config.ARGB_8888);
			region.eraseColor(Color.parseColor("#00000000"));
			Canvas canvas = new Canvas(region);
			canvas.drawColor(Color.parseColor("#CC000000"));
			if (size != null) {
				int originalWidth = size.width;
				int originalHeight = size.height;
				float k = (float) originalWidth / originalHeight;
				int scaledHeight = (int) ((float) originalHeight / Math.sqrt(1 + Math.pow(k, 2)));
				int scaledWidth = (int) (scaledHeight * k);
				int center = length / 2;
				canvas.drawRect(center - scaledWidth / 2, center - scaledHeight / 2, center + scaledWidth / 2, center + scaledHeight / 2, mPaint);
			}

			setImageBitmap(region);
		}
	}

}
