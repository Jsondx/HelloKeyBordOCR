package json.ldx.com.helloword;

import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;


import java.io.BufferedReader;
import java.io.InputStreamReader;


import java.net.URL;
import java.net.URLConnection;
import java.util.Map;


/**
 * Http请求工具类
 */
public class HttpUtil {

    private static AlertDialog alertDialog;
    private static AlertDialog.Builder builder;

    private HttpUtil() {

    }

    static {
        if (builder==null){
            builder = new AlertDialog.Builder(MyApp.activitys);
//            builder.setMessage("正在识别请稍后...");
            View view = LayoutInflater.from(MyApp.activitys).inflate(R.layout.dia_loading, null, false);
            builder.setView(view);
            alertDialog = builder.create();
        }

    }

    /**
     * 发送post请求
     *
     * @param url
     * @param header
     * @param body
     * @param callBackInterface
     * @return
     */
    public static void doPost(String url, Map<String, String> header, String body, final CallBackInterface callBackInterface) {
        HttpHeaders httpHeaders = new HttpHeaders();
        for (String key : header.keySet()) {
            httpHeaders.put(key, header.get(key));
        }
        OkGo.<String>post(url)
                .headers(httpHeaders)
                .params("image", body)
                .execute(new StringCallback() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        Log.e("TAG", "start");
                        if (alertDialog != null) {
                            alertDialog.show();
                        }
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        Log.e("TAG", "finish");
                        if (alertDialog != null) {
                            alertDialog.dismiss();
                        }
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        String body = response.body();
                        JSONObject jsonObject = JSON.parseObject(body);
                        String code = jsonObject.getString("code");
                        Log.e("TAG", "code+  " + code);
                        if (code.equals("0")) {
                            callBackInterface.onSuccess(response.body());
                        } else {
                            String desc = jsonObject.getString("desc");
                            callBackInterface.onError(desc);
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        callBackInterface.onError(response.message());
                    }
                });

    }

    /**
     * 发送get请求
     *
     * @param url
     * @param header
     * @return
     */
    public static String doGet(String url, Map<String, String> header) {
        String result = "";
        BufferedReader in = null;
        try {
            // 设置 url
            URL realUrl = new URL(url);
            URLConnection connection = realUrl.openConnection();
            // 设置 header
            for (String key : header.keySet()) {
                connection.setRequestProperty(key, header.get(key));
            }
            // 设置请求 body
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            return null;
        }
        return result;
    }
}
