package me.strlght.campanion.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import me.strlght.campanion.app.R;
import me.strlght.campanion.app.adapter.ImageArrayAdapter;
import me.strlght.campanion.app.util.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by starlight on 10/5/14.
 */
public class GalleryActivity extends Activity {

	private GridView mGridView;
	private LinearLayout mActionLayout;
	private LinearLayout mSwitchLayout;
	private String mChoosenImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_gallery);

		Button cameraButton = (Button) findViewById(R.id.camera_button);
		cameraButton.setOnClickListener(new OnCameraButtonClickListener());

		mActionLayout = (LinearLayout) findViewById(R.id.action_layout);
		Button editButton = (Button) findViewById(R.id.edit_button);
		editButton.setOnClickListener(new OnEditButtonClickListener());
		Button shareButton = (Button) findViewById(R.id.share_button);
		shareButton.setOnClickListener(new OnShareButtonClickListener());
		Button closeButton = (Button) findViewById(R.id.close_button);
		closeButton.setOnClickListener(new OnCloseButtonClickListener());

		mSwitchLayout = (LinearLayout) findViewById(R.id.switch_layout);

		mGridView = (GridView) findViewById(R.id.pictures_view);
		mGridView.setOnItemClickListener(new OnItemClickListener());
	}

	@Override
	protected void onResume() {
		super.onResume();

		List<String> files;
		String[] strings = FileUtils.getSaveDirectory().list();
		if (strings == null) {
			files = null;
		} else {
			String directory = FileUtils.getSaveDirectory().getAbsolutePath();
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
		//TODO: add menu
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class OnItemClickListener implements GridView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
			//TODO: fix selection (probably just add the selector to the view)
			mActionLayout.setVisibility(View.VISIBLE);
			mSwitchLayout.setVisibility(View.GONE);
			mChoosenImage = (String) adapterView.getAdapter().getItem(i);
		}

	}

	private class OnCameraButtonClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			Intent intent = new Intent(GalleryActivity.this, ShotActivity.class);
			startActivity(intent);
		}

	}

	private class OnEditButtonClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			//TODO: do something
		}

	}

	private class OnShareButtonClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
			sharingIntent.setType("image/jpeg");
			sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + mChoosenImage));
			startActivity(Intent.createChooser(sharingIntent, "Share Image"));
		}

	}

	private class OnCloseButtonClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			mActionLayout.setVisibility(View.GONE);
			mSwitchLayout.setVisibility(View.VISIBLE);
			mChoosenImage = null;
		}

	}

}
