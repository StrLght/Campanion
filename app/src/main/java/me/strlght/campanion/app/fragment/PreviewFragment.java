package me.strlght.campanion.app.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
	private TextView mErrorText;
	private ProgressBar mProgressBar;
	private ImageViewTarget mTarget;
	private boolean mIsVisible = false;

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

		mProgressBar = (ProgressBar) v.findViewById(R.id.loading_spinner);
		mProgressBar.setVisibility(View.VISIBLE);

		mErrorText = (TextView) v.findViewById(R.id.error_text);

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (mIsVisible) {
			load();
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);

		mIsVisible = isVisibleToUser;
		if (getView() != null) {
			if (mIsVisible) {
				load();
			} else {
				if (mTarget != null) {
					Picasso.with(getActivity().getBaseContext())
							.cancelRequest(mTarget);
				}
			}
		}
	}

	private void load() {
		Picasso.with(getActivity().getBaseContext())
				.load(mImage)
				.resize(1536, 1536)
				.centerInside()
				.into(mTarget);
	}

	private class ImageViewTarget implements Target {

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			mImageView.setImageBitmap(bitmap);
			mProgressBar.setVisibility(View.GONE);
		}

		@Override
		public void onBitmapFailed(Drawable errorDrawable) {
			mErrorText.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {

		}

	}

}
