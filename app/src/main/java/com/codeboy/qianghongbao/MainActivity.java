package com.codeboy.qianghongbao;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.codeboy.qianghongbao.job.WechatAccessibilityJob;
import com.codeboy.qianghongbao.util.BitmapUtils;

import java.io.File;

/**
 * <p>Created by LeonLee on 15/2/17 下午10:11.</p>
 * <p><a href="mailto:codeboy2013@163.com">Email:codeboy2013@163.com</a></p>
 *
 * 抢红包主界面
 */
public class MainActivity extends BaseSettingsActivity {

    /* "关注codeboy微信" */
    private static final String KEY_FOLLOW_ME =       "KEY_FOLLOW_ME" ;
    /* "打赏开发者" */
    private static final String KEY_DONATE_ME =       "KEY_DONATE_ME" ;
    /* "微信设置" */
    private static final String KEY_WECHAT_SETTINGS = "WECHAT_SETTINGS" ;
    /* 红包提醒设置 */
    private static final String KEY_NOTIFY_SETTINGS = "NOTIFY_SETTINGS" ;
    /*注册qhb*/
    private static final String REGISTER_RECEIVER_FAILED = "qhbConnectReceiver is null,registerReceiver failed!";

    /*提示开启辅助服务的Dialog*/
    private Dialog mTipsDialog;
    /*待装载的Fragment*/
    private MainFragment mMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*这里调用父类BaseActivity的onCreate()中的代码，getSettingsFragment(),而该方法在本类中被复写*/
        super.onCreate(savedInstanceState);

        setApplicationTitle();

        //暂时没有实现
        QHBApplication.activityStartMain(this);

