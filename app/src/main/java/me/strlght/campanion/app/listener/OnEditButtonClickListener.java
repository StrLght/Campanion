package me.strlght.campanion.app.listener;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import com.aviary.android.feather.library.Constants;
import com.aviary.android.feather.sdk.FeatherActivity;
import me.strlght.campanion.app.BuildConfig;

import java.io.File;

/**
 * Created by starlight on 10/11/14.
 */
public class OnEditButtonClickListener implements MenuItem.OnMenuItemClickListener {

	public static final int AVIARY_ACTIVITY = 10;

	private EditImageGetter mEditImageGetter;

	public OnEditButtonClickListener(EditImageGetter editImageGetter) {
		mEditImageGetter = editImageGetter;
	}

	@Override
	public boolean onMenuItemClick(MenuItem menuItem) {
		if (mEditImageGetter != null) {
			File image = mEditImageGetter.getImage();
			Activity activity = mEditImageGetter.getActivity();
			Intent intent = new Intent(activity, FeatherActivity.class);
			intent.setData(Uri.parse("file://" + image.getAbsolutePath()));
			intent.putExtra(Constants.EXTRA_IN_API_KEY_SECRET, BuildConfig.AVIARY_SECRET);
			activity.startActivityForResult(intent, AVIARY_ACTIVITY);
			return true;
		}
		return false;
	}

	public interface EditImageGetter {
		public File getImage();

		public Activity getActivity();
	}

}
