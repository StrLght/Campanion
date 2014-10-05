package me.strlght.campanion.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by StarLight on 10/5/14.
 */
public class ImageArrayAdapter extends BaseAdapter {

	private Context mContext;
	private List<String> mImages;
	private int mWidth;
	private int mHeight;
	private int mPaddingLeft;
	private int mPaddingRight;
	private int mPaddingTop;
	private int mPaddingBottom;

	public ImageArrayAdapter(Context context, List<String> images) {
		this(context, images, 100, 100, 10, 10, 10, 10);
	}

	public ImageArrayAdapter(Context context, List<String> images, int width, int height, int paddingLeft, int paddingRight, int paddingTop, int paddingBottom) {
		mContext = context;
		mImages = images;
		mWidth = width;
		mHeight = height;
		mPaddingLeft = paddingLeft;
		mPaddingRight = paddingRight;
		mPaddingTop = paddingTop;
		mPaddingBottom = paddingBottom;
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

		ImageView imageView = (ImageView) view;
		if (view == null) {
			imageView = new ImageView(mContext);

		}
		imageView.setBackgroundColor(Color.parseColor("#ffffffff"));
		imageView.setLayoutParams(new GridView.LayoutParams(mWidth, mHeight));
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		imageView.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);

		Picasso.with(mContext)
				.load(mImages.get(i))
				.centerCrop()
				.into(imageView);

		return imageView;
	}

}
