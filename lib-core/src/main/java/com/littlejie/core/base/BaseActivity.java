package com.littlejie.core.base;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.littlejie.core.manager.ActivityManager;

import butterknife.ButterKnife;

/**
 * BaseActivity，所有子类 Activity 都应继承该类，封装 ButterKnife
 * Created by littlejie on 2016/4/6.
 */
public abstract class BaseActivity extends AppCompatActivity {

    public final String TAG = this.getClass().getSimpleName();

    protected Context mContext;
    protected int requestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        //设置所有 Activity 全部为竖屏
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(getPageLayoutID());
        ActivityManager.addActivity(this);
        ButterKnife.bind(this);
        initData();
        initView();
        initViewListener();
        process();
    }

    /**
     * 设置页面布局ID
     *
     * @return
     */
    protected abstract int getPageLayoutID();

    /**
     * 初始化数据
     */
    protected abstract void initData();

    /**
     * 初始化页面控件
     */
    protected abstract void initView();

    /**
     * 初始化控件监听器
     */
    protected abstract void initViewListener();

    protected abstract void process();

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.removeActivity(this);
    }

    /*--------------运行时权限相关，未完成----------------*/
    protected void requestPermission(final String permission, String reason, final int requestCode) {
        this.requestCode = requestCode;
        if (checkPermission(permission)) {
            Log.i(TAG, permission + " has NOT been granted. Requesting permissions.");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                //弹窗显示请求原因，并重新请求
                Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
            } else {
                requestPermission(new String[]{permission}, requestCode);
            }
        } else {
            Log.i(TAG, permission + " have already been granted.");
            processWithPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == this.requestCode) {
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, permissions[0] + "permission has now been granted.");
            } else {
                Log.i(TAG, "permission was NOT granted.");
                this.finish();
            }
        }
    }

    private boolean checkPermission(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String permission, int requestCode) {
        requestPermission(new String[]{permission}, requestCode);
    }

    private void requestPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    /**
     * 拥有权限时处理
     */
    protected void processWithPermission() {

    }
    /*--------------运行时权限相关，未完成----------------*/

    /*-------------------软键盘--------------*/
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
    /*-------------------软键盘--------------*/
}
