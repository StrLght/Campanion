package me.strlght.campanion.app.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.*;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import me.strlght.campanion.app.R;
import me.strlght.campanion.app.callback.DefaultPictureCallback;
import me.strlght.campanion.app.callback.StabilizedPictureCallback;
import me.strlght.campanion.app.view.CameraView;

/**
 * Created by starlight on 9/22/14.
 */
public class ShotActivity extends Activity implements SensorEventListener {

    private static final String TAG = "ShotActivity";

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
	private float mAzimuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_shot);

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

		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}

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
		//TODO: add interpolation or something to smoother results
		//TODO: gyroscope support
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
				float[] newR = new float[9];
				SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Y, newR);
				SensorManager.getOrientation(newR, orientation);

				float azimuth = (float) Math.toDegrees(orientation[0]);
				float pitch = (float) Math.toDegrees(orientation[1]);
				float roll = (float) Math.toDegrees(orientation[2]);

				mAzimuth = azimuth;
				mPitch = pitch;
				mRoll = roll;
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}

	private class OnShutterListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO: this is really horrible.
			int facing = mCameraView.getCameraFacing();
			mStabilizedPictureCallback.setPitch(mPitch);
			mStabilizedPictureCallback.setRoll(mRoll);
			mStabilizedPictureCallback.setFacing(facing);
			mDefaultPictureCallback.setPitch(mPitch);
			mDefaultPictureCallback.setRoll(mRoll);
			mDefaultPictureCallback.setFacing(facing);
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
