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

	    if (Camera.getNumberOfCameras() <= 1) {
		    Button button = (Button) findViewById(R.id.switch_button);
		    button.setOnClickListener(new OnSwitchListener());
			button.setVisibility(View.INVISIBLE);
	    }
    }

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
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
