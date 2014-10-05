package me.strlght.campanion.app.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import me.strlght.campanion.app.R;

import java.io.File;
import java.util.List;

/**
 * Created by StarLight on 10/5/14.
 */
public class ImageArrayAdapter extends BaseAdapter {

	public static final String TAG = "ImageArrayAdapter";

	private Context mContext;
	private List<String> mImages;

	public ImageArrayAdapter(Context context, List<String> images) {
		mContext = context;
		mImages = images;
	}

	@Override
	public int getCount() {
		if (mImages != null) {
			return mImages.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int i) {
		if (mImages != null) {
			return mImages.get(i);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int i) {
		if (mImages != null) {
			return mImages.get(i).hashCode();
		} else {
			return 0;
		}
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (mImages == null) {
			return null;
		}

		View v = view;
		if (v == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
			v = layoutInflater.inflate(R.layout.li_gallery, viewGroup, false);
			// v.setLayoutParams(new GridView.LayoutParams(256, 256));
		}
		ImageView imageView = (ImageView) v.findViewById(R.id.image_view);

		Log.d(TAG, mImages.get(i));

		Picasso.with(mContext)
				.load(new File(mImages.get(i)))
				.resize(256, 256)
				.centerInside()
				.into(imageView);

		return v;
	}

}
