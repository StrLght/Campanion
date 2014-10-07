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
import me.strlght.campanion.app.callback.PictureCallback;
import me.strlght.campanion.app.callback.StabilizedPictureCallback;
import me.strlght.campanion.app.view.CameraView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by starlight on 9/22/14.
 */
public class ShotActivity extends Activity implements SensorEventListener {

	private static final String TAG = "ShotActivity";

	private CameraView mCameraView;
	private List<PictureCallback> mPictureCallbacks;
	private int mActiveCallbackNumber;

	private Button mSwitchButton;
	private Button mShutterButton;
	private Button mStabilityButton;

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Sensor mMagnetometer;
	private float[] mGravity;
	private float[] mGeomagnetic;
	private float mAzimuth;
	private float mPitch;
	private float mRoll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_shot);

		mCameraView = (CameraView) findViewById(R.id.camera_preview);
		mSwitchButton = (Button) findViewById(R.id.switch_button);
		mShutterButton = (Button) findViewById(R.id.shutter_button);
		mStabilityButton = (Button) findViewById(R.id.stability_button);

		mPictureCallbacks = new ArrayList<PictureCallback>();
		mPictureCallbacks.add(new StabilizedPictureCallback());
		mPictureCallbacks.add(new DefaultPictureCallback());
		mActiveCallbackNumber = 0;

		for (PictureCallback callback : mPictureCallbacks) {
			callback.setContext(getBaseContext());
		}

		mCameraView.setJpegPictureCallback(mPictureCallbacks.get(0));

		mShutterButton.setOnClickListener(new OnShutterListener());
		mStabilityButton.setOnClickListener(new OnStabilizeListener());
		mSwitchButton.setOnClickListener(new OnSwitchListener());
		if (Camera.getNumberOfCameras() <= 1) {
			mSwitchButton.setEnabled(false);
			mSwitchButton.setVisibility(View.INVISIBLE);
		}

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	@Override
	protected void onResume() {
		super.onResume();

		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
		decorView.setSystemUiVisibility(uiOptions);

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}

		mCameraView.openCamera();

		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
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
		//TODO: sensor fusion
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
				SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, newR);
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

		private void setButtonsEnabled(boolean enabled) {
			mSwitchButton.setEnabled(enabled);
			mShutterButton.setEnabled(enabled);
			mStabilityButton.setEnabled(enabled);
		}

		@Override
		public void onClick(View view) {
			int facing = mCameraView.getCameraFacing();
			for (PictureCallback callback : mPictureCallbacks) {
				callback.setPitch(mPitch);
				callback.setRoll(mRoll);
				callback.setFacing(facing);
			}

			setButtonsEnabled(false);
			mCameraView.setShutterCallback(new Camera.ShutterCallback() {

				@Override
				public void onShutter() {
					setButtonsEnabled(true);
				}

			});

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
			mActiveCallbackNumber = (mActiveCallbackNumber + 1) % mPictureCallbacks.size();
			mCameraView.setJpegPictureCallback(mPictureCallbacks.get(mActiveCallbackNumber));
		}

	}
}
