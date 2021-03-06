package com.codeboy.qianghongbao;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * <p>Created 16/1/15 下午10:59.</p>
 * <p><a href="mailto:codeboy2013@gmail.com">Email:codeboy2013@gmail.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
public class Config {

    /*断开抢红包辅助服务*/
    public static final String ACTION_QHB_ACCESSIBILITY_SERVICE_DISCONNECT = "com.codeboy.qianghongbao.ACCESSBILITY_DISCONNECT";
    /*连接抢红包辅助服务*/
    public static final String ACTION_QHB_ACCESSIBILITY_SERVICE_CONNECT = "com.codeboy.qianghongbao.ACCESSBILITY_CONNECT";

    public static final String ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT = "com.codeboy.qianghongbao.NOTIFY_LISTENER_DISCONNECT";
    public static final String ACTION_NOTIFY_LISTENER_SERVICE_CONNECT = "com.codeboy.qianghongbao.NOTIFY_LISTENER_CONNECT";


    public static final String PREFERENCE_NAME = "config";
    /*是否启动微信抢红包*/
    public static final String KEY_ENABLE_WECHAT = "KEY_ENABLE_WECHAT";
    /*微信拆开红包后延时时间*/
    public static final String KEY_WECHAT_DELAY_TIME = "KEY_WECHAT_DELAY_TIME";
    /*微信-拆开红包后的事件*/
    public static final String KEY_EVENT_WECHAT_AFTER_UNPACK_HONGBAO = "KEY_EVENT_WECHAT_AFTER_UNPACK_HONGBAO";
    /*微信-抢到-红包后的事件*/
    public static final String KEY_EVENT_WECHAT_AFTER_GET_HONGBAO = "KEY_EVENT_WECHAT_AFTER_GET_HONGBAO";

    /*获取抢微信红包的模式*/
    public static final String KEY_WECHAT_MODE = "KEY_WECHAT_MODE";

    /*是否启动"快速监听通知栏"模式*/
    public static final String KEY_NOTIFICATION_SERVICE_ENABLE = "KEY_NOTIFICATION_SERVICE_ENABLE";

    /*是否开启声音*/
    public static final String KEY_NOTIFY_SOUND = "KEY_NOTIFY_SOUND";
    /*是否开启震动*/
    public static final String KEY_NOTIFY_VIBRATE = "KEY_NOTIFY_VIBRATE";
    /*是否开启夜间免打扰模式*/
    public static final String KEY_NOTIFY_NIGHT_ENABLE = "KEY_NOTIFY_NIGHT_ENABLE";

    /*是否同意免责声明*/
    private static final String KEY_AGREEMENT = "KEY_AGREEMENT";

    public static final int WX_AFTER_OPEN_HONGBAO = 0;//拆红包
    public static final int WX_AFTER_OPEN_SEE = 1; //看大家手气
    public static final int WX_AFTER_OPEN_NONE = 2; //静静地看着

    public static final int WX_AFTER_GET_GO_HOME = 0; //返回桌面
    public static final int WX_AFTER_GET_NONE = 1;

    public static final int WX_MODE_0 = 0;//自动抢
    public static final int WX_MODE_1 = 1;//抢单聊红包,群聊红包只通知
    public static final int WX_MODE_2 = 2;//抢群聊红包,单聊红包只通知
    public static final int WX_MODE_3 = 3;//通知手动抢

    private static Config current;

    public static synchronized Config getConfig(Context context) {
        //getApplicationContext()是获得整个应用的上下文，其生命周期是整个应用
        if(current == null) {
            current = new Config(context.getApplicationContext());
        }
        return current;
    }

    private SharedPreferences preferences;
    /* Application级别的Context */
    private Context mContext;

    private Config(Context context) {
        mContext = context;
        preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /** 是否启动微信抢红包*/
    public boolean isEnabledWechat() {
        return preferences.getBoolean(KEY_ENABLE_WECHAT, true) && UmengConfig.isEnabledWechat(mContext);
    }

    /** 微信拆开红包后的事件*/
    public int getWechatAfterUnpackHongBaoEvent() {
        int defaultValue = 0;
        String result =  preferences.getString(KEY_EVENT_WECHAT_AFTER_UNPACK_HONGBAO, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /** 微信抢到红包后的事件*/
    public int getWechatAfterGetHongBaoEvent() {
        int defaultValue = 1;
        String result =  preferences.getString(KEY_EVENT_WECHAT_AFTER_GET_HONGBAO, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /** 得到事件代号：“抢到红包后” 或者 “拆开红包后”*/
    public int getHongBaoEvent(String eventName,int defaultValue) {

        String result = preferences.getString(eventName, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /** 微信拆开红包后延时时间*/
    public int getWechatOpenDelayTime() {
        int defaultValue = 0;
        String result = preferences.getString(KEY_WECHAT_DELAY_TIME, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /** 获取抢微信红包的模式*/
    public int getWechatMode() {
        int defaultValue = 0;
        String result = preferences.getString(KEY_WECHAT_MODE, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /** 是否启动通知栏模式*/
    public boolean isEnabledNotificationService() {
        return preferences.getBoolean(KEY_NOTIFICATION_SERVICE_ENABLE, false);
    }

    /** 是否开启声音*/
    public boolean isNotifySound() {
        return preferences.getBoolean(KEY_NOTIFY_SOUND, true);
    }

    /** 是否开启震动*/
    public boolean isNotifyVibrate() {
        return preferences.getBoolean(KEY_NOTIFY_VIBRATE, true);
    }

    /** 是否开启夜间免打扰模式*/
    public boolean isNotifyNight() {
        return preferences.getBoolean(KEY_NOTIFY_NIGHT_ENABLE, false);
    }

    /** 免费声明*/
    public boolean isAgreement() {
        return preferences.getBoolean(KEY_AGREEMENT, false);
    }

    /** 设置是否同意*/
    public void setAgreement(boolean agreement) {
        preferences.edit().putBoolean(KEY_AGREEMENT, agreement).apply();
    }

}
