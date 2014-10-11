package me.strlght.campanion.app.adapter;

import android.content.Context;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import me.strlght.campanion.app.R;
import me.strlght.campanion.app.observer.ImageObserver;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by StarLight on 10/5/14.
 */
public class ImageDirectoryAdapter extends BaseAdapter {

	private static final String TAG = "ImageDirectoryAdapter";

	private final Handler mHandler = new Handler(Looper.getMainLooper());
	private Context mContext;
	private List<File> mImages;
	private List<Boolean> mSelected;
	private String mPath = null;
	private ImageObserver mObserver;

	public ImageDirectoryAdapter(Context context, String path) {
		mContext = context;
		mPath = path;
		mSelected = new ArrayList<Boolean>();
		mImages = new ArrayList<File>();

		int flags = FileObserver.CREATE | FileObserver.DELETE | FileObserver.DELETE_SELF
				| FileObserver.MODIFY | FileObserver.MOVE_SELF | FileObserver.MOVED_FROM
				| FileObserver.MOVED_FROM;
		mObserver = new ImageObserver(path, flags);
		mObserver.setCallback(new ImageObserver.ImageObserverCallback() {

			@Override
			public void onDirectoryChange(String[] files) {
				mImages.clear();
				for (String file : files) {
					mImages.add(new File(mPath + File.separator + file));
				}
				Collections.reverse(mImages);

				clearSelection();
			}

		});
		mObserver.onEvent(0, null);
		mObserver.startWatching();
	}

	@Override
	public void notifyDataSetChanged() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				ImageDirectoryAdapter.super.notifyDataSetChanged();
			}

		});
	}

	public void startWatching() {
		mObserver.startWatching();
	}

	public void stopWatching() {
		mObserver.stopWatching();
	}

	public void clearSelection() {
		mSelected.clear();
		for (int i = 0; i < mImages.size(); i++) {
			mSelected.add(false);
		}
		notifyDataSetChanged();
	}

	public boolean isAnySelected() {
		for (boolean isSelected : mSelected) {
			if (isSelected) {
				return true;
			}
		}
		return false;
	}

	public int getFirstSelectedIndex() {
		for (int i = 0; i < mSelected.size(); i++) {
			if (mSelected.get(i)) {
				return i;
			}
		}
		return -1;
	}

	public void setSelected(int i, boolean selected) {
		mSelected.set(i, selected);
		notifyDataSetChanged();
	}

	public boolean isSelected(int i) {
		return mSelected.get(i);
	}

	public List<File> getSelected() {
		List<File> images = new ArrayList<File>();
		for (int i = 0; i < mImages.size(); i++) {
			if (mSelected.get(i)) {
				images.add(mImages.get(i));
			}
		}
		return images;
	}

	@Override
	public int getCount() {
		return mImages.size();
	}

	@Override
	public Object getItem(int i) {
		return mImages.get(i);
	}

	@Override
	public long getItemId(int i) {
		return mImages.get(i).hashCode();
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		final String image = mImages.get(i).getAbsolutePath();

		View v = view;
		if (v == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
			v = layoutInflater.inflate(R.layout.li_gallery, viewGroup, false);
		}

		FrameLayout imageLayout = (FrameLayout) v.findViewById(R.id.image_layout);
		ImageView imageView = (ImageView) v.findViewById(R.id.image_view);

		if (mSelected.get(i)) {
			imageLayout.setForeground(mContext.getResources().getDrawable(R.drawable.selector_gallery));
		} else {
			imageLayout.setForeground(mContext.getResources().getDrawable(R.color.transparent));
		}

		Picasso.with(mContext)
				.load(new File(image))
				.resize(256, 256)
				.centerCrop()
				.into(imageView);

		return v;
	}

}
