package me.strlght.campanion.app.callback;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import me.strlght.campanion.app.util.Saver;

/**
 * Created by starlight on 9/26/14.
 */
public class DefaultPictureCallback implements Camera.PictureCallback {

    private static final String TAG = "DefaultPictureCallback";

	private Context mContext;
	private float mPitch;
	private float mRoll;
	private int mFacing;

	@Override
	public void onPictureTaken(byte[] bytes, Camera camera) {
		new SaveImageTask(mPitch, mRoll, mFacing).execute(bytes);
	}

	public void setContext(Context context) {
		mContext = context;
	}

	public void setPitch(float pitch) {
		mPitch = pitch;
	}

	public void setRoll(float roll) {
		mRoll = roll;
	}

	public void setFacing(int facing) {
		mFacing = facing;
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

			int rotation = 90;
			if (mPitch > 45 && mPitch < 135) {
				rotation += 180;
			} else if (mRoll > 45) {
				rotation += 90;
			} else if (mRoll < -45) {
				rotation += 270;
			}

			if (mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT && (rotation == 270 || rotation == 90)) {
				rotation += 180;
			}

			Matrix transformation = new Matrix();
			transformation.setRotate(rotation);
			img = Bitmap.createBitmap(img, 0, 0, origwidth, origheight, transformation, false);
			Saver.save(mContext, img);
			return null;
		}

	}
}
