package me.strlght.campanion.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by starlight on 9/22/14.
 */
public class ShotActivity extends Activity {

	private CameraView mCameraView;
	private StabilizedPictureCallback mStabilizedPictureCallback;
	private DefaultPictureCallback mDefaultPictureCallback;
	private boolean isStabilized;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_shot);

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

		mCameraView = (CameraView) findViewById(R.id.camera_preview);
		final Button switch_button = (Button) findViewById(R.id.switch_button);
		final Button shutter_button = (Button) findViewById(R.id.shutter_button);
		final Button stability_button = (Button) findViewById(R.id.stability_button);


		mCameraView.setShutterCallback(new CameraView.ShutterCallback() {

			@Override
			public void preShutter() {
				setButtonsEnabled(false);
			}

			@Override
			public void postShutter() {
				setButtonsEnabled(true);
			}

			private void setButtonsEnabled(boolean enabled) {
				switch_button.setEnabled(enabled);
				shutter_button.setEnabled(enabled);
				stability_button.setEnabled(enabled);
			}

		});

		mStabilizedPictureCallback = new StabilizedPictureCallback();
		mStabilizedPictureCallback.setContext(getBaseContext());
		mDefaultPictureCallback = new DefaultPictureCallback();
		mDefaultPictureCallback.setContext(getBaseContext());
		mCameraView.setCameraJpegPictureCallback(mStabilizedPictureCallback);
		isStabilized = true;

		switch_button.setOnClickListener(new OnSwitchListener());
		if (Camera.getNumberOfCameras() <= 1) {
			switch_button.setEnabled(false);
			switch_button.setVisibility(View.INVISIBLE);
		}

		shutter_button.setOnClickListener(new OnShutterListener());

		stability_button.setOnClickListener(new OnStabilizeListener());
	}

	@Override
	protected void onResume() {
		super.onResume();

		mCameraView.openCamera();
	}

	@Override
	protected void onPause() {
		super.onPause();

		mCameraView.releaseCamera();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private class OnShutterListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			mCameraView.takePicture();
		}

	}

	private class OnSwitchListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			mCameraView.switchCamera();
		}

	}

	private class OnStabilizeListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			mCameraView.
					setCameraJpegPictureCallback(isStabilized ? mDefaultPictureCallback : mStabilizedPictureCallback);
			isStabilized = !isStabilized;
		}

	}
}