        registerQhbConnectReceiver();

    }

    private void registerQhbConnectReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.ACTION_QHB_ACCESSIBILITY_SERVICE_CONNECT);
        filter.addAction(Config.ACTION_QHB_ACCESSIBILITY_SERVICE_DISCONNECT);
        filter.addAction(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT);
        filter.addAction(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT);
        //注册广播
        if(qhbConnectReceiver != null){
            registerReceiver(qhbConnectReceiver, filter);
        }else {
            Toast.makeText(MainActivity.this,REGISTER_RECEIVER_FAILED,Toast.LENGTH_SHORT).show();
        }
    }

    /*得到应用的版本信息并显示在ActionBar上*/
    private void setApplicationTitle() {
        //打开应用之后再获取标题（涉及到版本号）
        String version = "";
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = " v" + info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        setTitle(getString(R.string.app_name) + version);
    }

    @Override
    protected boolean isShowBack() {
        return false;
    }

    /*这里覆写了父类BaseActivity的方法，该方法在MainActivity的super.onCreate()中会调用*/
    @Override
    public Fragment getSettingsFragment() {
        mMainFragment = new MainFragment();
        return mMainFragment;
    }

    private BroadcastReceiver qhbConnectReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            //判断Activity是否已被finish掉，如果Activitiy已被finish，会出bug
            if(isFinishing()) {
                return;
            }
            String action = intent.getAction();
            Log.d("MainActivity", "receive-->" + action);

            switch(action){
                //若抢红包辅助服务已连接，但DIalog依然在显示，则dismiss掉
                case Config.ACTION_QHB_ACCESSIBILITY_SERVICE_CONNECT:
                    if (mTipsDialog != null) {
                        mTipsDialog.dismiss();
                    }
                    break;
                //若抢红包辅助服务状态为未连接，则展示Dialog
                case Config.ACTION_QHB_ACCESSIBILITY_SERVICE_DISCONNECT:
                    showOpenAccessibilityServiceDialog();
                    break;
                case Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT:
                    if(mMainFragment != null) {
                        mMainFragment.updateNotifyPreference();
                    }
                    break;
                case Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT:
                    if(mMainFragment != null) {
                        mMainFragment.updateNotifyPreference();
                    }
                    break;
            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        checkQiangHongBaoService();

        checkIsAgreement();


    }

    /*免责声明  本质是获取preferences的值，只有点击了同意，该值才会为true
    * 在onResume里面写，保证每次Activity呈现给用户的时候都能看到“免责声明”
    * 若之前用户未同意，确保用户同意
    * */
    private void checkIsAgreement() {
        boolean isAgreement = Config.getConfig(this).isAgreement();
        if(!isAgreement) {
            showAgreementDialog();
        }
    }

    /**检查抢红包服务是否在运行*/
    private void checkQiangHongBaoService() {
        if(QHBAccessibilityService.isAccessibilityServiceRunning()) {
            if(mTipsDialog != null) {
                mTipsDialog.dismiss();
            }
        } else {
            showOpenAccessibilityServiceDialog();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(qhbConnectReceiver);
        } catch (Exception e) {}
        mTipsDialog = null;
    }

    /*菜单*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //“打开辅助服务”
        MenuItem item = menu.add(0, 0, 1, R.string.open_service_button);
        item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
        //“打开通知栏服务”
        MenuItem notifyitem = menu.add(0, 3, 2, R.string.open_notify_service);
        notifyitem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
        //“关于CodeBoy抢红包”
        MenuItem about = menu.add(0, 4, 4, R.string.about_title);
        about.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);

        return super.onCreateOptionsMenu(menu);
    }

    /*菜单*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                openAccessibilityServiceSettings();
                QHBApplication.eventStatistics(this, "menu_service");
                return true;
            case 3:
                openNotificationServiceSettings();
                QHBApplication.eventStatistics(this, "menu_notify");
                break;
            case 4:
                openAboutMeActivity();
                QHBApplication.eventStatistics(this, "menu_about");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openAboutMeActivity() {
        startActivity(new Intent(this, AboutMeActivity.class));
    }

    /** 显示免责声明的对话框*/
    private void showAgreementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.agreement_title);
        builder.setMessage(getString(R.string.agreement_message, getString(R.string.app_name)));
        builder.setPositiveButton("同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Config.getConfig(getApplicationContext()).setAgreement(true);
                QHBApplication.eventStatistics(MainActivity.this, "agreement", "true");
            }
        });
        builder.setNegativeButton("不同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Config.getConfig(getApplicationContext()).setAgreement(false);
                QHBApplication.eventStatistics(MainActivity.this, "agreement", "false");
                finish();
            }
        });
        builder.show();
    }

    /** 分享*/
    private void showShareDialog() {
        QHBApplication.showShare(this);
    }

    /** 二维码 Dialog*/
    private void showQrDialog() {
        final Dialog dialog = new Dialog(this, R.style.QR_Dialog_Theme);
        View view = getLayoutInflater().inflate(R.layout.qr_dialog_layout, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = getString(R.string.qr_wx_id);
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", id);
                clipboardManager.setPrimaryClip(clip);

                //跳到微信
                Intent wxIntent = getPackageManager().getLaunchIntentForPackage(
                        WechatAccessibilityJob.WECHAT_PACKAGENAME);
                if(wxIntent != null) {
                    try {
                        startActivity(wxIntent);
                    } catch (Exception e){}
                }

                Toast.makeText(getApplicationContext(), "已复制到粘贴板", Toast.LENGTH_LONG).show();
                QHBApplication.eventStatistics(MainActivity.this, "copy_qr");
                dialog.dismiss();
            }
        });
        dialog.setContentView(view);
        dialog.show();
    }

    /** 捐赠 Dialog*/
    private void showDonateDialog() {
        final Dialog dialog = new Dialog(this, R.style.QR_Dialog_Theme);
        View view = getLayoutInflater().inflate(R.layout.donate_dialog_layout, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                File output = new File(android.os.Environment.getExternalStorageDirectory(), "codeboy_wechatpay_qr.jpg");
                if(!output.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wechatpay_qr);
                    BitmapUtils.saveBitmap(MainActivity.this, output, bitmap);
                }
                Toast.makeText(MainActivity.this, "已保存到:" + output.getAbsolutePath(), Toast.LENGTH_LONG).show();
                return true;
            }
        });
        dialog.setContentView(view);
        dialog.show();
    }

    /** 显示未开启辅助服务的对话框*/
    private void showOpenAccessibilityServiceDialog() {
        if(mTipsDialog != null && mTipsDialog.isShowing()) {
            return;
        }
        //加载进来那个“由上下两个分割线包围的中间夹一个图片的那个view”
        View view = getLayoutInflater().inflate(R.layout.dialog_tips_layout, null);
        //也就是说这个view本身也有监听点击之后的响应方法！！！
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccessibilityServiceSettings();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.open_service_title);
        //哦，原来AlertDialog是可以加载任意自定义View的
        builder.setView(view);
        builder.setPositiveButton(R.string.open_service_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openAccessibilityServiceSettings();
            }
        });
        mTipsDialog = builder.show();
    }

    /** 打开辅助服务的设置*/
    private void openAccessibilityServiceSettings() {
        try {
            // 固有Action："android.settings.ACCESSIBILITY_SETTINGS"
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            // 即打开“设置”里面的“辅助服务”这个界面
            startActivity(intent);
            // “找到[codeboy]抢红包，然后开启服务即可”
            Toast.makeText(this, R.string.tips, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 打开通知栏设置*/
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private void openNotificationServiceSettings() {
        try {
            // 固有Action："android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            // 应该是“设置”里面的“通知使用权”这个界面
            startActivity(intent);
            // "找到[CodeBoy抢红包]，然后开启服务即可"
            Toast.makeText(this, R.string.tips, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class MainFragment extends BaseSettingsFragment {

        /* "快速监听通知栏"开关 */
        private SwitchPreference notificationPref;
        /* 是否开启-"快速监听通知栏"开关*/
        private boolean targetNotificationValue;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            /*PreferenceFragment的方法：从xml形式的preference中导进来
            给这个PreferenceFragment指定了一个xml
            这样，当打开此界面时可以导入
            */
            addPreferencesFromResource(R.xml.main);

            // 1. 微信红包开关
            Preference wechatPref = findPreference(Config.KEY_ENABLE_WECHAT);
            wechatPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean) newValue && !QHBAccessibilityService.isAccessibilityServiceRunning()) {
                        ((MainActivity)getActivity()).showOpenAccessibilityServiceDialog();
                    }
                    return true;
                }
            });
            if(!UmengConfig.isEnableWechat(getActivity())) {
                wechatPref.setEnabled(false);
                wechatPref.setTitle("暂时不能使用");
            }

            // 2. "关注codeboy微信"
            findPreference(KEY_FOLLOW_ME).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // TODO: 这里可以传一个参数
                    /*显示 "关注微信" 二维码Dialog*/
                    ((MainActivity)getActivity()).showQrDialog();
                    QHBApplication.eventStatistics(getActivity(), "about_author");
                    return true;
                }
            });

            // 3. 打赏二维码
            findPreference(KEY_DONATE_ME).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // TODO: 这里可以传一个参数
                    /*显示 "打赏二维码" 二维码Dialog*/
                    ((MainActivity)getActivity()).showDonateDialog();
                    QHBApplication.eventStatistics(getActivity(), "donate");
                    return true;
                }
            });

            // 4. 微信设置
            findPreference(KEY_WECHAT_SETTINGS).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    /* Activity-微信设置 */
                    startActivity(new Intent(getActivity(), WechatSettingsActivity.class));
                    return true;
                }
            });

            // 5. 快速监听通知栏
            notificationPref = (SwitchPreference) findPreference(Config.KEY_NOTIFICATION_SERVICE_ENABLE);
            notificationPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        Toast.makeText(getActivity(), "该功能只支持安卓4.3以上的系统", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    targetNotificationValue = (Boolean)newValue;
                    if((Boolean) newValue && !QHBAccessibilityService.isNotificationServiceRunning()) {
                        ((MainActivity)getActivity()).openNotificationServiceSettings();
                        return false;
                    }
                    QHBApplication.eventStatistics(getActivity(), "notify_service", String.valueOf(newValue));
                    return true;
                }
            });

            // 6. 红包提醒设置
            findPreference(KEY_NOTIFY_SETTINGS).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    /* Activity-红包提醒设置*/
                    startActivity(new Intent(getActivity(), NotifySettingsActivity.class));
                    return true;
                }
            });
        }

        /** 更新快速读取通知的设置*/
        public void updateNotifyPreference() {
            if(notificationPref == null) {
                return;
            }
            if(targetNotificationValue && !notificationPref.isChecked() && QHBAccessibilityService.isNotificationServiceRunning()) {
                QHBApplication.eventStatistics(getActivity(), "notify_service", String.valueOf(true));
                notificationPref.setChecked(true);
            } else if(notificationPref.isChecked() && !QHBAccessibilityService.isNotificationServiceRunning()) {
                notificationPref.setChecked(false);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            updateNotifyPreference();
        }
    }
}
