package me.strlght.campanion.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.View;
import me.strlght.campanion.app.R;

/**
 * Created by starlight on 10/12/14.
 */
@SuppressWarnings("UnusedDeclaration")
public class RegionCameraView extends View {

	private Paint mPaint;
	private int mScaledHeight = -1;
	private int mScaledWidth = -1;

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
		mPaint.setColor(getResources().getColor(R.color.transparent));
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
			canvas.drawColor(getResources().getColor(R.color.stabilized_view_background));
			if (mScaledWidth >= 0 && mScaledHeight >= 0) {
				int centerX = canvas.getWidth() / 2;
				int centerY = canvas.getHeight() / 2;
				canvas.drawRect(centerX - mScaledWidth / 2, centerY - mScaledHeight / 2, centerX + mScaledWidth / 2, centerY + mScaledHeight / 2, mPaint);
			}
			canvas.save();
		}
	}

	public void setSize(Camera.Size previewSize, Camera.Size actualSize) {
		if (previewSize != null) {
			float k = (float) actualSize.width / actualSize.height;
			mScaledWidth = (int) ((float) previewSize.width / Math.sqrt(1 + Math.pow(k, 2)));
			mScaledHeight = (int) ((float) mScaledWidth / k);
		} else {
			mScaledWidth = -1;
			mScaledHeight = -1;
		}
		invalidate();
	}

}
