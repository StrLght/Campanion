package me.strlght.campanion.app.callback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import me.strlght.campanion.app.util.FileUtils;

/**
 * Created by starlight on 9/26/14.
 */
public class DefaultPictureCallback extends PictureCallback {

	private static final String TAG = "DefaultPictureCallback";

	@Override
	public void onPictureTaken(byte[] bytes, Camera camera) {
		new SaveImageTask(getRoll(), getFacing()).execute(bytes);
	}

	private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

		private final float mRoll;
		private final int mFacing;

		SaveImageTask(float roll, int facing) {
			super();

			mRoll = roll;
			mFacing = facing;
		}

		@Override
		protected Void doInBackground(byte[]... bytes) {
			Bitmap img = BitmapFactory.decodeByteArray(bytes[0], 0, bytes[0].length);
			int origwidth = img.getWidth();
			int origheight = img.getHeight();

			int rotation = 90;
			if (mRoll > 135 || mRoll < -135) {
				rotation += 180;
			} else if (mRoll > 45) {
				rotation += 90;
			} else if (mRoll < -45) {
				rotation += 270;
			}
			if (mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT
					&& (rotation == 90 || rotation == 270)) {
				rotation += 180;
			}

			Matrix transformation = new Matrix();
			transformation.setRotate(rotation);
			img = Bitmap.createBitmap(img, 0, 0, origwidth, origheight, transformation, false);
			FileUtils.save(getContext(), img);
			return null;
		}

	}
}
