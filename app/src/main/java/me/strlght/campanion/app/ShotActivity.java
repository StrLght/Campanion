package me.strlght.campanion.app;

import android.app.ActionBar;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by starlight on 9/22/14.
 */
public class ShotActivity extends Activity {

	private CameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_shot);

	    ActionBar actionBar = getActionBar();

	    if (actionBar != null) {
		    actionBar.hide();
	    }

	    mCameraView = (CameraView) findViewById(R.id.camera_preview);
	    final Button switch_button = (Button) findViewById(R.id.switch_button);
	    final Button shutter_button = (Button) findViewById(R.id.shutter_button);

	    mCameraView.setShutterCallback(new CameraView.ShutterCallback() {
		    @Override
		    public void onShutter() {
		        setButtonsEnabled(false);
		    }

		    @Override
		    public void postShutter() {
			    setButtonsEnabled(true);
		    }

		    private void setButtonsEnabled(boolean enabled) {
			    switch_button.setEnabled(enabled);
			    shutter_button.setEnabled(enabled);
		    }
	    });

	    switch_button.setOnClickListener(new OnSwitchListener());
	    if (Camera.getNumberOfCameras() <= 1) {
		    switch_button.setEnabled(false);
			switch_button.setVisibility(View.INVISIBLE);
	    }

	    shutter_button.setOnClickListener(new OnShutterListener());
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
}
