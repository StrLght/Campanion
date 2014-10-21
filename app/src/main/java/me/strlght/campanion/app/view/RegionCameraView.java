package me.strlght.campanion.app.view;

import android.content.Context;
import android.graphics.*;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by starlight on 10/12/14.
 */
public class RegionCameraView extends View {

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
		setLayerType(LAYER_TYPE_HARDWARE, null);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		final int size = width + height;

		setMeasuredDimension(size, size);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final int length = canvas.getWidth() + canvas.getHeight();
		if (length > 0) {
			canvas.drawColor(Color.parseColor("#CC000000"));
			if (mPreviewSize != null) {
				float k = (float) mActualSize.width / mActualSize.height;
				int scaledWidth = (int) ((float) mPreviewSize.width / Math.sqrt(1 + Math.pow(k, 2)));
				int scaledHeight = (int) ((float) scaledWidth / k);
				int centerX = canvas.getWidth() / 2;
				int centerY = canvas.getHeight() / 2;
				canvas.drawRect(centerX - scaledWidth / 2, centerY - scaledHeight / 2, centerX + scaledWidth / 2, centerY + scaledHeight / 2, mPaint);
			}
			canvas.save();
		}
	}

	public void setSize(Camera.Size previewSize, Camera.Size actualSize) {
		mPreviewSize = previewSize;
		mActualSize = actualSize;
		invalidate();
	}

}
