package me.strlght.campanion.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import me.strlght.campanion.app.R;
import me.strlght.campanion.app.adapter.ImageArrayAdapter;
import me.strlght.campanion.app.util.Saver;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class GalleryActivity extends Activity {

	private GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_gallery);

	    mGridView = (GridView) findViewById(R.id.pictures_view);
    }

	@Override
	protected void onResume() {
		super.onResume();

		List<String> files;
		String[] strings = Saver.getSaveDirectory().list();
		if (strings == null) {
			files = null;
		} else {
			String directory = Saver.getSaveDirectory().getAbsolutePath();
			for (int i = 0; i < strings.length; i++) {
				strings[i] = directory + File.separator + strings[i];
			}
			files = Arrays.asList(strings);
		}
		mGridView.setAdapter(new ImageArrayAdapter(getBaseContext(), files));
	}

	@Override
	protected void onPause() {
		super.onPause();

		mGridView.setAdapter(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
