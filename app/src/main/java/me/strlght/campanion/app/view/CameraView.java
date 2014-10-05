package me.strlght.campanion.app.view;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;

import java.io.IOException;
import java.util.List;

/**
 * Created by starlight on 9/22/14.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = "CameraView";

	private Camera mCamera;
	private int mCameraId = -1;
	private int mCameraFacing;
	private Camera.Size mPreviewSize;
	private Camera.ShutterCallback mShutterCallback;
	private Camera.PictureCallback mRawPictureCallback;
	private Camera.PictureCallback mPostPictureCallback;
	private Camera.PictureCallback mJpegPictureCallback;
	private int mWidth = -1;
	private int mHeight = -1;

	public CameraView(Context context) {
		super(context);
		init();
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

		if (mWidth < 0 && mHeight < 0) {
			mWidth = width;
			mHeight = height;
		}
		setMeasuredDimension(mWidth, mHeight);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (changed) {
			int previewWidth = 0;
			int previewHeight = 0;

			Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

			mPreviewSize = getOptimalPreviewSize(mWidth, mHeight);

			if (mPreviewSize == null) {
				return;
			}

			switch (display.getRotation()) {
				case Surface.ROTATION_0:
				case Surface.ROTATION_180:
					previewWidth = mPreviewSize.height;
					previewHeight = mPreviewSize.width;
					break;
				case Surface.ROTATION_90:
				case Surface.ROTATION_270:
					previewWidth = mPreviewSize.width;
					previewHeight = mPreviewSize.height;
					break;
			}

			layout(mWidth / 2 - previewWidth / 2, mHeight / 2 - previewHeight / 2, previewWidth, previewHeight);
		}
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

	public int getCameraFacing() {
		return mCameraFacing;
	}

	public Camera.Size getPreviewSize() {
		return mPreviewSize;
	}

	public void takePicture() {
		if (mCamera == null) {
			return;
		}

		mCamera.takePicture(mShutterCallback,
				mRawPictureCallback,
				mPostPictureCallback,
				new Camera.PictureCallback() {

					@Override
					public void onPictureTaken(byte[] bytes, Camera camera) {
						if (mJpegPictureCallback != null) {
							mJpegPictureCallback.onPictureTaken(bytes, camera);
						}
						restartPreview(getHolder());
					}

				}
		);
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

		releaseCamera();

		openCamera();
	}

	public void openCamera() {
		releaseCamera();

		if (mCameraId < 0) {
			findBackFacingCamera();
		}

		mCamera = Camera.open(mCameraId);
		mCamera.enableShutterSound(true);
		Camera.Parameters parameters = mCamera.getParameters();
		List<String> focusModes = parameters.getSupportedFocusModes();
		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		}
		mCamera.setParameters(parameters);
		startPreview(getHolder());
	}

	public void releaseCamera() {
		stopPreview();
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
			Log.d(TAG, "Failed to stop preview");
		}
	}

	private void startPreview(SurfaceHolder holder) {
		if (mCamera == null || holder.getSurface() == null) {
			return;
		}

		try {
			mCamera.setPreviewDisplay(holder);
			fixPreviewRotation();
			requestLayout();
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d(TAG, "Failed to start preview");
		}
	}

	private Camera.Size getOptimalPreviewSize(int width, int height) {
		if (mCamera == null) {
			return null;
		}

		final double ASPECT_TOLERANCE = 0.1f;
		double targetRatio = (double) height / width;

		if (Double.isNaN(targetRatio)) {
			return null;
		}

		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;
		int targetHeight = getHeight();

		for (Camera.Size size : mCamera.getParameters().getSupportedPreviewSizes()) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
				continue;
			}
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			for (Camera.Size size : mCamera.getParameters().getSupportedPreviewSizes()) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}

		return optimalSize;
	}

	private void fixPreviewRotation() {
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
		mCameraFacing = info.facing;
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
		if (mCameraId < 0) {
			findBackFacingCamera();
			openCamera();
		}

		startPreview(surfaceHolder);
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
		restartPreview(surfaceHolder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		stopPreview();
	}

}
