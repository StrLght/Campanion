package me.strlght.campanion.app;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;

/**
 * Created by starlight on 9/22/14.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

	private Camera mCamera;
	private int mCameraId = -1;
	private Camera.ShutterCallback mShutterCallback;
	private Camera.PictureCallback mRawPictureCallback;
	private Camera.PictureCallback mPostPictureCallback;
	private Camera.PictureCallback mJpegPictureCallback;


	public CameraView(Context context)
	{
		super(context);
		init();
	}

	public CameraView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public CameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		getHolder().addCallback(this);
	}

	public void setShutterCallback(Camera.ShutterCallback shutterCallback) {
		mShutterCallback = shutterCallback;
	}

	public void setRawPictureCallback(Camera.PictureCallback rawPictureCallback) {
		mRawPictureCallback = rawPictureCallback;
	}

	public void setPostPictureCallback(Camera.PictureCallback postPictureCallback) {
		mPostPictureCallback = postPictureCallback;
	}

	public void setJpegPictureCallback(Camera.PictureCallback jpegPictureCallback) {
		mJpegPictureCallback = jpegPictureCallback;
	}

	public void takePicture() {
		if (mCamera == null) {
			return;
		}

		mCamera.takePicture(mShutterCallback, mRawPictureCallback, mPostPictureCallback, mJpegPictureCallback);
	}

	private void findBackFacingCamera() {
		mCameraId = -1;
		Camera.CameraInfo info = new Camera.CameraInfo();

		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			Camera.getCameraInfo(i, info);

			if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				mCameraId = i;
				break;
			}
		}

		// Device doesn't have any back-facing camera.
		if (mCameraId < 0) {
			mCameraId = 0;
		}
	}

	public void switchCamera() {
		if (mCameraId < 0) {
			return;
		}

		mCameraId = (mCameraId + 1) % Camera.getNumberOfCameras();

		stopPreview();

		releaseCamera();

		mCamera = Camera.open(mCameraId);

		startPreview(getHolder());
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release();
		}
		mCamera = null;
	}

	private void restartPreview(SurfaceHolder holder) {
		stopPreview();

		startPreview(holder);
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
		if (mCamera == null || holder.getSurface() == null) {
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
		if (mCamera == null || mCameraId < 0)
			return;

		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(mCameraId, info);

		WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		int rotation = windowManager.getDefaultDisplay().getRotation();
		int degrees = 0;

		switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;
		} else {
			result = (info.orientation - degrees + 360) % 360;
		}

		mCamera.setDisplayOrientation(result);
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		startPreview(surfaceHolder);

		if (mCameraId < 0) {
			findBackFacingCamera();
		}

		mCamera = Camera.open(mCameraId);
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
		restartPreview(surfaceHolder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		stopPreview();

		releaseCamera();
	}
}
