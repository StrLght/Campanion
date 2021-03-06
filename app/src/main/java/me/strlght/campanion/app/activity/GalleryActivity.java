package me.strlght.campanion.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import me.strlght.campanion.app.R;
import me.strlght.campanion.app.adapter.ImageDirectoryAdapter;
import me.strlght.campanion.app.listener.OnEditButtonClickListener;
import me.strlght.campanion.app.listener.RecyclerItemClickListener;
import me.strlght.campanion.app.util.AviaryUtils;
import me.strlght.campanion.app.util.FileUtils;
import me.strlght.campanion.app.util.ShareUtils;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * Created by starlight on 10/5/14.
 */
public class GalleryActivity extends Activity {

	private static final int REQUEST_CODE = 1;
	private final String mDirectory = FileUtils.getSaveDirectory().getAbsolutePath();
	private RecyclerView mGridView;
	private ActionMode mActionMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_gallery);

		ImageButton cameraButton = (ImageButton) findViewById(R.id.camera_button);
		cameraButton.setOnClickListener(new OnCameraButtonClickListener());
		ImageButton addButton = (ImageButton) findViewById(R.id.add_button);
		addButton.setOnClickListener(new OnAddButtonClickListener());

		mGridView = (RecyclerView) findViewById(R.id.pictures_view);
		Point p = new Point();
		getWindowManager().getDefaultDisplay().getSize(p);
		int gridWidth = p.x - mGridView.getPaddingStart() - mGridView.getPaddingEnd();
		int elementWidth = getResources().getDimensionPixelSize(R.dimen.grid_size);
		int count = gridWidth / elementWidth;
		mGridView.setLayoutManager(new GridLayoutManager(this, count));
		mGridView.addOnItemTouchListener(new RecyclerItemClickListener(this, new OnItemClickListener()));
	}

	@Override
	protected void onResume() {
		super.onResume();

		init();
	}

	private void init() {
		mGridView.setAdapter(new ImageDirectoryAdapter(this, mDirectory));
	}

	@Override
	protected void onPause() {
		super.onPause();

		free();
	}

	private void free() {
		setMenuActionsEnabled(false);
		ImageDirectoryAdapter adapter = (ImageDirectoryAdapter) mGridView.getAdapter();
		if (adapter != null) {
			adapter.stopWatching();
		}
		mGridView.setAdapter(null);
	}

	private void setMenuActionsEnabled(boolean visibility) {
		if (mActionMenu == null && visibility) {
			mActionMenu = startActionMode(new ActionMenu());
		} else if (mActionMenu != null && !visibility) {
			mActionMenu.finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Context context = getApplicationContext();
		if (requestCode == REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				try {
					InputStream stream = getContentResolver().openInputStream(data.getData());
					Bitmap bitmap = BitmapFactory.decodeStream(stream);
					stream.close();
					FileUtils.save(context, bitmap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (requestCode == OnEditButtonClickListener.AVIARY_ACTIVITY && resultCode == Activity.RESULT_OK) {
			AviaryUtils.saveUriIfChanged(context, data);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gallery, menu);

		//TODO: add settings
		return true;
	}

	private class ActionMenu implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			getMenuInflater().inflate(R.menu.gallery_action, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			final ImageDirectoryAdapter adapter = (ImageDirectoryAdapter) mGridView.getAdapter();
			switch (item.getItemId()) {
				case R.id.action_edit:
					OnEditButtonClickListener clickListener = new OnEditButtonClickListener(
							new OnEditButtonClickListener.EditImageGetter() {

								@Override
								public File getImage() {
									ImageDirectoryAdapter adapter = (ImageDirectoryAdapter) mGridView.getAdapter();
									return adapter.getItem(adapter.getFirstSelectedIndex());
								}

								@Override
								public Activity getActivity() {
									return GalleryActivity.this;
								}

							});
					clickListener.onMenuItemClick(item);
					mode.finish();
					return true;
				case R.id.action_share:
					List<File> files = adapter.getSelected();
					if (files.size() == 1) {
						ShareUtils.shareImage(GalleryActivity.this, files.get(0));
					} else {
						ShareUtils.shareImages(GalleryActivity.this, files);
					}
					mode.finish();
					return true;
				case R.id.action_delete:
					final List<File> selection = adapter.getSelected();
					AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
					builder.setMessage(R.string.delete_question)
							.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									adapter.stopWatching();
									for (File image : selection) {
										if (!FileUtils.delete(image)) {
											Toast.makeText(getBaseContext(),
													R.string.delete_fail, Toast.LENGTH_SHORT)
													.show();
										}
									}
									adapter.startWatching();
								}

							})
							.setNegativeButton(android.R.string.no, null)
							.create()
							.show();
					mode.finish();
					return true;
				default:
					return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMenu = null;
			ImageDirectoryAdapter adapter = (ImageDirectoryAdapter) mGridView.getAdapter();
			adapter.clearSelection();
		}
	}

	private class OnItemClickListener implements RecyclerItemClickListener.OnItemClickListener {

		@Override
		public void onItemClick(View view, int i) {
			ImageDirectoryAdapter adapter = (ImageDirectoryAdapter) mGridView.getAdapter();
			boolean isSelected = adapter.isSelected(i);
			adapter.setSelected(i, !isSelected);
			boolean isOneElementSelected = (adapter.getSelected().size() == 1);
			setMenuActionsEnabled(adapter.isAnySelected());
			if (mActionMenu != null) {
				MenuItem item = mActionMenu.getMenu().findItem(R.id.action_edit);
				if (item != null) {
					item.setEnabled(isOneElementSelected);
				}
			}
		}

		@Override
		public void onDoubleItemClick(View view, int position) {
			ImageDirectoryAdapter adapter = (ImageDirectoryAdapter) mGridView.getAdapter();
			List<File> selected = adapter.getSelected();
			if (selected.size() == 1 && adapter.isSelected(position)) {
				Intent intent = new Intent(GalleryActivity.this, PreviewActivity.class);
				intent.putExtra(PreviewActivity.EXTRA_IMAGE_POSITION, position);
				intent.putExtra(PreviewActivity.EXTRA_IMAGE_DIRECTORY, mDirectory);
				startActivity(intent);
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

}
