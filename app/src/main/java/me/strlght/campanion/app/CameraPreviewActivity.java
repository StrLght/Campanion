package me.strlght.campanion.app;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by starlight on 9/22/14.
 */
public class CameraPreviewActivity extends SurfaceView implements SurfaceHolder.Callback {

	private Camera mCamera;
	private SurfaceHolder mHolder;

	CameraPreviewActivity(Context context, Camera camera) {
		super(context);

		mCamera = camera;

		mHolder = getHolder();
		mHolder.addCallback(this);
	}

	private void stopPreview() {
		if (mCamera == null) {
			return;
		}

		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			Log.d(getContext().getString(R.string.app_name), "Failed to stop preview");
		}
	}

	private void startPreview(SurfaceHolder holder) {
		if (mCamera == null) {
			return;
		}

		try {
			mCamera.setPreviewDisplay(holder);
			fixPreview();
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d(getContext().getString(R.string.app_name), "Failed to start preview");
		}
	}

	private void fixPreview() {
		//TODO: write this
	}

	public void setCamera(Camera camera) {
		stopPreview();

		mCamera = camera;

		startPreview(mHolder);
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		startPreview(surfaceHolder);
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
		if (surfaceHolder.getSurface() == null) {
			return;
		}

		stopPreview();

		startPreview(surfaceHolder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		stopPreview();

		mCamera = null;
	}
}
