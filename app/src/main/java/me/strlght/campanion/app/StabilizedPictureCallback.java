package me.strlght.campanion.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;


/**
 * Created by starlight on 9/25/14.
 */
public class StabilizedPictureCallback implements Camera.PictureCallback {

	private Context mContext;

	@Override
	public void onPictureTaken(byte[] bytes, Camera camera) {
		new SaveImageTask().execute(bytes);
	}

	public void setContext(Context context) {
		mContext = context;
	}

	private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

		@Override
		protected Void doInBackground(byte[]... bytes) {
			Bitmap img = BitmapFactory.decodeByteArray(bytes[0], 0, bytes[0].length);
			int origwidth = img.getWidth();
			int origheight = img.getHeight();

			//TODO: fix scale and rotation
			float rotation = 0.0f;
			int scaledwidth = Math.abs(
					Double.valueOf(origwidth * Math.cos(rotation) - origheight * Math.sin(rotation))
							.intValue());
			int scaledheight = Math.abs(
					Double.valueOf(origwidth * Math.sin(rotation) + origheight * Math.cos(rotation))
							.intValue());

			Matrix transformation = new Matrix();
			transformation.postRotate(rotation);
			transformation.postScale((float) scaledwidth / (float) origwidth, (float) scaledheight / (float) origheight);
			img = Bitmap.createBitmap(img, 0, 0, origwidth, origheight, transformation, true);
			int centerx = origwidth / 2;
			int centery = origheight / 2;
			img = Bitmap.createBitmap(img, centerx - (scaledwidth / 2), centery - (scaledheight / 2), scaledwidth, scaledheight);

			Saver.save(mContext, img);

			return null;
		}

	}
}
