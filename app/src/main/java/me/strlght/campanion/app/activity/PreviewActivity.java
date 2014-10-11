package me.strlght.campanion.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import me.strlght.campanion.app.R;
import me.strlght.campanion.app.adapter.ImageDirectoryPagerAdapter;
import me.strlght.campanion.app.util.FileUtils;
import me.strlght.campanion.app.util.ShareUtils;

import java.io.File;

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

		Button editButton = (Button) findViewById(R.id.edit_button);
		Button shareButton = (Button) findViewById(R.id.share_button);
		shareButton.setOnClickListener(new OnShareButtonClickListener());
		Button deleteButton = (Button) findViewById(R.id.delete_button);
		deleteButton.setOnClickListener(new OnDeleteButtonClickListener());
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

	private class OnShareButtonClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			ImageDirectoryPagerAdapter adapter = (ImageDirectoryPagerAdapter) mPreviewPager.getAdapter();
			File file = adapter.getImage(mPreviewPager.getCurrentItem());
			ShareUtils.shareImage(PreviewActivity.this, file);
		}

	}

	private class OnDeleteButtonClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			ImageDirectoryPagerAdapter adapter = (ImageDirectoryPagerAdapter) mPreviewPager.getAdapter();
			File selection = adapter.getImage(mPreviewPager.getCurrentItem());
			if (!FileUtils.delete(selection)) {
				Toast.makeText(getBaseContext(), R.string.delete_fail, Toast.LENGTH_SHORT).show();
			}
		}

	}

}
