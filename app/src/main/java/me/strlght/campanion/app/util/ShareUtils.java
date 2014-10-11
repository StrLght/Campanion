package me.strlght.campanion.app.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by starlight on 10/11/14.
 */
public class ShareUtils {

	public static void shareImage(Activity activity, File image) {
		String message = "Share image";
		Intent sharingIntent = createIntent(Intent.ACTION_SEND);
		sharingIntent.putExtra(Intent.EXTRA_STREAM,
				Uri.parse("file://" + image.getAbsolutePath()));
		activity.startActivity(Intent.createChooser(sharingIntent, message));
	}

	public static void shareImages(Activity activity, List<File> images) {
		String message = "Share images";
		Intent sharingIntent = createIntent(Intent.ACTION_SEND_MULTIPLE);
		ArrayList<Uri> uris = new ArrayList<Uri>();
		for (File image : images) {
			uris.add(Uri.parse("file://" + image.getAbsolutePath()));
		}
		sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		activity.startActivity(Intent.createChooser(sharingIntent, message));
	}

	private static Intent createIntent(String type) {
		Intent sharingIntent = new Intent(type);
		sharingIntent.setType("image/*");
		return sharingIntent;
	}

}
