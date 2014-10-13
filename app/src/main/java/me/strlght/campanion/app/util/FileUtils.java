package me.strlght.campanion.app.util;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by starlight on 9/26/14.
 */
public class FileUtils {

	private static final String TAG = "Saver";

	public static File getSaveDirectory() {
		File sdDir = Environment.getExternalStorageDirectory();
		File dir = new File(sdDir.getAbsolutePath() + File.separator + "Campanion");
		dir.mkdirs();
		return dir;
	}

	public static boolean delete(File file) {
		return file.delete();
	}

	public static void save(Context context, Bitmap img) {
		if (context == null) {
			return;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
		String date = dateFormat.format(new Date());
		String name = "img_" + date + ".jpg";

		File dir = getSaveDirectory();
		String path = dir + File.separator + name;
		File pic = new File(path);
		int i = 1;
		while (pic.exists()) {
			name = "img_" + date + "_" + i + ".jpg";
			path = dir + File.separator + name;
			pic = new File(path);
			i++;
		}

		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(pic);
		} catch (IOException e) {
			Log.d(TAG, "failed to open " + path);
			return;
		}

		try {
			if (!img.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
				Log.d(TAG, "failed to write " + path);
				return;
			}
			outputStream.close();
			ContentValues values = new ContentValues();
			values.put(MediaStore.Images.Media.DATA, pic.getAbsolutePath());
			values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
			context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		} catch (IOException e) {
			Log.d(TAG, "failed to open " + path);
		}
	}

	public static class Comparator implements java.util.Comparator<File> {

		@Override
		public int compare(File file1, File file2) {
			return Long.valueOf(file2.lastModified())
					.compareTo(file1.lastModified());
		}

	}

}
