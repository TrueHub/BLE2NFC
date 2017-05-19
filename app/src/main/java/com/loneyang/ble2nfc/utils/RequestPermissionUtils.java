package com.loneyang.ble2nfc.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dell on 2017-3-30.
 */
public class RequestPermissionUtils {
    static final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    /**
     *
     * @param activity
     * @param permissions 权限数组,如         String[] permissions = new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_EXTERNAL_STORAGE};
     * @param str 向用户解释需要权限的必要性
     */
    public static void requestPermission(final Activity activity, final String[] permissions,String str) {
        /**@param indexs 权限列表,未满足的个数以及坐标*/
        List<Integer> indexs = isPermissionComplete(activity, permissions);

        if (indexs.size() == 0) {
        } else {
            List<String> unCompletePermissionList = new ArrayList<>();
            for (int j = 0; j < indexs.size(); j++) {
                unCompletePermissionList.add(permissions[indexs.get(j)]);
            }

            final String[] unCompletePermissions = unCompletePermissionList.toArray(new String[unCompletePermissionList.size()]);

            // Should we show an explanation?  嗯 和用户低头，解释，请求。

            if (shouldShow(activity,unCompletePermissions)) {

                Log.i("MSL", "requestPermission: 解释");
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showMessageOKCancel(activity, str,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity, unCompletePermissions,
                                        REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        });

            } else {
                Log.i("MSL", "requestPermission: 不解释");
                // No explanation needed, we can request the permission.不解释，直接申请

                ActivityCompat.requestPermissions(activity,
                        unCompletePermissions,
                        REQUEST_CODE_ASK_PERMISSIONS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

        }
    }
    /**
     * @param activity
     * @param permissions 必须满足的权限
     * @return 决定了未满足权限的个数以及permissions中的具体位置
     */
    public static List<Integer> isPermissionComplete(Activity activity, String[] permissions) {
        ArrayList<Integer> indexs = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (PackageManager.PERMISSION_GRANTED !=
                    ContextCompat.checkSelfPermission(activity, permissions[i])
                    ) {
                indexs.add(i);
            }
        }
        return indexs;
    }

    private static boolean shouldShow(Activity activity, String[] unCompletePermissions) {
        for (int i = 0; i < unCompletePermissions.length; i++) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    unCompletePermissions[i]))return true;
        }
        return false;
    }

    private static void showMessageOKCancel(Activity activity, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton("好啊", okListener)
                .setNegativeButton("残忍拒绝", null)
                .create()
                .show();
    }
}
