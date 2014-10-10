package me.strlght.campanion.app.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import me.strlght.campanion.app.R;

import java.io.File;

/**
 * Created by starlight on 10/10/14.
 */
public class PreviewFragment extends Fragment {

	public static final String BUNDLE_FILE = "file";

	private File mImage;
	private ImageViewTouch mImageView;
	private TextView mTextView;
	private ImageViewTarget mTarget;

	public static PreviewFragment newInstance(File image) {
		PreviewFragment previewFragment = new PreviewFragment();
		Bundle args = new Bundle();
		args.putSerializable(BUNDLE_FILE, image);
		previewFragment.setArguments(args);
		return previewFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mImage = (File) getArguments().getSerializable(BUNDLE_FILE);
		mTarget = new ImageViewTarget();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.pi_preview, container, false);

		mImageView = (ImageViewTouch) v.findViewById(R.id.image_preview);
		mImageView.setDisplayType(ImageViewTouch.DisplayType.FIT_TO_SCREEN);

		mTextView = (TextView) v.findViewById(R.id.loading_text);
		mTextView.setVisibility(View.VISIBLE);

		return v;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);

		if (isVisibleToUser) {
			Picasso.with(getActivity().getBaseContext())
					.load(mImage)
					.resize(1024, 1024)
					.centerInside()
					.into(mTarget);
		} else {
			if (mTarget != null) {
				Picasso.with(getActivity().getBaseContext())
						.cancelRequest(mTarget);
			}
		}
	}

	private class ImageViewTarget implements Target {

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			mImageView.setImageBitmap(bitmap);
			mTextView.setVisibility(View.GONE);
		}

		@Override
		public void onBitmapFailed(Drawable errorDrawable) {
			mTextView.setText(R.string.loading_fail);
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {

		}

	}

}
