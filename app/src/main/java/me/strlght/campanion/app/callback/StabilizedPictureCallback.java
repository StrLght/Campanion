package me.strlght.campanion.app.callback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import me.strlght.campanion.app.util.FileUtils;

/**
 * Created by starlight on 9/25/14.
 */
public class StabilizedPictureCallback extends PictureCallback {

	private static final String TAG = "StabilizedPictureCallback";

	@Override
	public void onPictureTaken(byte[] bytes, Camera camera) {
		new SaveImageTask(getPitch(), getRoll(), getFacing()).execute(bytes);
	}

	private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

		private final float mPitch;
		private final float mRoll;
		private final int mFacing;

		SaveImageTask(float pitch, float roll, int facing) {
			super();

			mPitch = pitch;
			mRoll = roll;
			mFacing = facing;
		}

		@Override
		protected Void doInBackground(byte[]... bytes) {
			Bitmap img = BitmapFactory.decodeByteArray(bytes[0], 0, bytes[0].length);
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

			if (mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT &&
					((mPitch < -45 && mPitch > -135) || (mPitch > 45 && mPitch < 135))) {
				rotation += 180.0f;
			}

			Matrix transformation = new Matrix();
			transformation.setRotate(rotation);
			img = Bitmap.createBitmap(img, 0, 0, originalWidth, originalHeight, transformation, true);
			int centerX = img.getWidth() / 2;
			int centerY = img.getHeight() / 2;
			img = Bitmap.createBitmap(img, centerX - scaledWidth / 2, centerY - scaledHeight / 2, scaledWidth, scaledHeight);

			FileUtils.save(getContext(), img);

			return null;
		}

	}
}
