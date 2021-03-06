package me.strlght.campanion.app.observer;

import android.os.FileObserver;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by starlight on 10/10/14.
 */
@SuppressWarnings("UnusedDeclaration")
public class ImageObserver extends FileObserver {

	private final Object mWaitLock = new Object();
	private String mPath;
	private ImageObserverCallback mCallback;
	private Boolean mShouldWait = false;

	public ImageObserver(String path) {
		super(path);
		mPath = path;
	}

	public ImageObserver(String path, int flags) {
		super(path, flags);
		mPath = path;
	}

	public void setCallback(ImageObserverCallback callback) {
		mCallback = callback;
	}

	@Override
	public void onEvent(final int i, final String s) {
		if ((i & FileObserver.MOVE_SELF) != 0) {
			mPath = s;
		}
		synchronized (mWaitLock) {
			if ((i & FileObserver.CLOSE_WRITE) != 0) {
				mShouldWait = false;
			} else if ((i & FileObserver.MODIFY) != 0) {
				mShouldWait = true;
			}
		}
		if (mShouldWait) {
			return;
		}
		String[] strings = new File(mPath).list(new FilenameFilter() {

			@Override
			public boolean accept(File file, String s) {
				String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath() + s);
				if (extension != null) {
					MimeTypeMap mime = MimeTypeMap.getSingleton();
					if (mime.getMimeTypeFromExtension(extension).startsWith("image")) {
						return true;
					}
				}
				return false;
			}

		});

		if (mCallback != null) {
			mCallback.onDirectoryChange(strings);
		}
	}

	public interface ImageObserverCallback {
		public void onDirectoryChange(String[] files);
	}

}