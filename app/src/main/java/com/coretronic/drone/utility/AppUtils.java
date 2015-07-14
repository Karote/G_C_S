package com.coretronic.drone.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.Locale;

import static android.graphics.BitmapFactory.decodeFile;

/**
 * Created by james on 15/6/5.
 */
public class AppUtils {

    private static String TAG = AppUtils.class.getSimpleName();

    public static CustomerTwoBtnAlertDialog getAlertDialog(final Context mContext, String msg, String okStr, String cancelStr, View.OnClickListener listener) {
        CustomerTwoBtnAlertDialog dialog = new CustomerTwoBtnAlertDialog(mContext);
        dialog.setMsg(msg)
                .setPositiveBtnText(okStr)
                .setNegativeBtnText(cancelStr)
                .setPositiveListener(listener);
        return dialog;
    }

    public static Bitmap rotateAndResizeBitmap(String filePath, int reqWidth, int reqHeight) {
        Log.i(TAG, "filePath:" + filePath);
        Log.i(TAG, "reqWidth:" + reqWidth + "/" + "reqHeight:" + reqHeight);

        // file Exif
        ExifInterface exifInterface = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        decodeFile(filePath, options);
        try {
            exifInterface = new ExifInterface(filePath);
            Log.i(TAG, "exifInterface = nwe ExifInterface()");
            Log.i(TAG, "options:" + options);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "e:" + e.getMessage());
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return null;
        }
        // orientation
        int tag = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        int orientation = 0;
        if (tag == ExifInterface.ORIENTATION_ROTATE_90) {
            orientation = 90;
        } else if (tag == ExifInterface.ORIENTATION_ROTATE_180) {
            orientation = 180;
        } else if (tag == ExifInterface.ORIENTATION_ROTATE_270) {
            orientation = 270;
        }

        try {
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            Log.d(TAG, "insamplesize:" + options.inSampleSize);
//        options.inSampleSize = 4;
            options.inJustDecodeBounds = false;
            Matrix mtx = new Matrix();
            Bitmap bitmap = decodeFile(filePath, options);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
//            Log.d(TAG, "--- bitmap:" + bitmap);
//            Log.d(TAG, "--- bitmap width/height:" + width + "/" + height);

            float scaleX = reqWidth / (float) width;
            float scaleY = reqHeight / (float) height;
            float baseScale = Math.min(scaleX, scaleY);
//            Log.d(TAG, "--- scaleX/scaleY:" + scaleX + "/" + scaleY);
//            Log.d(TAG, "--- baseScale:" + baseScale);

            mtx.setScale(baseScale, baseScale);
            mtx.postRotate(orientation);
            return Bitmap.createBitmap(bitmap, 0, 0, width, height, mtx, true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int width = options.outWidth;
        final int height = options.outHeight;
//        Log.d(TAG, "--- width/height:" + width + "/" + height);

        int inSampleSize = 1;

//        Log.i(TAG, "calculateInSampleSize.reqWidth:" + reqWidth);
//        Log.i(TAG, "calculateInSampleSize.reqHeight:" + reqHeight);
//
//        Log.i(TAG, "width:" + width);
//        Log.i(TAG, "height:" + height);


        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            Log.i(TAG, "halfWidth width:" + halfWidth);
            Log.i(TAG, "halfHeight height:" + halfHeight);

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static String readableFileSize(String sizeString) {
        int size = Integer.valueOf(sizeString.split(" bytes")[0]);
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String stringForTime(int time) {
        Formatter formatter = new Formatter(new StringBuilder(), Locale.getDefault());
        int seconds = time % 60;
        int minutes = (time / 60) % 60;
        int hours = time / 3600;

        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
}
