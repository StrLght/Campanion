package me.strlght.campanion.app.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import com.aviary.android.feather.library.Constants;

/**
 * Created by starlight on 10/14/14.
 */
public class AviaryUtils {

	public static void saveUriIfChanged(Context context, Intent data) {
		Uri imageUri = data.getData();
		Bundle extra = data.getExtras();
		if (extra != null) {
			boolean changed = extra.getBoolean(Constants.EXTRA_OUT_BITMAP_CHANGED);
			if (changed) {
				Bitmap bitmap = BitmapFactory.decodeFile(imageUri.toString());
				FileUtils.save(context, bitmap);
			}
		}
	}

}
