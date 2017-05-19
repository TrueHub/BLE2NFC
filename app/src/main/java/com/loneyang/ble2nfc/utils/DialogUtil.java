package com.loneyang.ble2nfc.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.TextView;

import com.loneyang.ble2nfc.R;

/**
 * Created by user on 2017/5/19.
 */

public class DialogUtil {
    private void showMessageOKCancel(Activity activity, String message, DialogInterface.OnClickListener okListener) {
        AlertDialog dlgShowBack = new AlertDialog.Builder(activity, R.style.MyDialogStyle)
                .setMessage(message)
                .setPositiveButton("已经接收完了", okListener)
                .setNegativeButton("暂时不删", null)
                .create();
        dlgShowBack.show();
        Button btnPositive =dlgShowBack.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button btnNegative =dlgShowBack.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

        btnNegative.setTextColor(activity.getResources().getColor(R.color.textColorPrimary));
//        btnNegative.setBackground(getResources().getDrawable(R.drawable.btn_selector_dialog));

        btnPositive.setTextColor(activity.getResources().getColor(R.color.textColorPrimary_alpha));
//        btnPositive.setBackground(getResources().getDrawable(R.drawable.btn_selector_dialog));

        TextView tvMsg = (TextView) dlgShowBack.findViewById(android.R.id.message);
        tvMsg.setTextColor(activity.getResources().getColor(R.color.textColorPrimary));
//        tvMsg.setTextAppearance(this,R.style.text_msg);
//        tvMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP , 10);
    }
}
