package me.strlght.campanion.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import me.strlght.campanion.app.R;
import me.strlght.campanion.app.adapter.ImageArrayAdapter;
import me.strlght.campanion.app.util.Saver;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by starlight on 10/5/14.
 */
public class GalleryActivity extends Activity {

	private GridView mGridView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_gallery);

		Button cameraButton = (Button) findViewById(R.id.camera_button);
		cameraButton.setOnClickListener(new OnCameraButtonClickListener());

		mGridView = (GridView) findViewById(R.id.pictures_view);
	}

	@Override
	protected void onResume() {
		super.onResume();

		List<String> files;
		String[] strings = Saver.getSaveDirectory().list();
		if (strings == null) {
			files = null;
		} else {
			String directory = Saver.getSaveDirectory().getAbsolutePath();
			for (int i = 0; i < strings.length; i++) {
				strings[i] = directory + File.separator + strings[i];
			}
			files = Arrays.asList(strings);
			Collections.reverse(files);
		}
		mGridView.setAdapter(new ImageArrayAdapter(getBaseContext(), files));
	}

	@Override
	protected void onPause() {
		super.onPause();

		mGridView.setAdapter(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gallery, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class OnCameraButtonClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			Intent intent = new Intent(GalleryActivity.this, ShotActivity.class);
			startActivity(intent);
		}

	}

}
