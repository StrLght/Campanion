package me.strlght.campanion.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;
import me.strlght.campanion.app.R;
import me.strlght.campanion.app.adapter.ImageDirectoryAdapter;
import me.strlght.campanion.app.listener.OnEditButtonClickListener;
import me.strlght.campanion.app.util.FileUtils;
import me.strlght.campanion.app.util.ShareUtils;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * Created by starlight on 10/5/14.
 */
public class GalleryActivity extends Activity {

	private final static long sDoubleTapInterval = 400;
	private static final int REQUEST_CODE = 1;
	private final String mDirectory = FileUtils.getSaveDirectory().getAbsolutePath();
	private GridView mGridView;
	private Menu mMenu;
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
		setMenuActionsVisible(false);
		ImageDirectoryAdapter adapter = (ImageDirectoryAdapter) mGridView.getAdapter();
		if (adapter != null) {
			adapter.stopWatching();
		}
		mGridView.setAdapter(null);
	}

	private void setMenuActionsVisible(boolean visibility) {
		mMenu.findItem(R.id.action_edit).setEnabled(visibility);
		mMenu.findItem(R.id.action_share).setEnabled(visibility);
		mMenu.findItem(R.id.action_delete).setEnabled(visibility);
		mMenu.findItem(R.id.action_deselect).setEnabled(visibility);
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

		//TODO: add settings

		mMenu = menu;

		menu.findItem(R.id.action_edit).setOnMenuItemClickListener(new OnEditButtonClickListener(
				new OnEditButtonClickListener.EditImageGetter() {

					@Override
					public File getImage() {
						ImageDirectoryAdapter adapter = (ImageDirectoryAdapter) mGridView.getAdapter();
						return (File) adapter.getItem(adapter.getFirstSelectedIndex());
					}

					@Override
					public Activity getActivity() {
						return GalleryActivity.this;
					}

				}));

		menu.findItem(R.id.action_share).setOnMenuItemClickListener(new OnShareButtonClickListener());

		menu.findItem(R.id.action_delete).setOnMenuItemClickListener(new OnDeleteButtonClickListener());

		menu.findItem(R.id.action_deselect).setOnMenuItemClickListener(new OnCloseButtonClickListener());

		return true;
	}

	private class OnItemClickListener implements GridView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
			synchronized (this) {
				ImageDirectoryAdapter adapter = (ImageDirectoryAdapter) mGridView.getAdapter();
				long time = System.currentTimeMillis();
				List<File> selected = adapter.getSelected();
				boolean isOneOrLessSelected = (selected.size() <= 1);
				boolean isSelected = adapter.isSelected(i);
				if (i == mLastSelectedElement
						&& (time - mLastSelectedTime) <= sDoubleTapInterval
						&& isOneOrLessSelected
						&& isSelected) {
					if (selected.size() == 1) {
						Intent intent = new Intent(GalleryActivity.this, PreviewActivity.class);
						intent.putExtra(PreviewActivity.EXTRA_IMAGE_POSITION, i);
						intent.putExtra(PreviewActivity.EXTRA_IMAGE_DIRECTORY, mDirectory);
						startActivity(intent);
						return;
					}
				}
				adapter.setSelected(i, !isSelected);
				mLastSelectedElement = i;
				mLastSelectedTime = time;

				boolean isOneElementSelected = (adapter.getSelected().size() == 1);
				setMenuActionsVisible(adapter.isAnySelected());
				mMenu.findItem(R.id.action_edit).setEnabled(isOneElementSelected);
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

	private class OnShareButtonClickListener implements MenuItem.OnMenuItemClickListener {

		@Override
		public boolean onMenuItemClick(MenuItem menuItem) {
			ImageDirectoryAdapter adapter = (ImageDirectoryAdapter) mGridView.getAdapter();
			List<File> files = adapter.getSelected();
			if (files.size() == 1) {
				ShareUtils.shareImage(GalleryActivity.this, files.get(0));
			} else {
				ShareUtils.shareImages(GalleryActivity.this, files);
			}
			return true;
		}

	}

	private class OnDeleteButtonClickListener implements MenuItem.OnMenuItemClickListener {

		@Override
		public boolean onMenuItemClick(MenuItem menuItem) {
			ImageDirectoryAdapter adapter = (ImageDirectoryAdapter) mGridView.getAdapter();
			List<File> selection = adapter.getSelected();
			for (File image : selection) {
				if (!FileUtils.delete(image)) {
					Toast.makeText(getBaseContext(), R.string.delete_fail, Toast.LENGTH_SHORT).show();
				}
			}
			setMenuActionsVisible(false);
			adapter.notifyDataSetChanged();
			return true;
		}

	}

	private class OnCloseButtonClickListener implements MenuItem.OnMenuItemClickListener {

		@Override
		public boolean onMenuItemClick(MenuItem menuItem) {
			ImageDirectoryAdapter adapter = (ImageDirectoryAdapter) mGridView.getAdapter();
			adapter.clearSelection();
			setMenuActionsVisible(false);
			return true;
		}

	}

}
