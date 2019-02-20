package json.ldx.com.helloword;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.ParseException;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/2/18 0018.
 */

public class ImageUtils {
    private static final int MAXBYTE = 204800;
    private static FileInputStream f;

    /**
     * 判断是否压缩
     * 上传
     *
     * @param file
     */
    public static void onCompressedUpload(Context context, File file, ImageView view) {
        try {
            f = new FileInputStream(file);
            int available = f.available();
            //
//            if (available > MAXBYTE) {
//                //进行压缩操作
//                Log.e("TAG", "压缩");
//            } else {
//                //上传服务器
//                Log.e("TAG", "上传服务器");
//                toFileBase64(context, file, view);
//            }
            toFileBase64(context, file, view);

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


    // 手写文字识别webapi接口地址
    private static final String WEBOCR_URL = "http://webapi.xfyun.cn/v1/service/v1/ocr/handwriting";
    // 测试应用ID
    private static final String TEST_APPID = "5c6cb869";
    // 测试接口密钥
    private static final String TEST_API_KEY = "7b3ed8ff3653f40e822b9029cfb5a23f";


    /**
     * 上传base 64 到汉王
     *
     * @param ivBase64
     */
    public static void onUploadImage(String ivBase64) {
        try {
            Map<String, String> header = constructHeader("en", "false");
            HttpUtil.doPost(WEBOCR_URL, header, ivBase64, new CallBackInterface() {
                @Override
                public void onSuccess(String body) {
                    Log.e("TAG", ImageUtils.class.getName() + ": " + body);
                    if (!TextUtils.isEmpty(body)) {
                        ResultBean resultBean = new Gson().fromJson(body, ResultBean.class);
                        List<ResultBean.DataBean.BlockBean> block = resultBean.getData().getBlock();
                        for (int i = 0; i < block.size(); i++) {
                            ResultBean.DataBean.BlockBean blockBean = block.get(i);
                            List<ResultBean.DataBean.BlockBean.LineBean> line = blockBean.getLine();
                            for (int i1 = 0; i1 < line.size(); i1++) {
                                ResultBean.DataBean.BlockBean.LineBean lineBean = line.get(i1);
                                List<ResultBean.DataBean.BlockBean.LineBean.WordBean> word = lineBean.getWord();
                                for (int i2 = 0; i2 < word.size(); i2++) {
                                    ResultBean.DataBean.BlockBean.LineBean.WordBean wordBean = word.get(i2);
                                    String content = wordBean.getContent();
                                    Log.e("TAG", "content: " + content);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    ToastUtils.showShort("error: " + error);
                }
            });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


    /**
     * 组装http请求头
     *
     * @return
     * @throws UnsupportedEncodingException
     * @throws ParseException
     */
    private static Map<String, String> constructHeader(String language, String location) throws UnsupportedEncodingException, ParseException {
        // 系统当前时间戳
        String X_CurTime = System.currentTimeMillis() / 1000L + "";
        // 业务参数
        String param = "{\"language\":\"" + language + "\"" + ",\"location\":\"" + location + "\"}";
        String X_Param = new String(org.apache.commons.codec.binary.Base64.encodeBase64(param.getBytes("UTF-8")));
        // 接口密钥
        String apiKey = TEST_API_KEY;
        // 讯飞开放平台应用ID
        String X_Appid = TEST_APPID;
        // 生成令牌
//        String X_CheckSum = DigestUtils.md5Hex(apiKey + X_CurTime + X_Param);
        String X_CheckSum = md5(apiKey + X_CurTime + X_Param);

        // 组装请求头
        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        header.put("X-Param", X_Param);
        header.put("X-CurTime", X_CurTime);
        header.put("X-CheckSum", X_CheckSum);
        header.put("X-Appid", X_Appid);
        return header;
    }


    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}