package com.codeboy.qianghongbao;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

/**
 * <p>Created 16/2/5 下午6:06.</p>
 * <p><a href="mailto:codeboy2013@gmail.com">Email:codeboy2013@gmail.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
public abstract class BaseSettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置Activity的内容(里面只有一个 container)
        setContentView(R.layout.activity_main);
        //将自己定义的Fragment的内容加到Activity的布局文件中
        getFragmentManager().beginTransaction().add(R.id.container, getSettingsFragment()).commitAllowingStateLoss();

        if(isShowBack()) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                /*调用该方法以后，该界面的图标左侧就会出现“<”符，但是点击的时候是不会有任何效果。
                这个功能的实现在：onSupportNavigateUp()方法
                */
                actionBar.setDisplayHomeAsUpEnabled(true);

            }
        }
    }

    protected boolean isShowBack() {
        return true;
    }

    /*这就是点击左上角的按钮之后做的操作*/
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /*虚拟的，等待被继承*/
    public abstract Fragment getSettingsFragment();

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
    }
}
