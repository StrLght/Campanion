package me.strlght.campanion.app.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

/**
 * Created by starlight on 10/5/14.
 */
public class ImageLoader extends AsyncTask<String, Void, Bitmap> {

	private ImageLoaderCallback mImageLoaderCallback;

	public ImageLoader() {
		this(null);
	}

	public ImageLoader(ImageLoaderCallback imageLoaderCallback) {
		mImageLoaderCallback = imageLoaderCallback;
	}

	@Override
	protected Bitmap doInBackground(String... strings) {
		return BitmapFactory.decodeFile(strings[0]);
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		super.onPostExecute(bitmap);

		if (mImageLoaderCallback != null) {
			mImageLoaderCallback.onLoadFinished(bitmap);
		}
	}

	public interface ImageLoaderCallback {
		public void onLoadFinished(Bitmap bitmap);
	}

}
