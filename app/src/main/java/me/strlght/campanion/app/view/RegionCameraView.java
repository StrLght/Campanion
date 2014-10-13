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
	private Camera.Size mPreviewSize;
	private Camera.Size mActualSize;

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

		if (mPreviewSize != null && mActualSize != null) {
			setSize(mPreviewSize, mActualSize);
		}
	}

	public void setSize(Camera.Size previewSize, Camera.Size actualSize) {
		requestLayout();
		final int wph = getWidth() + getHeight();
		final int length = Math.min(2048, wph);
		mPreviewSize = previewSize;
		mActualSize = actualSize;

		if (length > 0 && wph != 0) {
			Bitmap region = Bitmap.createBitmap(length, length, Bitmap.Config.ARGB_8888);
			region.eraseColor(Color.parseColor("#00000000"));
			Canvas canvas = new Canvas(region);
			canvas.drawColor(Color.parseColor("#CC000000"));
			if (previewSize != null) {
				float k = (float) mActualSize.width / mActualSize.height;
				int scaledWidth = (int) ((float) previewSize.width / Math.sqrt(1 + Math.pow(k, 2)));
				int scaledHeight = (int) ((float) scaledWidth / k);
				int center = length / 2;
				canvas.drawRect(center - scaledWidth / 2, center - scaledHeight / 2, center + scaledWidth / 2, center + scaledHeight / 2, mPaint);
			}

			setImageBitmap(region);
		}
	}

}
