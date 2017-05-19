package com.loneyang.ble2nfc.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loneyang.ble2nfc.R;

/**
 * Created by user on 2017/5/19.
 */

public class ToastUtil {
    private Context context;
    private Toast toast;

    public ToastUtil(Context context) {
        this.context = context.getApplicationContext();
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    public boolean showToast(String str) {
        if (toast.getView().getParent() != null) {
            if (toast.getView().getContentDescription().equals(str)) {
                return false;
            } else {
                toast.cancel();
                toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
            toast.show();
        }
        return true;
    }

    public boolean showToastWithImg(String str, Drawable imgRes) {
        if (toast.getView().getParent() != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);

        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16);
        textView.setText(str);
        textView.setPadding(80, 0, 80, 0);
        imgRes.setBounds(0, 0, imgRes.getMinimumWidth(), imgRes.getMinimumHeight());
        textView.setCompoundDrawables(imgRes, null, null, null);
        textView.setBackground(context.getResources().getDrawable(R.drawable.shape_round_toast));

        toast.setView(textView);

        toast.show();


        return false;
    }
}
