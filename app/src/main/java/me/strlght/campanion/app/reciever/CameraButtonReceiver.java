package me.strlght.campanion.app.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by starlight on 10/14/14.
 */
public class CameraButtonReceiver extends BroadcastReceiver {

	private CameraButtonCallback mCameraButtonCallback;

	public CameraButtonReceiver(CameraButtonCallback cameraButtonCallback) {
		mCameraButtonCallback = cameraButtonCallback;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (mCameraButtonCallback != null) {
			mCameraButtonCallback.onReceive();
			abortBroadcast();
		}
	}

	public interface CameraButtonCallback {
		public void onReceive();
	}

}
