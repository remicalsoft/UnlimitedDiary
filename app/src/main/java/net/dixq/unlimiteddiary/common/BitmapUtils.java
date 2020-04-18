package net.dixq.unlimiteddiary.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtils {

    public static Bitmap rotateImageIfRequired(Bitmap bitmap, Context context, Uri uri) throws IOException {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
            return bitmap;
        }

        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        ExifInterface ei = new ExifInterface(fileDescriptor);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        parcelFileDescriptor.close();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(bitmap, 270);
            default:
                return bitmap;
        }
    }

    private static Bitmap rotateImage(Bitmap bitmap, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return rotatedImg;
    }

    public static Bitmap resize(Bitmap bmp, int within){
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        if(w<=within && h<=within){
            return bmp;
        }
        float scale = 0;
        if(w>h){
            scale = within / (float)w;
        } else {
            scale = within / (float)h;
        }
        return Bitmap.createScaledBitmap(
                bmp,
                (int)(w * scale),
                (int)(h * scale),
                true
        );
    }

    public static Bitmap cropSquareFrom(String path, int size){
        try {
            InputStream istream = new FileInputStream(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(istream, null, options);
            int w = options.outWidth;
            int h = options.outHeight;

            istream = new FileInputStream(path);
            BitmapRegionDecoder decoder = null;
                decoder = BitmapRegionDecoder.newInstance(istream, true);
            Rect rect;
            if(w>h){
                rect = new Rect(w/2-h/2, 0, w/2+h/2, h);
            } else {
                rect = new Rect(0, h/2-w/2, w, h/2+w/2);
            }
            options.inJustDecodeBounds = false;
            Bitmap bitmap = decoder.decodeRegion(rect, options);
            return resize(bitmap, size);
        } catch (IOException e) {
            return null;
        }
    }

    public static byte[] compress(Bitmap bmp, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }

}
