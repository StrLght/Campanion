package me.strlght.campanion.app.callback;

import android.content.Context;
import android.hardware.Camera;

/**
 * Created by starlight on 10/5/14.
 */
public abstract class PictureCallback implements Camera.PictureCallback {

	private Context mContext;
	private float mRoll;
	private int mFacing;

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
