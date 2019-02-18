package json.ldx.com.helloword;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.hanvon.HWCloudManager;
import com.hanvon.utils.ConnectionDetector;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.tianshaokai.mathkeyboard.KeyboardFragment;
import com.tianshaokai.mathkeyboard.utils.LatexUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private File cameraSavePath;//拍照照片路径
    private Uri uri;//照片uri
    private String photoPath;
    private ImageView ivPhoto;

    private File savePath = null;
    private HWCloudManager hwCloudManager;
    private String result;
    private DiscernHandler discernHandler;
    private ProgressDialog pd;
    private TextView tvContent;
    private String[] storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //强制为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        hwCloudManager = new HWCloudManager(this, "7e5a87ca-e0f5-4f32-9a5e-a2acd23a5f60");
        discernHandler = new DiscernHandler();

        ivPhoto = findViewById(R.id.iv_photo);

        tvContent = (TextView) findViewById(R.id.tvTitle);
        Button btnShow = (Button) findViewById(R.id.btnShow);
        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyboard(tvContent);
            }
        });
        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            int i = ContextCompat.checkSelfPermission(this, storagePermission[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (i != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, storagePermission, 100);
            } else {
                LatexUtil.init(this);
            }
        } else {
            LatexUtil.init(this);
        }
    }


    private void showKeyboard(TextView textView) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(KeyboardFragment.TAG);
        if (fragment != null) {
            //为了不重复显示dialog，在显示对话框之前移除正在显示的对话框
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
        final KeyboardFragment keyboardFragment = new KeyboardFragment();
        keyboardFragment.setOutSide(textView);
        fragmentManager.beginTransaction().add(keyboardFragment, KeyboardFragment.TAG).commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                    boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                    if (!b) {
                        // 用户还是想用我的 APP 的
                        // 提示用户去应用设置界面手动开启权限
                        Toast.makeText(this, "需要开启权限", Toast.LENGTH_SHORT).show();
                    } else {
                        finish();
                    }
                } else {
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();

                    LatexUtil.init(this);
                }
            }
        }
    }

    /**
     * 拍照
     *
     * @param view
     */
    public void onCamera(View view) {
        if (isCamera()) {
            goCamera();
        } else {
            onApplyCamera();
        }

    }

    /**
     * 判断权限是否开启
     *
     * @return
     */
    private boolean isCamera() {
        return XXPermissions.isHasPermission(this, Manifest.permission.CAMERA);
    }

    /**
     * 申请相机权限
     */
    private void onApplyCamera() {
        XXPermissions.with(this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.CAMERA) //不指定权限则自动获取清单中的危险权限
                .request(new OnPermission() {

                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            ToastUtils.showShort("获取权限成功");
                            //获取成功直接调用
                            goCamera();
                        } else {
                            ToastUtils.showShort("获取权限成功，部分权限未正常授予");
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if (quick) {
                            ToastUtils.showShort("被永久拒绝授权，请手动授予权限");
                            //如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(MainActivity.this);
                        } else {
                            ToastUtils.showShort("获取权限失败");
                        }
                    }
                });
    }

    //激活相机操作
    private void goCamera() {
        cameraSavePath = new File(Environment.getExternalStorageDirectory().getPath() + "/" + System.currentTimeMillis() + ".jpg");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //第二个参数为 包名.fileprovider
            uri = FileProvider.getUriForFile(MainActivity.this, "json.ldx.com.helloword.fileprovider", cameraSavePath);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(cameraSavePath);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        MainActivity.this.startActivityForResult(intent, 1);
    }

    /**
     * 裁剪
     *
     * @param data
     */
    private void startPhotoZoom(File data) {
        Uri uri = ImageUtils.getUri(this, data);
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setDataAndType(uri, "image/*");
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setDataAndType(uri, "image/*");
        }

        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        savePath = new File(Environment.getExternalStorageDirectory().getPath() + "/" + System.currentTimeMillis() + ".jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUtils.getUri(this, savePath));
        startActivityForResult(intent, 2);

    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        doNext(requestCode, grantResults);
//    }
//
//    private void doNext(int requestCode, int[] grantResults) {
//        if (requestCode == 200) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                //同意权限
//                hwCloudManager = new HWCloudManager(this, "7e5a87ca-e0f5-4f32-9a5e-a2acd23a5f60");
//                discernHandler = new DiscernHandler();
//
//            } else {
//                //denyPermission();
//            }
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1://拍照 并调用裁剪
                    startPhotoZoom(cameraSavePath);
                    break;
                case 2://裁剪返回的结果
                    Glide.with(MainActivity.this).load(savePath).into(ivPhoto);
//                    ImageUtils.onCompressedUpload(this,savePath,ivPhoto);
                    String absolutePath = savePath.getAbsolutePath();
                    ConnectionDetector connectionDetector = new ConnectionDetector(getApplication());
                    if (connectionDetector.isConnectingTOInternet()) {
                        if (null != absolutePath) {
                            pd = ProgressDialog.show(MainActivity.this, "", "正在识别请稍后......");
                            DiscernThread discernThread = new DiscernThread();
                            new Thread(discernThread).start();
                        } else {
                            ToastUtils.showShort("请选择图片后再试");
                        }
                    } else {
                        ToastUtils.showShort("网络连接失败，请检查网络后重试！");
                    }
                    break;
            }
        }
    }

    public class DiscernThread implements Runnable {

        @Override
        public void run() {
            try {
                /**
                 * 调用汉王云题目识别方法
                 */
                result = hwCloudManager.formulaOCRLanguage(savePath.getAbsolutePath());
//				result = hwCloudManagerFormula.formulaOCRLanguage4Https(picPath);
                //System.out.println(result);
            } catch (Exception e) {
                // TODO: handle exception
            }
            Bundle mBundle = new Bundle();
            mBundle.putString("response", result);
            Message msg = new Message();
            msg.setData(mBundle);
            discernHandler.sendMessage(msg);
        }
    }

    public class DiscernHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            pd.dismiss();
            Bundle bundle = msg.getData();
            String response = bundle.getString("response");
            if (response != null && !response.equals("")) {
                Log.e("TAG", response);
//                testView.setText(response);
            } else {
                ToastUtils.showShort("请重试！");
            }
        }
    }

}
