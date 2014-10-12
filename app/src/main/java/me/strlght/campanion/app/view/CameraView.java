package me.strlght.campanion.app.view;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

/**
 * Created by starlight on 9/22/14.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = "CameraView";

	private Camera mCamera;
	private int mCameraId = -1;
	private Camera.CameraInfo mCameraInfo;
	private Camera.Size mPreviewSize;
	private Camera.ShutterCallback mShutterCallback;
	private Camera.PictureCallback mRawPictureCallback;
	private Camera.PictureCallback mPostPictureCallback;
	private Camera.PictureCallback mJpegPictureCallback;
	private OnPreviewSizeChangeListener mOnPreviewSizeChangeListener;

	private int mParentWidth = -1;
	private int mParentHeight = -1;

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

		if (mParentHeight < height || mParentWidth < width) {
			mParentWidth = width;
			mParentHeight = height;
		}

		int previewWidth = width;
		int previewHeight = height;

		updatePreviewSize();
		if (mCameraInfo != null && mPreviewSize != null) {
			switch (mCameraInfo.orientation) {
				case 90:
				case 270:
					previewWidth = mPreviewSize.height;
					previewHeight = mPreviewSize.width;
					break;
				case 0:
				case 180:
					previewWidth = mPreviewSize.width;
					previewHeight = mPreviewSize.height;
					break;
			}
		}

		float ratio = (float) previewHeight / previewWidth;


		setMeasuredDimension(mParentWidth, (int) (mParentWidth * ratio));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		setPreviewSize(getOptimalPreviewSize(w, h));
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
		return mCameraInfo.facing;
	}

	public Camera.Size getPreviewSize() {
		return mPreviewSize;
	}

	private void setPreviewSize(Camera.Size previewSize) {
		mPreviewSize = previewSize;
		if (mOnPreviewSizeChangeListener != null) {
			mOnPreviewSizeChangeListener.onPreviewSizeChanged(mPreviewSize);
		}
	}

	public void setOnPreviewSizeChangeListener(OnPreviewSizeChangeListener onPreviewSizeChangeListener) {
		mOnPreviewSizeChangeListener = onPreviewSizeChangeListener;
		setPreviewSize(mPreviewSize);
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
		mCameraInfo = new Camera.CameraInfo();
		Camera.getCameraInfo(mCameraId, mCameraInfo);
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setJpegQuality(100);
		List<String> focusModes = parameters.getSupportedFocusModes();
		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		}
		mCamera.setParameters(parameters);
		updatePreviewSize();
		setBestPictureQuality();
		startPreview(getHolder());
	}

	private void updatePreviewSize() {
		Camera.Parameters parameters = mCamera.getParameters();
		setPreviewSize(getOptimalPreviewSize(mParentWidth, mParentHeight));
		if (mPreviewSize != null) {
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		}
		mCamera.setParameters(parameters);
	}

	public void releaseCamera() {
		stopPreview();
		if (mCamera != null) {
			mCamera.release();
		}
		mCamera = null;
		mCameraInfo = null;
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
		if (mCamera == null || holder == null) {
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

	private void setBestPictureQuality() {
		int surface = -1;
		Camera.Size bestSize = null;
		Camera.Parameters parameters = mCamera.getParameters();
		for (Camera.Size size : parameters.getSupportedPictureSizes()) {
			if (size.width * size.height >= surface) {
				surface = size.width * size.height;
				bestSize = size;
			}
		}
		parameters.setPictureSize(bestSize.width, bestSize.height);
		mCamera.setParameters(parameters);
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
		if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (mCameraInfo.orientation + degrees) % 360;
			result = (360 - result) % 360;
		} else {
			result = (mCameraInfo.orientation - degrees + 360) % 360;
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

	public interface OnPreviewSizeChangeListener {
		public void onPreviewSizeChanged(Camera.Size size);
	}

}
