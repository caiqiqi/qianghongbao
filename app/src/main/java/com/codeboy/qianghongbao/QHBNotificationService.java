package com.codeboy.qianghongbao;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * <p>Created 16/2/4 下午11:16.</p>
 * <p><a href="mailto:codeboy2013@gmail.com">Email:codeboy2013@gmail.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
/* `NotificationListenerService`: A sQHBNotifiService that receives calls from the *system*
   when new notifications are posted or removed, or their ranking changed. */
public class QHBNotificationService extends NotificationListenerService {

    private static final String TAG = "QHBNotificationService";

    private static QHBNotificationService sQHBNotifiService;

    @Override
    public void onCreate() {
        super.onCreate();
        //判断当前Android手机的版本号(是否 < 21)
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            onListenerConnected();
        }
    }

    private Config getConfig() {
        return Config.getConfig(this);
    }

    /*关键代码：一旦状态栏有通知发出之后，响应的方法(是由框架来调用的)*/
    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
        if(BuildConfig.DEBUG) {
            Log.i(TAG, "onNotificationRemoved");
        }
        if(!getConfig().isAgreement()) {
            return;
        }
        if(!getConfig().isEnabledNotificationService()) {
            return;
        }
        QHBAccessibilityService.handleNotificationPosted(new IStatusBarNotification() {
            @Override
            public String getPackageName() {
                return sbn.getPackageName();
            }

            @Override
            public Notification getNotification() {
                return sbn.getNotification();
            }
        });
    }

    /*一旦状态栏的通知被移除之后，响应的方法(是由框架来调用的)*/
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onNotificationRemoved(sbn);
        }
        //用来标示是否处在debug状态，从而决定要不要log出来
        if(BuildConfig.DEBUG) {
            Log.i(TAG, "onNotificationRemoved");
        }
    }

    /* 一旦该`NotificationListenerService`被绑定之后，响应的方法（是由框架来调用的）*/
    @Override
    public void onListenerConnected() {
        //版本 >=21
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onListenerConnected();
        }

        Log.i(TAG, "onListenerConnected");
        sQHBNotifiService = this;
        //发送广播，已经连接上了
        Intent intent = new Intent(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        sQHBNotifiService = null;
        //发送广播，已经断开连接了
        Intent intent = new Intent(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT);
        sendBroadcast(intent);
    }

    /** 判断是否启动通知栏监听*/
    public static boolean isNotificationServiceRunning() {
        if(sQHBNotifiService == null) {
            return false;
        }
        return true;
    }
}
