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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by starlight on 10/5/14.
 */
public class GalleryActivity extends Activity {

	private final static long sDoubleTapInterval = 300;

	private GridView mGridView;
	private LinearLayout mActionLayout;
	private LinearLayout mSwitchLayout;
	private Button mEditButton;
	private int mLastSelectedElement = -1;
	private long mLastSelectedTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_gallery);

		Button cameraButton = (Button) findViewById(R.id.camera_button);
		cameraButton.setOnClickListener(new OnCameraButtonClickListener());

		mActionLayout = (LinearLayout) findViewById(R.id.action_layout);
		mEditButton = (Button) findViewById(R.id.edit_button);
		mEditButton.setOnClickListener(new OnEditButtonClickListener());
		Button shareButton = (Button) findViewById(R.id.share_button);
		shareButton.setOnClickListener(new OnShareButtonClickListener());
		Button deleteButton = (Button) findViewById(R.id.delete_button);
		deleteButton.setOnClickListener(new OnDeleteButtonClickListener());
		Button closeButton = (Button) findViewById(R.id.close_button);
		closeButton.setOnClickListener(new OnCloseButtonClickListener());

		mSwitchLayout = (LinearLayout) findViewById(R.id.switch_layout);

		mGridView = (GridView) findViewById(R.id.pictures_view);
		mGridView.setOnItemClickListener(new OnItemClickListener());
	}

	@Override
	protected void onResume() {
		super.onResume();

		init();
	}

	private List<String> getFiles() {
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

		return files;
	}

	private void init() {
		mGridView.setAdapter(new ImageArrayAdapter(getBaseContext(), getFiles()));
	}

	@Override
	protected void onPause() {
		super.onPause();

		free();
	}

	private void free() {
		mGridView.setAdapter(null);
	}

	private void updateGridView() {
		ImageArrayAdapter adapter = (ImageArrayAdapter) mGridView.getAdapter();
		adapter.setImages(getFiles());
	}

	private void closeActionLayout() {
		mActionLayout.setVisibility(View.GONE);
		mSwitchLayout.setVisibility(View.VISIBLE);
		ImageArrayAdapter adapter = (ImageArrayAdapter) mGridView.getAdapter();
		adapter.clearSelection();
		adapter.notifyDataSetChanged();
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
			ImageArrayAdapter adapter = (ImageArrayAdapter) mGridView.getAdapter();
			long time = System.currentTimeMillis();
			boolean isOneOrLessSelected = (adapter.getSelected().size() <= 1);
			if (i == mLastSelectedElement && (time - mLastSelectedTime) <= sDoubleTapInterval && isOneOrLessSelected) {
				//TODO: open preview window on double tap
				return;
			} else {
				adapter.setSelected(i, !adapter.isSelected(i));
				mLastSelectedElement = i;
				mLastSelectedTime = time;
			}
			adapter.notifyDataSetChanged();
			if (adapter.isAnyChosen()) {
				mActionLayout.setVisibility(View.VISIBLE);
				mSwitchLayout.setVisibility(View.GONE);
				mEditButton.setEnabled(isOneOrLessSelected);
			} else {
				closeActionLayout();
			}
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
			Intent sharingIntent;
			String message;
			ImageArrayAdapter adapter = (ImageArrayAdapter) mGridView.getAdapter();
			List<String> files = adapter.getSelected();
			if (files.size() == 1) {
				sharingIntent = new Intent(Intent.ACTION_SEND);
				sharingIntent.setType("image/jpeg");
				sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + files.get(0)));
				message = "Share image";
			} else {
				sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
				sharingIntent.setType("image/jpeg");
				ArrayList<Uri> uris = new ArrayList<Uri>();
				for (String file : files) {
					uris.add(Uri.parse("file://" + file));
				}
				sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
				message = "Share images";
			}
			startActivity(Intent.createChooser(sharingIntent, message));
		}

	}

	private class OnDeleteButtonClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			ImageArrayAdapter adapter = (ImageArrayAdapter) mGridView.getAdapter();
			for (String image : adapter.getSelected()) {
				FileUtils.delete(image);
			}

			updateGridView();
			closeActionLayout();
		}

	}

	private class OnCloseButtonClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			closeActionLayout();
		}

	}

}
