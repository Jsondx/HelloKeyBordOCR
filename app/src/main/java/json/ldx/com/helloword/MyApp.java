package json.ldx.com.helloword;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2019/2/18 0018.
 */

public class MyApp extends Application {

    public static Activity activitys = null;

    @Override
    public void onCreate() {
        super.onCreate();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //log相关
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
        //log打印级别，决定了log显示的详细程度
//        if (BuildConfig.DEBUG) {
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
//        } else {
//            loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.NONE);
//        }
        //log颜色级别，决定了log在控制台显示的颜色
        loggingInterceptor.setColorLevel(Level.WARNING);
        builder.addInterceptor(loggingInterceptor);


        //超时时间设置，默认60秒
        // 全局的读取超时时间
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        //全局的写入超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        //全局的连接超时时间
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);

        //建议设置OkHttpClient，不设置将使用默认的
        OkGo.getInstance().init(this)
                .setOkHttpClient(builder.build())
                //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheMode(CacheMode.FIRST_CACHE_THEN_REQUEST)
                //全局统一缓存时间，默认永不过期，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)
                .setRetryCount(3);





        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                activitys = activity;
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

    }
}
