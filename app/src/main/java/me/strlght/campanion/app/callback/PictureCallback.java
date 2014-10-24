package me.strlght.campanion.app.callback;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by starlight on 10/5/14.
 */
public abstract class PictureCallback implements Camera.PictureCallback {

	public static final String TAG = "PictureCallback";
	private static HandlerThread mBackgroundThread = new HandlerThread(TAG);
	private static Handler mBackgroundHandler;
	private Context mContext;
	private float mRoll;
	private int mFacing;

	static {
		mBackgroundThread.start();
		mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
	}

	public static Handler getBackgroundHandler() {
		return mBackgroundHandler;
	}

	public Context getContext() {
		return mContext;
	}

	public void setContext(Context context) {
		mContext = context;
	}

	public float getRoll() {
		return mRoll;
	}

	public void setRoll(float roll) {
		mRoll = roll;
	}

	public int getFacing() {
		return mFacing;
	}

	public void setFacing(int facing) {
		mFacing = facing;
	}
}
