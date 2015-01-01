package me.strlght.campanion.app.callback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import me.strlght.campanion.app.util.FileUtils;

/**
 * Created by starlight on 9/25/14.
 */
@SuppressWarnings("UnusedDeclaration")
public class StabilizedPictureCallback extends PictureCallback {

	private static final String TAG = "StabilizedPictureCallback";

	@Override
	public void onPictureTaken(byte[] bytes, Camera camera) {
		getBackgroundHandler().post(new Saver(getRoll(), getFacing(), bytes));
	}

	private class Saver implements Runnable {

		private final float mRoll;
		private final int mFacing;
		private final byte[] mBytes;

		public Saver(float roll, int facing, byte[] bytes) {
			mRoll = roll;
			mFacing = facing;
			mBytes = bytes.clone();
		}

		@Override
		public void run() {
			Bitmap img = BitmapFactory.decodeByteArray(mBytes, 0, mBytes.length);
			int originalWidth = img.getWidth();
			int originalHeight = img.getHeight();
			float k = (float) originalWidth / originalHeight;

			float rotation;
			if (mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				rotation = 270.0f - mRoll;
			} else {
				rotation = 90.0f + mRoll;
			}
			int scaledHeight = (int) ((float) originalHeight / Math.sqrt(1 + Math.pow(k, 2)));
			int scaledWidth = (int) (scaledHeight * k);

			Matrix transformation = new Matrix();
			transformation.setRotate(rotation);
			img = Bitmap.createBitmap(img, 0, 0, originalWidth, originalHeight, transformation, true);
			int centerX = img.getWidth() / 2;
			int centerY = img.getHeight() / 2;
			img = Bitmap.createBitmap(img, centerX - scaledWidth / 2, centerY - scaledHeight / 2, scaledWidth, scaledHeight);
			FileUtils.save(getContext(), img);
		}

	}
}
