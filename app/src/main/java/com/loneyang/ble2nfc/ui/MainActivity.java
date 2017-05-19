package com.loneyang.ble2nfc.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loneyang.ble2nfc.R;
import com.loneyang.ble2nfc.eventbeans.Comm2GATT;
import com.loneyang.ble2nfc.eventbeans.EventNotification;
import com.loneyang.ble2nfc.service.GATTService;
import com.loneyang.ble2nfc.utils.DialogUtil;
import com.loneyang.ble2nfc.utils.EventUtil;
import com.loneyang.ble2nfc.utils.RequestPermissionUtils;
import com.loneyang.ble2nfc.utils.ScreenUtil;
import com.loneyang.ble2nfc.utils.ServiceUtils;
import com.loneyang.ble2nfc.utils.ToastUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;

import static android.R.attr.dial;
import static android.R.id.message;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.loneyang.ble2nfc.eventbeans.Comm2GATT.EventType.CONN;
import static com.loneyang.ble2nfc.eventbeans.Comm2GATT.EventType.DISCONN;
import static com.loneyang.ble2nfc.eventbeans.Comm2GATT.EventType.READ_INFO_ALL;
import static com.loneyang.ble2nfc.eventbeans.Comm2GATT.EventType.READ_BLOCK_INFO_SINGLE;
import static com.loneyang.ble2nfc.eventbeans.Comm2GATT.EventType.READ_SYS_INFO;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private AppBarLayout appbar;
    private FrameLayout frame_content;
    private CoordinatorLayout main_content;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    /**
     * 连接按钮
     */
    private MenuItem menu_conn;
    private MenuItem menu_sys_info;
    private MenuItem menu_block_info_single;
    private MenuItem menu_block_info_all;
    private Intent gattService;
    private TextView nvg_header_deviceId;
    private ToastUtil toastUtil;
    private TextInputEditText tiEt;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestMyPermissions();

        initView();

        setActionBar();

        initNavigationMenuItem();

        setNavigationItemSelectedListener();

        toastUtil = new ToastUtil(getApplicationContext());

        toast = Toast.makeText(this, "再次点击退出程序", Toast.LENGTH_SHORT);

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventUtil.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventUtil.unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(gattService);
    }

    @Override
    public void onBackPressed() {
        if (toast.getView().getParent() == null)
            toast.show();
        else {
            toast.cancel();
            super.onBackPressed();
        }
    }

    private void requestMyPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            RequestPermissionUtils.requestPermission(this, permissions, "BLE设备连接蓝牙还需要获取以下权限");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getDataOver(EventNotification eventNotification) {
        switch (eventNotification.getType()) {
            case GATTService.DEVICE_ID:
                if (eventNotification.isGetOver()) {

                    new ToastUtil(MainActivity.this).showToast("连接 " + eventNotification.getType() + " 成功");
                    //侧滑菜单的header中
                    RelativeLayout relativeLayout = (RelativeLayout) navigationView.getHeaderView(0);
                    nvg_header_deviceId = (TextView) relativeLayout.findViewById(R.id.header_connect_status);
                    nvg_header_deviceId.setText(eventNotification.getType());
                    menu_conn.setTitle("断开");

                    //连接上之后 立马获取系统信息
                    EventUtil.post(new Comm2GATT(DISCONN, 0));

                } else {

                }
        }
    }

    private void setActionBar() {
        if (toolbar == null) return;
        showToolbar(true);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        Log.d("MSL", "setActionBar: 1");
        if (actionBar == null) return;
        Log.d("MSL", "setActionBar: 2");

        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    /**
     * 显示Toolbar
     *
     * @param show true:显示,false:隐藏
     */
    public void showToolbar(boolean show) {
        if (toolbar == null) {
            Log.e("MSL", "Toolbar is null.");
        } else {
            int paddingTop = toolbar.getPaddingTop();
            int paddingBottom = toolbar.getPaddingBottom();
            int paddingLeft = toolbar.getPaddingLeft();
            int paddingRight = toolbar.getPaddingRight();
            int statusHeight = ScreenUtil.getStatusHeight(this);
            ViewGroup.LayoutParams params = toolbar.getLayoutParams();
            int height = params.height;
            /**
             * 利用状态栏的高度，4.4及以上版本给Toolbar设置一个paddingTop值为status_bar的高度，
             * Toolbar延伸到status_bar顶部
             **/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                setTranslucentStatus(show);
                if (show) {
                    paddingTop += statusHeight;
                    height += statusHeight;
                } else {
                    paddingTop -= statusHeight;
                    height -= statusHeight;
                }
            }
            params.height = height;
            toolbar.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            toolbar.setVisibility(show ? VISIBLE : GONE);
        }
    }

    /**
     * 设置透明状态栏
     * 对4.4及以上版本有效
     *
     * @param on
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        appbar = (AppBarLayout) findViewById(R.id.appbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        frame_content = (FrameLayout) findViewById(R.id.frame_content);
        main_content = (CoordinatorLayout) findViewById(R.id.main_content);
        navigationView = (NavigationView) findViewById(R.id.navigationView);

        navigationView.setItemIconTintList(null);
        //取消竖直方向的scrollbar
        if (navigationView != null) {
            NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
            if (navigationMenuView != null) {
                navigationMenuView.setVerticalScrollBarEnabled(false);
            }
        }
    }

    private void initNavigationMenuItem() {
        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            MenuItem menuItem = navigationView.getMenu().getItem(i);

            switch (menuItem.getItemId()) {
                case R.id.nvg_menu_conn:
                    menu_conn = menuItem;
                    break;
                case R.id.nvg_menu_readInfo:

                    for (int j = 0; j < menuItem.getSubMenu().size(); j++) {
                        MenuItem subMenuItem = menuItem.getSubMenu().getItem(i);
                        switch (subMenuItem.getItemId()) {
                            case R.id.nvg_sys_info:
                                menu_sys_info = subMenuItem;
                                break;
                            case R.id.nvg_block_info_single:
                                menu_block_info_single = subMenuItem;
                                break;
                            case R.id.nvg_read_info_all:
                                menu_block_info_all = subMenuItem;
                                break;
                        }
                    }

                    break;
            }

        }
    }

    private void setNavigationItemSelectedListener() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nvg_menu_conn:
                        String title = (String) (item.getTitle());
                        Log.d("MSL", "onNavigationItemSelected: conn");
                        requestMyPermissions();

                        if (title.equals("连接")) {
                            if (ServiceUtils.isServiceWork(MainActivity.this, "com.youyi.weigan.service.GATTService")) {
                                EventUtil.post(new Comm2GATT(CONN, 0));
                                Log.e("MSL", "onClick: gatt service is running");
                            } else {
                                gattService = new Intent(MainActivity.this, GATTService.class);
                                startService(gattService);
                                Log.e("MSL", "onClick: gatt is not running");
                            }
//                            startWriteService();

                        } else if (title.equals("断开")) {
                            EventUtil.post(new Comm2GATT(DISCONN, 0));
                        }
                        break;
                    case R.id.nvg_sys_info:

                        toastUtil.showToastWithImg("读取系统信息",
                                getResources().getDrawable(R.drawable.ic_device_info_toast));
                        EventUtil.post(new Comm2GATT(READ_SYS_INFO, 0));
                        EventUtil.post("READ_SYS_INFO");
                        break;
                    case R.id.nvg_block_info_single:

                        //选择0 ~ 42作为Comm2GATT的构造器的参数2
                        showDialogForEdit(MainActivity.this);

                        break;
                    case R.id.nvg_read_info_all:

                        toastUtil.showToastWithImg("获取所有block信息",
                                getResources().getDrawable(R.drawable.ic_all_info_toast));
                        EventUtil.post(new Comm2GATT(READ_INFO_ALL, 0));
                        EventUtil.post("READ_INFO_ALL");

                        break;
                }

                return false;
            }
        });
    }

    /**
     * 初始化一个带输入框的alertDialog
     * @param activity
     */
    private void showDialogForEdit(Activity activity) {

        final TextInputLayout til = (TextInputLayout) getLayoutInflater().inflate(
                R.layout.textinput_ll, (ViewGroup) findViewById(R.id.til_ll));
        til.setFocusable(true);
        til.setHint("输入需要查询的模块编号(0 - 43)");
        tiEt = (TextInputEditText) til.findViewById(R.id.textInputEditText);

        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle("读取模块信息")
                .setMessage(" ")
                .setIcon(R.drawable.ic_block_info_24dp)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (tiEt == null) return;
                        if (TextUtils.isEmpty(tiEt.getText())) return;

                        int blockNum = Integer.valueOf(tiEt.getText().toString());
                        toastUtil.showToastWithImg("读取第" + blockNum + "单片block信息",
                                getResources().getDrawable(R.drawable.ic_block_info_toast));

                        EventUtil.post(new Comm2GATT(READ_BLOCK_INFO_SINGLE, blockNum));
                        EventUtil.post("READ_BLOCK_INFO_SINGLE");
                    }
                })
                .setNegativeButton("CANCEL", null)
                .setView(til)
                .create();
        alertDialog.show();

        final Button btnPositive = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        final Button btnNegative = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

        tiEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) return;

                if (Integer.valueOf(s.toString()) > 43) {
                    til.setErrorEnabled(true);
                    til.setError("所选值大于43，建议修改");
                    btnPositive.setClickable(false);
                    btnPositive.setTextColor(getResources().getColor(R.color.colorAccent_undo));
                } else {
                    til.setErrorEnabled(false);
                    btnPositive.setClickable(true);
                    btnPositive.setTextColor(btnNegative.getCurrentTextColor());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

}
