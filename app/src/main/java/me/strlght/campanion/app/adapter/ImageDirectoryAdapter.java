package me.strlght.campanion.app.adapter;

import android.content.Context;
import android.os.FileObserver;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import me.strlght.campanion.app.R;
import me.strlght.campanion.app.observer.ImageObserver;
import me.strlght.campanion.app.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by StarLight on 10/5/14.
 */
public class ImageDirectoryAdapter extends RecyclerView.Adapter<ImageDirectoryAdapter.ViewHolder> {

	@SuppressWarnings("UnusedDeclaration")
	private static final String TAG = "ImageDirectoryAdapter";
	private final List<File> mImages;
	private Context mContext;
	private List<Boolean> mSelected;
	private String mPath = null;
	private ImageObserver mObserver;

	public ImageDirectoryAdapter(Context context, String path) {
		mContext = context;
		mPath = path;
		mSelected = new ArrayList<Boolean>();
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

					clearSelection();
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

	public File getItem(int position) {
		return mImages.get(position);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View v = LayoutInflater.from(mContext).inflate(R.layout.li_gallery, viewGroup, false);
		FrameLayout imageLayout = (FrameLayout) v.findViewById(R.id.image_layout);
		ImageView imageView = (ImageView) v.findViewById(R.id.image_view);
		return new ViewHolder(imageLayout, imageView);
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int i) {
		if (mSelected.get(i)) {
			viewHolder.mFrameLayout.setForeground(mContext.getResources().getDrawable(R.drawable.selector_gallery));
		} else {
			viewHolder.mFrameLayout.setForeground(mContext.getResources().getDrawable(R.color.transparent));
		}

		Picasso.with(mContext)
				.load(new File(mImages.get(i).getAbsolutePath()))
				.resize(256, 256)
				.centerCrop()
				.into(viewHolder.mImageView);
	}

	@Override
	public long getItemId(int i) {
		return mImages.get(i).hashCode();
	}

	@Override
	public int getItemCount() {
		return mImages.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {

		private final FrameLayout mFrameLayout;
		private final ImageView mImageView;

		public ViewHolder(FrameLayout frameLayout, ImageView imageView) {
			super(frameLayout);
			mFrameLayout = frameLayout;
			mImageView = imageView;
		}

	}

}
