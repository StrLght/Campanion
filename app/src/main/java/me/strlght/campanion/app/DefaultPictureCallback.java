package me.strlght.campanion.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;

/**
 * Created by starlight on 9/26/14.
 */
public class DefaultPictureCallback implements Camera.PictureCallback {

	private Context mContext;
	private float mPitch;
	private float mRoll;

	@Override
	public void onPictureTaken(byte[] bytes, Camera camera) {
		new SaveImageTask(mPitch, mRoll).execute(bytes);
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

	private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

		private float mPitch;
		private float mRoll;

		SaveImageTask(float pitch, float roll) {
			super();

			mPitch = pitch;
			mRoll = roll;
		}

		@Override
		protected Void doInBackground(byte[]... bytes) {
			Bitmap img = BitmapFactory.decodeByteArray(bytes[0], 0, bytes[0].length);
			int origwidth = img.getWidth();
			int origheight = img.getHeight();

			//TODO: fix rotation

			Matrix transformation = new Matrix();
			// transformation.setRotate(rotation);
			img = Bitmap.createBitmap(img, 0, 0, origwidth, origheight, transformation, false);
			Saver.save(mContext, img);
			return null;
		}

	}
}
