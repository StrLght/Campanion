package me.strlght.campanion.app.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import me.strlght.campanion.app.R;

/**
 * Created by starlight on 10/11/14.
 */
public class EditActivity extends Activity {

	public static final String EXTRA_IMAGE = "extra_image";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_edit);

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

}
