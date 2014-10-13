package me.strlght.campanion.app;

import android.app.Application;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by starlight on 10/13/14.
 */
@ReportsCrashes(
		formKey = "",
		formUri = "https://collector.tracepot.com/e6546205"
)
public class AppDelegate extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		ACRA.init(this);
	}

}
