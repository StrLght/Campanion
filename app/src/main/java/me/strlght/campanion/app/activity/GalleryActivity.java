package me.strlght.campanion.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import me.strlght.campanion.app.R;
import me.strlght.campanion.app.adapter.ImageDirectoryAdapter;
import me.strlght.campanion.app.util.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by starlight on 10/5/14.
 */
public class GalleryActivity extends Activity {

	private final static long sDoubleTapInterval = 400;
	private static final int REQUEST_CODE = 1;
	private final String mDirectory = FileUtils.getSaveDirectory().getAbsolutePath();
	private GridView mGridView;
	private LinearLayout mActionLayout;
	private RelativeLayout mSwitchLayout;
	private Button mEditButton;
	private int mLastSelectedElement = -1;
	private long mLastSelectedTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_gallery);

		Button cameraButton = (Button) findViewById(R.id.camera_button);
		cameraButton.setOnClickListener(new OnCameraButtonClickListener());
		Button addButton = (Button) findViewById(R.id.add_button);
		addButton.setOnClickListener(new OnAddButtonClickListener());

		mActionLayout = (LinearLayout) findViewById(R.id.action_layout);
		mEditButton = (Button) findViewById(R.id.edit_button);
		mEditButton.setOnClickListener(new OnEditButtonClickListener());
		Button shareButton = (Button) findViewById(R.id.share_button);
		shareButton.setOnClickListener(new OnShareButtonClickListener());
		Button deleteButton = (Button) findViewById(R.id.delete_button);
		deleteButton.setOnClickListener(new OnDeleteButtonClickListener());
		Button closeButton = (Button) findViewById(R.id.close_button);
		closeButton.setOnClickListener(new OnCloseButtonClickListener());

		mSwitchLayout = (RelativeLayout) findViewById(R.id.switch_layout);

		mGridView = (GridView) findViewById(R.id.pictures_view);
		mGridView.setOnItemClickListener(new OnItemClickListener());
	}

	@Override
	protected void onResume() {
		super.onResume();

		init();
	}

	private void init() {
		mGridView.setAdapter(new ImageDirectoryAdapter(getBaseContext(),
				mDirectory));
	}

	@Override
	protected void onPause() {
		super.onPause();

		free();
	}

	private void free() {
		closeActionLayout();
		ImageDirectoryAdapter adapter = (ImageDirectoryAdapter) mGridView.getAdapter();
		if (adapter != null) {
			adapter.stopWatching();
		}
		mGridView.setAdapter(null);
	}

	private void closeActionLayout() {
		mActionLayout.setVisibility(View.GONE);
		mSwitchLayout.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Context context = getBaseContext();
		if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			try {
				InputStream stream = getContentResolver().openInputStream(data.getData());
				Bitmap bitmap = BitmapFactory.decodeStream(stream);
				stream.close();
				FileUtils.save(context, bitmap);
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Toast.makeText(context, R.string.import_fail, Toast.LENGTH_SHORT).show();
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
			ImageDirectoryAdapter adapter = (ImageDirectoryAdapter) mGridView.getAdapter();
			long time = System.currentTimeMillis();
			List<File> selected = adapter.getSelected();
			boolean isOneOrLessSelected = (selected.size() <= 1);
			if (i == mLastSelectedElement && (time - mLastSelectedTime) <= sDoubleTapInterval && isOneOrLessSelected) {
				if (selected.size() == 1) {
					Intent intent = new Intent(GalleryActivity.this, PreviewActivity.class);
					intent.putExtra(PreviewActivity.EXTRA_IMAGE_POSITION, i);
					intent.putExtra(PreviewActivity.EXTRA_IMAGE_DIRECTORY, mDirectory);
					startActivity(intent);
					return;
				}
			} else {
				adapter.setSelected(i, !adapter.isSelected(i));
				mLastSelectedElement = i;
				mLastSelectedTime = time;
			}
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

	private class OnAddButtonClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			startActivityForResult(intent, REQUEST_CODE);
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
			ImageDirectoryAdapter adapter = (ImageDirectoryAdapter) mGridView.getAdapter();
			List<File> files = adapter.getSelected();
			if (files.size() == 1) {
				sharingIntent = new Intent(Intent.ACTION_SEND);
				sharingIntent.setType("image/*");
				sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + files.get(0).getAbsolutePath()));
				message = "Share image";
			} else {
				sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
				sharingIntent.setType("image/*");
				ArrayList<Uri> uris = new ArrayList<Uri>();
				for (File file : files) {
					uris.add(Uri.parse("file://" + file.getAbsolutePath()));
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
			ImageDirectoryAdapter adapter = (ImageDirectoryAdapter) mGridView.getAdapter();
			List<File> selection = adapter.getSelected();
			for (File image : selection) {
				if (!FileUtils.delete(image)) {
					Toast.makeText(getBaseContext(), R.string.delete_fail, Toast.LENGTH_SHORT).show();
				}
			}

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
