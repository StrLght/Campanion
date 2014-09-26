package me.strlght.campanion.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.*;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Created by starlight on 9/22/14.
 */
public class ShotActivity extends Activity implements SensorEventListener {

	private CameraView mCameraView;
	private StabilizedPictureCallback mStabilizedPictureCallback;
	private DefaultPictureCallback mDefaultPictureCallback;
	private boolean isStabilized;

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Sensor mMagnetometer;
	private float[] mGravity;
	private float[] mGeomagnetic;
	private float mPitch;
	private float mRoll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_shot);

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

		shutter_button.setOnClickListener(new OnShutterListener());
		stability_button.setOnClickListener(new OnStabilizeListener());
		switch_button.setOnClickListener(new OnSwitchListener());
		if (Camera.getNumberOfCameras() <= 1) {
			switch_button.setEnabled(false);
			switch_button.setVisibility(View.INVISIBLE);
		}

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mCameraView.openCamera();

		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	protected void onPause() {
		super.onPause();

		mCameraView.releaseCamera();

		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			mGravity = sensorEvent.values.clone();
		}

		if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			mGeomagnetic = sensorEvent.values.clone();
		}

		if (mGravity != null && mGeomagnetic != null) {
			float[] R = new float[9];
			float[] I = new float[9];
			if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {
				float[] orientation = new float[3];
				SensorManager.getOrientation(R, orientation);

				float pitch = Double
						.valueOf(Math.toDegrees(orientation[1]))
						.floatValue();
				float roll = Double
						.valueOf(Math.toDegrees(orientation[2]))
						.floatValue();

				mPitch = pitch;
				mRoll = roll;

				// TODO: do something with orientation data.
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}

	private class OnShutterListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			mStabilizedPictureCallback.setPitch(mPitch);
			mStabilizedPictureCallback.setRoll(mRoll);
			mDefaultPictureCallback.setPitch(mPitch);
			mDefaultPictureCallback.setRoll(mRoll);
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
