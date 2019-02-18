package json.ldx.com.helloword;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.blankj.utilcode.util.GsonUtils;
import com.bumptech.glide.Glide;
import com.hanvon.HWCloudManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Administrator on 2019/2/18 0018.
 */

public class ImageUtils {
    private static   final int MAXBYTE = 204800;
    private static  FileInputStream f;

    /**
     * 判断是否压缩
     * 上传
     *
     * @param file
     */
    public  static void onCompressedUpload(Context context, File file, ImageView view) {
        try {
            f = new FileInputStream(file);
            int available = f.available();
            Log.e("TAG", "size + " + available);
            //
            if (available > MAXBYTE) {
                //进行压缩操作
                Log.e("TAG", "压缩");
            } else {
                //上传服务器
                Log.e("TAG", "上传服务器");
                toFileBase64(context, file, view);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != f) {
                try {
                    f.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * file 转 bitmap 转 base64
     *
     * @param view 可为null
     * @param file
     */
    public static void toFileBase64(Context context, File file, ImageView view) {
        Uri uri = Uri.fromFile(file);
        //file 转bitmap
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //bitmap 颜色 灰色
        Bitmap bitmap1 = toGrayscale(bitmap);
        if (view != null) {
            Glide.with(context).load(bitmap1).into(view);
        }
        //bitmap 转64
        String base64 = bitmapToBase64(bitmap1);
        Log.e("TAG", "base64 " + base64);
        onUploadImage(base64);


    }


    /**
     * 图片去色,返回灰度图片
     *
     * @param bmp 传入的图片
     * @return 去色后的图片
     */
    public static Bitmap toGrayscale(Bitmap bmp) {
        if (bmp != null) {
            int width, height;
            Paint paint = new Paint();
            height = bmp.getHeight();
            width = bmp.getWidth();
            Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            Canvas c = new Canvas(bm);
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0);
            ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
            paint.setColorFilter(f);
            c.drawBitmap(bmp, 0, 0, paint);
            return bm;
        } else {
            return bmp;
        }
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Uri getUri(Context context, File file) {
        Uri uri = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    /**
     * 上传base 64 到汉王
     *
     * @param ivBase64
     */
    public static void onUploadImage(String ivBase64) {
        HanVanBean hanVanBean = new HanVanBean();
        hanVanBean.setUid("180.76.118.80");
        hanVanBean.setColor("gray");
        hanVanBean.setImage(ivBase64);
        String json = GsonUtils.toJson(hanVanBean);
        Log.e("TAG", "json   " + json);

    }
}
