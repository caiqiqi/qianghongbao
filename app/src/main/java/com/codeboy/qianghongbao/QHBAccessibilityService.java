package com.codeboy.qianghongbao;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.codeboy.qianghongbao.job.AccessbilityJob;
import com.codeboy.qianghongbao.job.WechatAccessibilityJob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * <p>Created by LeonLee on 15/2/17 下午10:25.</p>
 * <p><a href="mailto:codeboy2013@163.com">Email:codeboy2013@163.com</a></p>
 *
 * 抢红包外挂服务
 */
public class QHBAccessibilityService extends AccessibilityService {

    private static final String TAG = "QHBAccessibilityService";

    private static final String QHB_ACCESSIBILITY_SERVICE_INTERRUPTED = "中断抢红包服务";
    private static final String QHB_ACCESSIBILITY_SERVICE_CONNECTED = "已连接抢红包服务" ;

    private static final Class[] ACCESSBILITY_JOBS= {
            WechatAccessibilityJob.class,
    };

    private static QHBAccessibilityService sService;

    private List<AccessbilityJob> mList_jobs;
    //Key：应用的包名
    private HashMap<String, AccessbilityJob> mMap_jobs;

    @Override
    public void onCreate() {
        super.onCreate();

        mList_jobs = new ArrayList<>();
        mMap_jobs = new HashMap<>();

        //初始化辅助插件工作
        for(Class clazz : ACCESSBILITY_JOBS) {
            try {
                Object object = clazz.newInstance();
                if(object instanceof AccessbilityJob) {
                    AccessbilityJob job = (AccessbilityJob) object;
                    /*onCreateJob*/
                    job.onCreateJob(this);
                    mList_jobs.add(job);
                    mMap_jobs.put(job.getTargetPackageName(), job);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "qianghongbao sService destoryed");
        if(mMap_jobs != null) {
			//Map.clear()的实现也是讲Map中所有元素都赋值null
            mMap_jobs.clear();
        }
        if(mList_jobs != null && !mList_jobs.isEmpty()) {
            for (AccessbilityJob job : mList_jobs) {
                /*onStopJob*/
                job.onStopJob();
            }
			//List.clear()的实现其实也是遍历list内数组将各个元素赋值null，让gc去回收每个元素
            mList_jobs.clear();
        }

        sService = null;
        mList_jobs = null;
        mMap_jobs = null;
        //发送广播，已经断开辅助服务
        Intent intent = new Intent(Config.ACTION_QHB_ACCESSIBILITY_SERVICE_DISCONNECT);
        sendBroadcast(intent);
    }

    /*复写AccessibilityService的方法*/
    @Override
    //在“设置”->“辅助功能”里面打开了“抢红包”
    protected void onServiceConnected() {
        super.onServiceConnected();
        //在这个辅助服务开启之后，这个service对象才赋有值，不然下面判断if(sService==null)就通不过
        sService = this;
        //发送广播，已经连接上了
        Intent intent = new Intent(Config.ACTION_QHB_ACCESSIBILITY_SERVICE_CONNECT);
        sendBroadcast(intent);
        Toast.makeText(this, QHB_ACCESSIBILITY_SERVICE_CONNECTED, Toast.LENGTH_SHORT).show();
    }

	/*服务中断，如授权关闭或者将服务杀死*/
    @Override
    public void onInterrupt() {
        Log.d(TAG, "qianghongbao sService interrupt");
        Toast.makeText(this, QHB_ACCESSIBILITY_SERVICE_INTERRUPTED, Toast.LENGTH_SHORT).show();
    }


    /*接收事件,如触发了通知栏变化、界面变化等*/
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "事件--->" + event );
        }
        String pkn = String.valueOf(event.getPackageName());
        if(mList_jobs != null && !mList_jobs.isEmpty()) {
            //此时若发现用户并没有同意“免责声明”，则直接返回
            if(!getConfig().isAgreement()) {
                return;
            }
            for (AccessbilityJob job : mList_jobs) {
                if(pkn.equals(job.getTargetPackageName()) && job.isEnabledWechat()) {
                    /*onReceiveJob*/
                    job.onReceiveJob(event);
                }
            }
        }
    }

    public Config getConfig() {
        return Config.getConfig(this);
    }

    /** 处理接收到的通知栏事件*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	//注意：方法是静态的，所以是外部类直接调用
    public static void handleNotificationPosted(IStatusBarNotification notification) {
        if(notification == null) {
            return;
        }
        if(sService == null || sService.mMap_jobs == null) {
            return;
        }
        String pkgName = notification.getPackageName();
        AccessbilityJob job = sService.mMap_jobs.get(pkgName);
        if(job == null) {
            return;
        }
        /*onNotificationPosted*/
        job.onNotificationPosted(notification);
    }

    /**
     * 判断当前辅助服务是否正在运行
     * */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isQHBAccessibilityServiceRunning() {
        if(sService == null) {
            return false;
        }
        AccessibilityManager accessibilityManager = (AccessibilityManager) sService.getSystemService(Context.ACCESSIBILITY_SERVICE);
        AccessibilityServiceInfo info = sService.getServiceInfo();
        if(info == null) {
            return false;
        }
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        Iterator<AccessibilityServiceInfo> iterator = list.iterator();

        boolean isConnect = false;
        while (iterator.hasNext()) {
            AccessibilityServiceInfo i = iterator.next();
            if(i.getId().equals(info.getId())) {
                isConnect = true;
                break;
            }
        }
        if(!isConnect) {
            return false;
        }
        return true;
    }

    /** 判断快速读取通知栏服务是否启动*/
    public static boolean isNotificationServiceRunning() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return false;
        }
        //部份手机没有NotificationService服务
        try {
            return QHBNotificationService.isNotificationServiceRunning();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }


}
