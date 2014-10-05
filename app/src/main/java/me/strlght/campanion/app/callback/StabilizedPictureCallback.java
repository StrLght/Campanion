package me.strlght.campanion.app.callback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import me.strlght.campanion.app.util.Saver;

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
			int origwidth = img.getWidth();
			int origheight = img.getHeight();
			float k = (float) origwidth / origheight;

			//TODO: fix orientation
			float rotation = 90.0f - mRoll;
			int scaledheight = (int) ((float) origheight / Math.sqrt(1 + Math.pow((float) origwidth / origheight, 2)));
			int scaledwidth = (int) (scaledheight * k);

			if (mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT &&
					((mPitch < -45 && mPitch > -135) || (mPitch > 45 && mPitch < 135))) {
				rotation += 180.0f;
			}

			Matrix transformation = new Matrix();
			transformation.setRotate(rotation);
			img = Bitmap.createBitmap(img, 0, 0, origwidth, origheight, transformation, true);
			int centerx = img.getWidth() / 2;
			int centery = img.getHeight() / 2;
			img = Bitmap.createBitmap(img, centerx - scaledwidth / 2, centery - scaledheight / 2, scaledwidth, scaledheight);

			Saver.save(getContext(), img);

			return null;
		}

	}
}
