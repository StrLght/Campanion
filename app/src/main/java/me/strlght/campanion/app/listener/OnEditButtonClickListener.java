package me.strlght.campanion.app.listener;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import me.strlght.campanion.app.activity.EditActivity;

import java.io.File;

/**
 * Created by starlight on 10/11/14.
 */
public class OnEditButtonClickListener implements MenuItem.OnMenuItemClickListener {

	private EditImageGetter mEditImageGetter;

	public OnEditButtonClickListener(EditImageGetter editImageGetter) {
		mEditImageGetter = editImageGetter;
	}

	@Override
	public boolean onMenuItemClick(MenuItem menuItem) {
		if (mEditImageGetter != null) {
			File image = mEditImageGetter.getImage();
			Activity activity = mEditImageGetter.getActivity();
			Intent intent = new Intent(activity, EditActivity.class);
			intent.putExtra(EditActivity.EXTRA_IMAGE, image);
			activity.startActivity(intent);
			return true;
		}
		return false;
	}

	public interface EditImageGetter {
		public File getImage();

		public Activity getActivity();
	}

}
