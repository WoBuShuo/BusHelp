package view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.hkbushelp.apps.R;
import util.DimensionUtil;

/**
 * Created by Hello on 2017/8/16.
 */

public class UpdateVersionDialog {
    private Dialog updateDialog;
    private ConfirmDialogListener mlistener;

    public void showDialog(Context context, String upDataLog,String versionId,String versionSize, ConfirmDialogListener listener) {
        dismiss();
        this.mlistener = listener;
        View dialogview = LayoutInflater.from(context).inflate(R.layout.dialog_update_version, null);
        updateDialog = new Dialog(context);
        //去掉标题栏
        updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置view
        updateDialog.setContentView(dialogview);
        updateDialog.setCanceledOnTouchOutside(false);
        //dialog默认是环绕内容的
        //通过window来设置位置、高宽
        Window window = updateDialog.getWindow();
        WindowManager.LayoutParams windowparams = window.getAttributes();
////        window.setGravity(Gravity.BOTTOM);
//        windowparams.height = ;
        windowparams.width = (int) (DimensionUtil.getWidth(context) * 0.78f);
//        设置背景透明,但是那个标题头还是在的，只是看不见了
        //注意：不设置背景，就不能全屏
//        window.setBackgroundDrawableResource(android.R.color.transparent);

        TextView btok = (TextView) dialogview.findViewById(R.id.dialog_ok);
        TextView btcancel = (TextView) dialogview.findViewById(R.id.dialog_cancel);
        TextView log = (TextView) dialogview.findViewById(R.id.up_data_log);
        TextView id = (TextView) dialogview.findViewById(R.id.version_size);
        TextView size = (TextView) dialogview.findViewById(R.id.version_id);
        log.setText(upDataLog);
        id.setText("新版本："+versionId);
        size.setText("大小："+versionSize);
//        if (showCancel) {
//            btcancel.setVisibility(View.VISIBLE);
//        } else {
//            btcancel.setVisibility(View.GONE);
//        }
        btok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mlistener != null) {
                    mlistener.ok();
                }
                dismiss();
            }
        });
        btcancel.setOnClickListener(
                new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (mlistener != null) {
                                                mlistener.cancel();
                                            }
                                            dismiss();
                                        }
                                    }
        );
        updateDialog.show();
    }

    public void dismiss() {
        if (updateDialog != null) {
            updateDialog.dismiss();
            mlistener = null;
        }
    }

    public interface ConfirmDialogListener {
        void ok();

        void cancel();

    }


}
