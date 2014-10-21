package me.strlght.campanion.app.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.*;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import me.strlght.campanion.app.R;
import me.strlght.campanion.app.callback.DefaultPictureCallback;
import me.strlght.campanion.app.callback.PictureCallback;
import me.strlght.campanion.app.callback.StabilizedPictureCallback;
import me.strlght.campanion.app.receiver.CameraButtonReceiver;
import me.strlght.campanion.app.view.CameraView;
import me.strlght.campanion.app.view.RegionCameraView;

/**
 * Created by starlight on 9/22/14.
 */
@SuppressWarnings("UnusedDeclaration")
public class ShotActivity extends Activity implements SensorEventListener {

	private static final String TAG = "ShotActivity";

	private CameraView mCameraView;
	private RegionCameraView mRegionCameraView;
	private PictureCallback[] mPictureCallbacks = {new StabilizedPictureCallback(), new DefaultPictureCallback()};
	private int[] mPictureSwitchImages = {R.drawable.ic_action_picture, R.drawable.ic_action_crop};
	private boolean[] mRegionVisibility = {true, false};
	private int mActiveCallbackNumber;

	private ImageButton mSwitchButton;
	private ImageButton mShutterButton;
	private ImageButton mStabilityButton;

	private SensorManager mSensorManager;
	private Sensor mGravitySensor;
	private Sensor mAccelerometer;
	private Sensor mMagnetometer;
	private boolean mIsUsingGravity;
	private float[] mGravity;
	private float[] mGeomagnetic;
	private float mRoll;

	private CameraButtonReceiver mButtonReceiver = new CameraButtonReceiver(
			new CameraButtonReceiver.CameraButtonCallback() {

				@Override
				public void onReceive() {
					takePhoto();
				}

			});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_shot);

		mCameraView = (CameraView) findViewById(R.id.camera_preview);
		mRegionCameraView = (RegionCameraView) findViewById(R.id.region_view);
		mSwitchButton = (ImageButton) findViewById(R.id.switch_button);
		mShutterButton = (ImageButton) findViewById(R.id.shutter_button);
		mStabilityButton = (ImageButton) findViewById(R.id.stability_button);

		mActiveCallbackNumber = 0;

		for (PictureCallback callback : mPictureCallbacks) {
			callback.setContext(getBaseContext());
		}

		mCameraView.setJpegPictureCallback(mPictureCallbacks[mActiveCallbackNumber]);
		mCameraView.setOnPreviewSizeChangeListener(new CameraView.OnPreviewSizeChangeListener() {

			@Override
			public void onPreviewSizeChanged(Camera.Size previewSize, Camera.Size actualSize) {
				mRegionCameraView.setSize(previewSize, actualSize);
			}

		});
		mStabilityButton.setImageResource(mPictureSwitchImages[mActiveCallbackNumber]);

		mShutterButton.setOnClickListener(new OnShutterListener());
		mStabilityButton.setOnClickListener(new OnStabilizeListener());
		mSwitchButton.setOnClickListener(new OnSwitchListener());
		if (Camera.getNumberOfCameras() <= 1) {
			mSwitchButton.setEnabled(false);
			mSwitchButton.setVisibility(View.GONE);
		}

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = 1.0f;
		getWindow().setAttributes(lp);
	}

	@Override
	protected void onResume() {
		super.onResume();

		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
		decorView.setSystemUiVisibility(uiOptions);

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}

		mCameraView.openCamera();

		mIsUsingGravity = false;
		mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_FASTEST);

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_CAMERA_BUTTON);
		filter.addAction(Intent.ACTION_MEDIA_BUTTON);
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
		registerReceiver(mButtonReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(mButtonReceiver);
		mSensorManager.unregisterListener(this);
		mCameraView.releaseCamera();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		//TODO: sensor fusion
		if (sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) {
			mGravity = sensorEvent.values.clone();
			mIsUsingGravity = true;
		}

		if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !mIsUsingGravity) {
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

				mRoll = (float) Math.toDegrees(orientation[2]);

				RelativeLayout.LayoutParams switchParams =
						(RelativeLayout.LayoutParams) mSwitchButton.getLayoutParams();
				RelativeLayout.LayoutParams shutterParams =
						(RelativeLayout.LayoutParams) mShutterButton.getLayoutParams();
				RelativeLayout.LayoutParams stabilityParams =
						(RelativeLayout.LayoutParams) mStabilityButton.getLayoutParams();
				int rule;
				if (shutterParams.getRules()[RelativeLayout.ALIGN_PARENT_BOTTOM] != 0) {
					rule = RelativeLayout.ALIGN_PARENT_BOTTOM;
				} else {
					rule = RelativeLayout.ALIGN_PARENT_TOP;
				}
				if (mRoll > -45 && mRoll < 45) {
					rule = RelativeLayout.ALIGN_PARENT_BOTTOM;
				} else if (mRoll < -135 || mRoll > 135) {
					rule = RelativeLayout.ALIGN_PARENT_TOP;
				}
				switchParams = clearParamsAndAddRule(switchParams, rule);
				shutterParams = clearParamsAndAddRule(shutterParams, rule);
				stabilityParams = clearParamsAndAddRule(stabilityParams, rule);
				mSwitchButton.setLayoutParams(switchParams);
				mShutterButton.setLayoutParams(shutterParams);
				mStabilityButton.setLayoutParams(stabilityParams);

				float tempRoll = -mRoll;
				mRegionCameraView.setRotation(tempRoll);
				mSwitchButton.setRotation(tempRoll);
				mStabilityButton.setRotation(tempRoll);
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}

	private RelativeLayout.LayoutParams clearParamsAndAddRule(RelativeLayout.LayoutParams params, int rule) {
		params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(rule);
		return params;
	}

	private void takePhoto() {
		int facing = mCameraView.getCameraFacing();
		for (PictureCallback callback : mPictureCallbacks) {
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

	private void setButtonsEnabled(boolean enabled) {
		mSwitchButton.setEnabled(enabled);
		mShutterButton.setEnabled(enabled);
		mStabilityButton.setEnabled(enabled);
	}

	private class OnShutterListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			takePhoto();
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
			mActiveCallbackNumber = (mActiveCallbackNumber + 1) % mPictureCallbacks.length;
			mCameraView.setJpegPictureCallback(mPictureCallbacks[mActiveCallbackNumber]);
			mStabilityButton.setImageResource(mPictureSwitchImages[mActiveCallbackNumber]);
			mStabilityButton.setBackground(getResources().getDrawable(R.drawable.selector_camera_button));
			int visibility = View.INVISIBLE;
			if (mRegionVisibility[mActiveCallbackNumber]) {
				visibility = View.VISIBLE;
			}
			mRegionCameraView.setVisibility(visibility);
		}

	}
}
