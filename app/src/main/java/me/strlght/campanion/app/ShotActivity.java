package me.strlght.campanion.app;

import android.app.ActionBar;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class ShotActivity extends Activity {

	private Camera mCamera;
	private int mCameraId;
	private CameraPreviewActivity mCameraPreviewActivity;

	private void fixRotation() {
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(mCameraId, info);

		int rotation = getWindowManager().getDefaultDisplay().getRotation();
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

		int result = 0;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;
		} else {
			result = (info.orientation - degrees + 360) % 360;
		}

		mCamera.setDisplayOrientation(result);
	}

	private void openCamera(int cameraId) {
		if (cameraId < 0) {
			Log.d(getString(R.string.app_name), "openCamera got negative number");
			return;
		}

		mCamera = Camera.open(cameraId);

		mCameraId = cameraId;

		fixRotation();

		mCameraPreviewActivity.setCamera(mCamera);
	}

	private void releaseCamera() {
		mCameraPreviewActivity.setCamera(null);

		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	private void switchCamera() {
		releaseCamera();

		mCameraId = (mCameraId + 1) % Camera.getNumberOfCameras();

		openCamera(mCameraId);
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_shot);

	    ActionBar actionBar = getActionBar();

	    if (actionBar != null) {
		    actionBar.hide();
	    }

	    FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	    mCameraPreviewActivity = new CameraPreviewActivity(getBaseContext(), null);
	    preview.addView(mCameraPreviewActivity);


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

	    if (Camera.getNumberOfCameras() <= 1) {
		    Button button = (Button) findViewById(R.id.switch_button);
			button.setVisibility(View.INVISIBLE);
	    }
    }

	@Override
	protected void onResume() {
		super.onResume();

		openCamera(mCameraId);
	}

	@Override
	protected void onPause() {
		super.onPause();

		releaseCamera();
	}
}
