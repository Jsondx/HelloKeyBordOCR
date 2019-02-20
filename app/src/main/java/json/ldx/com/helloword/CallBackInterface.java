package json.ldx.com.helloword;

/**
 * Created by Administrator on 2019/2/20 0020.
 */

public interface CallBackInterface {
    void onSuccess(String body);

    void onError(String error);
}
