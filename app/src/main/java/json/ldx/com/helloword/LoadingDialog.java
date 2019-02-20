package json.ldx.com.helloword;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Administrator on 2019/2/20 0020.
 */

public class LoadingDialog extends Dialog {

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.DialogActivity);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        attributes.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
        attributes.gravity = Gravity.CENTER;
        setCancelable(true);
        setCanceledOnTouchOutside(true);

    }

    private void initView() {
        View inflate = LayoutInflater.from(this.getContext()).inflate(R.layout.dia_loading, null, false);
        setContentView(inflate);

    }

}
