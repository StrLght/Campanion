package me.strlght.campanion.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import me.strlght.campanion.app.R;

import java.io.File;

public class PreviewActivity extends Activity {

	public static final String EXTRA_IMAGE_FILE = "image_file";
	private static final String TAG = "PreviewActivity";
	private ImageViewTouch mImagePreview;
	private File mFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);

		Intent intent = getIntent();
		mFile = (File) intent.getSerializableExtra(EXTRA_IMAGE_FILE);

		mImagePreview = (ImageViewTouch) findViewById(R.id.image_preview);
		mImagePreview.setDisplayType(ImageViewTouch.DisplayType.FIT_TO_SCREEN);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Picasso.with(getApplicationContext())
				.load(mFile)
				.into(new ImageViewTarget());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.preview, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class ImageViewTarget implements Target {

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			mImagePreview.setImageBitmap(bitmap);
		}

		@Override
		public void onBitmapFailed(Drawable errorDrawable) {
			//TODO: handle error
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {

		}

	}

}
