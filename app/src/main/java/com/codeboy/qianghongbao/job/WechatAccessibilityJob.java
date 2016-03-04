package com.codeboy.qianghongbao.job;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.codeboy.qianghongbao.BuildConfig;
import com.codeboy.qianghongbao.Config;
import com.codeboy.qianghongbao.IStatusBarNotification;
import com.codeboy.qianghongbao.QHBApplication;
import com.codeboy.qianghongbao.QHBAccessibilityService;
import com.codeboy.qianghongbao.util.AccessibilityHelper;
import com.codeboy.qianghongbao.util.NotifyHelper;

import java.util.List;

/**
 * <p>Created 16/1/16 上午12:40.</p>
 * <p><a href="mailto:codeboy2013@gmail.com">Email:codeboy2013@gmail.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
public class WechatAccessibilityJob extends BaseAccessbilityJob {

    private static final String TAG = "WechatAccessibilityJob";
    /** 微信的包名*/
    public static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";

    /* 红包消息的关键字*/
    private static final String TEXT_KEY_HONGBAO = "[微信红包]";
    /* 领取红包 */
    private static final String TEXT_KEY_LINGQU_HONGBAO = "领取红包" ;
    /* 拆红包 */
    private static final String TEXT_KEY_CHAI_HONGBAO = "拆红包" ;
    /* 发了红包 */
    private static final String TEXT_KEY_FA_HONGBAO_1 = "发了一个红包";
    private static final String TEXT_KEY_FA_HONGBAO_2 = "给你发了一个红包";
    private static final String TEXT_KEY_FA_HONGBAO_3 = "发了一个红包，金额随机";
    /* 看一看大家手气 */
    private static final String TEX_KEY_KAN_SHOUQI = "看看大家的手气" ;
    /* 返回 */
    private static final String TEXT_KEY_BACK = "返回" ;
    /*通过组件查找*/
    private static final String BUTTON_CLASS_NAME = "android.widget.Button";


    /** 不能再使用文字匹配的最小版本号 */
    private static final int USE_ID_MIN_VERSION = 700;// 6.3.8 对应code为680,6.3.9对应code为700

    private static final int WINDOW_NONE = 0;
	/*微信群聊或者单聊的发布红包的地方*/
    private static final int WINDOW_HONGBAO_RECEIVEUI = 1;
	/*打开红包之后的“红包细节” */
    private static final int WINDOW_HONGBAO_DETAIL = 2;
	/*桌面*/
    private static final int WINDOW_LAUNCHER = 3;
    private static final int WINDOW_OTHER = -1;

    /* 聊天界面中收到的`红包消息`界面*/
    public static final String UI_LUCKY_MONEY_RECEIVE = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    /* `红包细节`界面*/
    public static final String UI_LUCKY_MONEY_DETAIL =  "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    /* `微信启动界面`，即展示所有聊天列表的界面 */
    public static final String UI_WECHAT_LAUNCHER = "com.tencent.mm.ui.LauncherUI";

    /*标识当前所在窗口*/
    private int mCurrentWindow = WINDOW_NONE;

	/* 判断是否发布了红包 */
    private boolean isReceivingHongBao;
    /*包信息（微信的），为了得到版本号（微信的）*/
    private PackageInfo mWechatPackageInfo = null;
    private Handler mHandler = null;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //更新安装包信息(因为获取微信的版本时会用到)
            updatePackageInfo();
        }
    };

    /*主要是在上下文中注册广播*/
    @Override
    public void onCreateJob(QHBAccessibilityService service) {
        super.onCreateJob(service);

        //更新安装包信息(因为获取微信的版本时会用到)
        updatePackageInfo();

        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");

		/*getApplicationContext()的在其父类BaseAccessbilityJob中定义，即为qiangHongBaoService.getApplicationContext()得到整个应用级别的上下文*/
        getApplicationContext().registerReceiver(broadcastReceiver, filter);
    }

    /*就是在上下文中注销掉广播*/
    @Override
    public void onStopJob() {
        try {
            getApplicationContext().unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {}
    }

    @Override
    public void onReceiveJob(AccessibilityEvent event) {
        //事件类型
        final int eventType = event.getEventType();
        //通知栏事件
        if(eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            if(QHBAccessibilityService.isNotificationServiceRunning() && getConfig().isEnabledNotificationService()) { //开启快速模式，不处理
                return;
            }
            //获取事件的内容
            List<CharSequence> texts = event.getText();
            if(!texts.isEmpty()) {
                for(CharSequence t : texts) {
                    String text = String.valueOf(t);
                    //若事件内容中包含关键字 [微信红包]
                    if(text.contains(TEXT_KEY_HONGBAO)) {
                        openNotification(event);
                        break;
                    }
                }
            }
        } else if(eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            unpackHongBao(event);
        } else if(eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            //不在聊天界面或聊天列表，不处理
            if(mCurrentWindow != WINDOW_LAUNCHER) {
                return;
            }
            if(isReceivingHongBao) {
                handleChatListHongBao();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onNotificationPosted(IStatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        String ticker = String.valueOf(sbn.getNotification().tickerText);
        if(!ticker.contains(TEXT_KEY_HONGBAO)) {
            return;
        }
        try {
            // Notification.contentIntent: The intent to execute when the expanded status entry is clicked
            PendingIntent pendingIntent = notification.contentIntent;
            if(!NotifyHelper.isLockScreen(getApplicationContext())) {
                //send(): Perform the operation associated with this PendingIntent.
                pendingIntent.send();
            }
            NotifyHelper.checkAndPlayNotify(getApplicationContext(), getConfig(), pendingIntent, true);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isEnabledWechat() {
        return getConfig().isEnabledWechat();
    }

    /*获取微信包名——"com.tencent.mm" */
    @Override
    public String getTargetPackageName() {
        return WECHAT_PACKAGE_NAME;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    /* unpackHongBao这个名字就比之前的openHongBao要好*/
    private void unpackHongBao(AccessibilityEvent event) {
        // TODO:待补充
        if(UI_LUCKY_MONEY_RECEIVE.equals(event.getClassName())) {
            mCurrentWindow = WINDOW_HONGBAO_RECEIVEUI;
            //点中了红包，下一步就是去拆红包
            handleHongBaoReceived();
        } else if(UI_LUCKY_MONEY_DETAIL.equals(event.getClassName())) {
            mCurrentWindow = WINDOW_HONGBAO_DETAIL;
            //拆完红包后看详细的纪录界面
            if(getConfig().getHongBaoEvent(Config.KEY_EVENT_WECHAT_AFTER_UNPACK_HONGBAO,0) == Config.WX_AFTER_GET_GO_HOME) {
                //返回主界面，以便收到下一次的红包通知
                AccessibilityHelper.performHome(getQHBAccessibilityService());
            }
        } else if(UI_WECHAT_LAUNCHER.equals(event.getClassName())) {
            mCurrentWindow = WINDOW_LAUNCHER;
            //在聊天界面,去点中红包
            handleChatListHongBao();
        } else {
            mCurrentWindow = WINDOW_OTHER;
        }

        switch(event.getClassName().toString()){

            //收到红包的界面
            case UI_LUCKY_MONEY_RECEIVE:
                mCurrentWindow = WINDOW_HONGBAO_RECEIVEUI;
                break;
            case UI_LUCKY_MONEY_DETAIL:
                mCurrentWindow = WINDOW_HONGBAO_DETAIL;

                break;
            case UI_WECHAT_LAUNCHER:

                break;
            default :
                mCurrentWindow = WINDOW_OTHER;
        }
    }

    /**
     * 点击聊天里的红包后，显示的界面
     * */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void handleHongBaoReceived() {

        /*         `AccessibilityEvent.getSource()` 得到的是被点击的单体对象
        * API16以后`AccessibilityService.getRootInActiveWindow()` 得到整个窗口的对象
        * getRootInActiveWindow(): Gets the root node in the currently active window if this service can retrieve window content,
        * The active window is the one that the user is currently touching or the window with input focus, if the user is not touching any window.
        * */

        //得到当前正在与用户交互的窗口
        AccessibilityNodeInfo nodeInfo = getQHBAccessibilityService().getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }

        AccessibilityNodeInfo targetNode = null;

        // 事件代号
        int event = getConfig().getWechatAfterUnpackHongBaoEvent();
        int wechatVersion = getWechatVersion();
        if(event == Config.WX_AFTER_OPEN_HONGBAO) { //拆红包
            if (wechatVersion < USE_ID_MIN_VERSION) {
                targetNode = AccessibilityHelper.findNodeInfosByText(nodeInfo, TEXT_KEY_CHAI_HONGBAO);
            } else {
                String buttonId = "com.tencent.mm:id/b43";

                if(wechatVersion == 700) {
                    buttonId = "com.tencent.mm:id/b2c";
                }

                if(buttonId != null) {
                    targetNode = AccessibilityHelper.findNodeInfosById(nodeInfo, buttonId);

                }

                if(targetNode == null) {
                    //分别对应固定金额的红包 拼手气红包
                    AccessibilityNodeInfo textNode = AccessibilityHelper.findNodeInfosByTexts(nodeInfo, TEXT_KEY_FA_HONGBAO_1, TEXT_KEY_FA_HONGBAO_2, TEXT_KEY_FA_HONGBAO_3);

                    if(textNode != null) {
                        for (int i = 0; i < textNode.getChildCount(); i++) {
                            AccessibilityNodeInfo node = textNode.getChild(i);
                            if (BUTTON_CLASS_NAME.equals(node.getClassName())) {
                                targetNode = node;
                                break;
                            }
                        }
                    }
                }

                if(targetNode == null) {
                    //通过组件查找
                    targetNode = AccessibilityHelper.findNodeInfosByClassName(nodeInfo, BUTTON_CLASS_NAME);
                }
            }
        } else if(event == Config.WX_AFTER_OPEN_SEE) { //看一看
            if(getWechatVersion() < USE_ID_MIN_VERSION) { //低版本才有 看大家手气的功能
                targetNode = AccessibilityHelper.findNodeInfosByText(nodeInfo, TEX_KEY_KAN_SHOUQI);
            }
        } else if(event == Config.WX_AFTER_OPEN_NONE) {
            return;
        }

        if(targetNode != null) {
            final AccessibilityNodeInfo n = targetNode;
            long sDelayTime = getConfig().getWechatOpenDelayTime();
            if(sDelayTime != 0) {
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AccessibilityHelper.performClick(n);
                    }
                }, sDelayTime);
            } else {
                AccessibilityHelper.performClick(n);
            }
            if(event == Config.WX_AFTER_OPEN_HONGBAO) {
                QHBApplication.eventStatistics(getApplicationContext(), "open_hongbao");
            } else {
                QHBApplication.eventStatistics(getApplicationContext(), "open_see");
            }
        }
    }

    /**
     * 处在微信消息列表界面时，处理红包
     * */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handleChatListHongBao() {
        int mode = getConfig().getWechatMode();
        if(mode == Config.WX_MODE_3) { //只通知模式
            return;
        }

        AccessibilityNodeInfo nodeInfo = getQHBAccessibilityService().getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }

        if(mode != Config.WX_MODE_0) {
            boolean isMember = isGroupChatUi(nodeInfo);
            if(mode == Config.WX_MODE_1 && isMember) {//过滤群聊
                return;
            } else if(mode == Config.WX_MODE_2 && !isMember) { //过滤单聊
                return;
            }
        }

        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(TEXT_KEY_LINGQU_HONGBAO);

        if(list != null && list.isEmpty()) {
            // 从消息列表查找红包
            AccessibilityNodeInfo node = AccessibilityHelper.findNodeInfosByText(nodeInfo, TEXT_KEY_HONGBAO);
            if(node != null) {
                if(BuildConfig.DEBUG) {
                    Log.i(TAG, "-->微信红包:" + node);
                }
                isReceivingHongBao = true;
                //模拟执行点击事件
                AccessibilityHelper.performClick(nodeInfo);
            }
        } else if(list != null) {
            if (isReceivingHongBao){
                //最新的红包领起
                AccessibilityNodeInfo node = list.get(list.size() - 1);
                //模拟执行点击事件
                AccessibilityHelper.performClick(node);
                isReceivingHongBao = false;
            }
        }
    }

    /** 打开通知栏消息*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openNotification(AccessibilityEvent event) {
        Parcelable data = event.getParcelableData();
        if(data == null || !(data instanceof Notification)) {
            return;
        }

        /*能够进入 openNotification()这个方法内部，则认为已经发布了红包（这个逻辑判断的责任交给调用这个方法的地方）*/
        isReceivingHongBao = true;
        try {
            //以下是精华！！！将微信的通知栏消息打开
            Notification notification = (Notification) data;
            PendingIntent pendingIntent = notification.contentIntent;
            //先确定是不是处于“非锁屏”状态下
            if(!NotifyHelper.isLockScreen(getApplicationContext())) {
                pendingIntent.send();
            }
            NotifyHelper.checkAndPlayNotify(getApplicationContext(), getConfig(), pendingIntent, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*关于pendingIntent:
        * PendingIntent就是一个可以在满足一定条件下执行的Intent，它相比于Intent的优势在于自己携带有Context对象，这样他就不必依赖于某个activity才可以存在。
        * */
    }

    /** 是否为群聊天*/
    private boolean isGroupChatUi(AccessibilityNodeInfo nodeInfo) {
        if(nodeInfo == null) {
            return false;
        }
        String id = "com.tencent.mm:id/ces";
        int wv = getWechatVersion();
        if(wv <= 680) {
            id = "com.tencent.mm:id/ew";
        } else if(wv <= 700) {
            id = "com.tencent.mm:id/cbo";
        }
        String title = null;
        AccessibilityNodeInfo target = AccessibilityHelper.findNodeInfosById(nodeInfo, id);
        if(target != null) {
            title = String.valueOf(target.getText());
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(TEXT_KEY_BACK);

        if(list != null && !list.isEmpty()) {
            AccessibilityNodeInfo parent = null;
            for(AccessibilityNodeInfo node : list) {
                if(!"android.widget.ImageView".equals(node.getClassName())) {
                    continue;
                }
                String desc = String.valueOf(node.getContentDescription());
                if(!"返回".equals(desc)) {
                    continue;
                }
                parent = node.getParent();
                break;
            }
            if(parent != null) {
                parent = parent.getParent();
            }
            if(parent != null) {
                if( parent.getChildCount() >= 2) {
                    AccessibilityNodeInfo node = parent.getChild(1);
                    if("android.widget.TextView".equals(node.getClassName())) {
                        title = String.valueOf(node.getText());
                    }
                }
            }
        }


        if(title != null && title.endsWith(")")) {
            return true;
        }
        return false;
    }

    /*单例模式*/
    private Handler getHandler() {
        if(mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    /** 获取微信的版本*/
    private int getWechatVersion() {
        if(mWechatPackageInfo == null) {
            return 0;
        }
        return mWechatPackageInfo.versionCode;
    }

    /** 更新微信包信息*/
    private void updatePackageInfo() {
        try {
            mWechatPackageInfo = getApplicationContext().getPackageManager().getPackageInfo(WECHAT_PACKAGE_NAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
