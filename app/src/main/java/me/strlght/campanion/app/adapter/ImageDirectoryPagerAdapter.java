package me.strlght.campanion.app.adapter;

import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import me.strlght.campanion.app.fragment.PreviewFragment;
import me.strlght.campanion.app.observer.ImageObserver;
import me.strlght.campanion.app.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by starlight on 10/10/14.
 */
@SuppressWarnings("UnusedDeclaration")
public class ImageDirectoryPagerAdapter extends FragmentStatePagerAdapter {

	private static final String TAG = "ImageDirectoryPagerAdapter";

	private final Handler mHandler = new Handler(Looper.getMainLooper());
	private final List<File> mImages;
	private String mPath = null;
	private ImageObserver mObserver;

	public ImageDirectoryPagerAdapter(FragmentManager fragmentManager, String path) {
		super(fragmentManager);
		mPath = path;
		mImages = new ArrayList<File>();

		int flags = FileObserver.CREATE | FileObserver.DELETE | FileObserver.DELETE_SELF
				| FileObserver.MODIFY | FileObserver.MOVE_SELF | FileObserver.MOVED_FROM;
		mObserver = new ImageObserver(path, flags);
		mObserver.setCallback(new ImageObserver.ImageObserverCallback() {

			@Override
			public void onDirectoryChange(String[] files) {
				synchronized (mImages) {
					mImages.clear();
					for (String file : files) {
						mImages.add(new File(mPath + File.separator + file));
					}
					Collections.sort(mImages, new FileUtils.Comparator());

					mHandler.post(new Runnable() {

						@Override
						public void run() {
							notifyDataSetChanged();
						}

					});
				}
			}

		});
		startWatching();
	}

	public void startWatching() {
		mObserver.onEvent(0, null);
		mObserver.startWatching();
	}

	public void stopWatching() {
		mObserver.stopWatching();
	}

	public File getImage(int i) {
		return mImages.get(i);
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public int getCount() {
		return mImages.size();
	}

	@Override
	public Fragment getItem(int i) {
		return PreviewFragment.newInstance(mImages.get(i));
	}

}
