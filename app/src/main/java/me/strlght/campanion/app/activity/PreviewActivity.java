package me.strlght.campanion.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import me.strlght.campanion.app.R;
import me.strlght.campanion.app.adapter.ImageDirectoryPagerAdapter;
import me.strlght.campanion.app.listener.OnEditButtonClickListener;
import me.strlght.campanion.app.util.AviaryUtils;
import me.strlght.campanion.app.util.FileUtils;
import me.strlght.campanion.app.util.ShareUtils;

import java.io.File;

/**
 * Created by starlight on 10/9/14.
 */
public class PreviewActivity extends FragmentActivity {

	public static final String EXTRA_IMAGE_POSITION = "image_position";
	public static final String EXTRA_IMAGE_DIRECTORY = "image_directory";
	private static final String TAG = "PreviewActivity";
	private ViewPager mPreviewPager;
	private int mPosition;
	private String mDirectory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_preview);

		Intent intent = getIntent();
		mDirectory = intent.getStringExtra(EXTRA_IMAGE_DIRECTORY);

		if (savedInstanceState == null) {
			mPosition = intent.getIntExtra(EXTRA_IMAGE_POSITION, 0);
		} else {
			mPosition = savedInstanceState.getInt(EXTRA_IMAGE_POSITION);
		}

		mPreviewPager = (ViewPager) findViewById(R.id.preview_pager);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mPreviewPager.setAdapter(new ImageDirectoryPagerAdapter(getSupportFragmentManager(), mDirectory));
		mPreviewPager.setCurrentItem(mPosition);
	}

	@Override
	protected void onPause() {
		super.onPause();

		mPosition = mPreviewPager.getCurrentItem();
		ImageDirectoryPagerAdapter adapter = (ImageDirectoryPagerAdapter) mPreviewPager.getAdapter();
		adapter.stopWatching();
		mPreviewPager.setAdapter(null);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(EXTRA_IMAGE_POSITION, mPosition);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.preview, menu);

		//TODO: add settings

		menu.findItem(R.id.action_edit).setOnMenuItemClickListener(new OnEditButtonClickListener(
				new OnEditButtonClickListener.EditImageGetter() {

					@Override
					public File getImage() {
						ImageDirectoryPagerAdapter adapter = (ImageDirectoryPagerAdapter) mPreviewPager.getAdapter();
						return adapter.getImage(mPreviewPager.getCurrentItem());
					}

					@Override
					public Activity getActivity() {
						return PreviewActivity.this;
					}

				}
		));

		menu.findItem(R.id.action_share).setOnMenuItemClickListener(new OnShareButtonClickListener());

		menu.findItem(R.id.action_delete).setOnMenuItemClickListener(new OnDeleteButtonClickListener());

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Context context = getApplicationContext();
		if (requestCode == OnEditButtonClickListener.AVIARY_ACTIVITY && resultCode == Activity.RESULT_OK) {
			AviaryUtils.saveUriIfChanged(context, data);
		}
	}

	private class OnShareButtonClickListener implements MenuItem.OnMenuItemClickListener {

		@Override
		public boolean onMenuItemClick(MenuItem menuItem) {
			ImageDirectoryPagerAdapter adapter = (ImageDirectoryPagerAdapter) mPreviewPager.getAdapter();
			File file = adapter.getImage(mPreviewPager.getCurrentItem());
			ShareUtils.shareImage(PreviewActivity.this, file);
			return true;
		}

	}

	private class OnDeleteButtonClickListener implements MenuItem.OnMenuItemClickListener {

		@Override
		public boolean onMenuItemClick(MenuItem menuItem) {
			AlertDialog.Builder builder = new AlertDialog.Builder(PreviewActivity.this);
			builder.setMessage(R.string.delete_question)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							ImageDirectoryPagerAdapter adapter = (ImageDirectoryPagerAdapter) mPreviewPager.getAdapter();
							File selection = adapter.getImage(mPreviewPager.getCurrentItem());
							if (!FileUtils.delete(selection)) {
								Toast.makeText(getBaseContext(), R.string.delete_fail, Toast.LENGTH_SHORT).show();
							}
						}

					})
					.setNegativeButton(android.R.string.no, null)
					.create()
					.show();

			return true;
		}

	}

}
