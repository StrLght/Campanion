package me.strlght.campanion.app.view;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import me.strlght.campanion.app.R;

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
	private Camera.ShutterCallback mCameraShutterCallback;
	private Camera.PictureCallback mCameraRawPictureCallback;
	private Camera.PictureCallback mCameraPostPictureCallback;
	private Camera.PictureCallback mCameraJpegPictureCallback;
	private ShutterCallback mShutterCallback;

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

	private void init() {
		getHolder().addCallback(this);
	}

	public void setShutterCallback(ShutterCallback shutterCallback) {
		mShutterCallback = shutterCallback;
	}

	public void setCameraShutterCallback(Camera.ShutterCallback cameraShutterCallback) {
		mCameraShutterCallback = cameraShutterCallback;
	}

	public void setCameraRawPictureCallback(Camera.PictureCallback cameraRawPictureCallback) {
		mCameraRawPictureCallback = cameraRawPictureCallback;
	}

	public void setCameraPostPictureCallback(Camera.PictureCallback cameraPostPictureCallback) {
		mCameraPostPictureCallback = cameraPostPictureCallback;
	}

	public void setCameraJpegPictureCallback(Camera.PictureCallback cameraJpegPictureCallback) {
		mCameraJpegPictureCallback = cameraJpegPictureCallback;
	}

	public int getCameraFacing() {
		return mCameraFacing;
	}

	public void takePicture() {
		if (mCamera == null) {
			return;
		}

		if (mShutterCallback != null) {
			mShutterCallback.preShutter();
		}

		mCamera.takePicture(new Camera.ShutterCallback() {
			                    @Override
			                    public void onShutter() {
				                    if (mCameraShutterCallback != null) {
					                    mCameraShutterCallback.onShutter();
				                    }

				                    if (mShutterCallback != null) {
					                    mShutterCallback.postShutter();
				                    }
			                    }
		                    },
				mCameraRawPictureCallback,
				mCameraPostPictureCallback,
				new Camera.PictureCallback() {

					@Override
					public void onPictureTaken(byte[] bytes, Camera camera) {
						if (mCameraJpegPictureCallback != null) {
							mCameraJpegPictureCallback.onPictureTaken(bytes, camera);
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
			Log.d(getContext().getString(R.string.app_name), "Failed to stop preview");
		}
	}

	private void startPreview(SurfaceHolder holder) {
		if (mCamera == null || holder.getSurface() == null) {
			return;
		}

		try {
			mCamera.setPreviewDisplay(holder);
            fixPreviewSize();
            fixPreviewRotation();
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(getContext().getString(R.string.app_name), "Failed to start preview");
		}
	}

    private void fixPreviewSize() {
        if (mCamera == null || mCameraId < 0) {
            return;
        }

        final double ASPECT_TOLERANCE = 0.1f;
        double targetRatio = (double) getHeight() / getWidth();

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

        if (optimalSize != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            mCamera.setParameters(parameters);
        }
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
		startPreview(surfaceHolder);

		if (mCameraId < 0) {
			findBackFacingCamera();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
		restartPreview(surfaceHolder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		stopPreview();
	}


	public static interface ShutterCallback {
		void preShutter();

		void postShutter();
	}
}