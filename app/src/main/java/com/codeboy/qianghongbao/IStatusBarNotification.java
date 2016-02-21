package com.codeboy.qianghongbao;

import android.app.Notification;

/**
 * <p>Created 16/2/7 下午5:48.</p>
 * <p><a href="mailto:730395591@qq.com">Email:730395591@qq.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 * 这两个接口方法声明 跟 `android.service.notification.StatusBarNotification`里的API是一样的 ！
 */
public interface IStatusBarNotification {

    /*The package of the app that posted the notification*/
    String getPackageName();
    /* The Notification supplied to notify(int, Notification).
     NotificationManager.notify(): Post a notification to be shown in the status bar
     */
    Notification getNotification();
}
