package com.codeboy.qianghongbao.util;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.reflect.Field;
import java.util.List;

/**
 * <p>Created 16/2/4 上午9:49.</p>
 * <p><a href="mailto:730395591@qq.com">Email:730395591@qq.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
public final class AccessibilityHelper {

    private AccessibilityHelper() {}

    /** 通过`id`查找*/
    public static AccessibilityNodeInfo findNodeInfosById(AccessibilityNodeInfo nodeInfo, String resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //所有能匹配到的，是一个List
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(resId);
            if(list != null && !list.isEmpty()) {
                //只返回List的第一个元素
                return list.get(0);
            }
        }
        return null;
    }

    /**
     * 通过`Id`(上面)和`文本`(下面)的这两个方法，算法是一样的
     */

    /** 通过`文本`查找*/
    public static AccessibilityNodeInfo findNodeInfosByText(AccessibilityNodeInfo nodeInfo, String text) {
        //所有能匹配到的，是一个List
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
        if(list == null || list.isEmpty()) {
            return null;
        }
        //只返回List的第一个元素
        return list.get(0);
    }

    /** 通过`关键字`查找(对各个关键字轮流调用`findNodeInfosByText`)
     * @param texts :不定长度的字符串参数
     * */
    public static AccessibilityNodeInfo findNodeInfosByTexts(AccessibilityNodeInfo nodeInfo, String... texts) {
        for(String key : texts) {
            AccessibilityNodeInfo info = findNodeInfosByText(nodeInfo, key);
            if(info != null) {
                return info;
            }
        }
        return null;
    }

    /** 通过`组件名字`查找*/
    public static AccessibilityNodeInfo findNodeInfosByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if(TextUtils.isEmpty(className)) {
            return null;
        }
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo node = nodeInfo.getChild(i);
            if(className.equals(node.getClassName())) {
                return node;
            }
        }
        return null;
    }

    /** [no usage]找父组件*/
    public static AccessibilityNodeInfo findParentNodeInfosByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if(nodeInfo == null) {
            return null;
        }
        if(TextUtils.isEmpty(className)) {
            return null;
        }
        if(className.equals(nodeInfo.getClassName())) {
            return nodeInfo;
        }
        return findParentNodeInfosByClassName(nodeInfo.getParent(), className);
    }

    /*这里的static变量应该初始化，不然会报错*/
//    private static final Field sSourceNodeField;

//    static {
//        Field field = null;
//        try {
//            field = AccessibilityNodeInfo.class.getDeclaredField("mSourceNodeId");
//            field.setAccessible(true);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        sSourceNodeField = field;
//    }

    /*[no usage]*/
//    public static long getSourceNodeId (AccessibilityNodeInfo nodeInfo) {
//        if(sSourceNodeField == null) {
//            return -1;
//        }
//        try {
//            return sSourceNodeField.getLong(nodeInfo);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return -1;
//    }

    /*[no usage]*/
    public static String getViewIdResourceName(AccessibilityNodeInfo nodeInfo) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return nodeInfo.getViewIdResourceName();
        }
        return null;
    }

    /** 模拟返回主界面事件*/
    public static void performHome(AccessibilityService service) {
        if(service == null) {
            return;
        }
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    /* [no usage] 模拟"BACK"事件 */
    public static void performBack(AccessibilityService service) {
        if(service == null) {
            return;
        }
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    /** 模拟执行点击事件*/
    public static void performClick(AccessibilityNodeInfo nodeInfo) {
        if(nodeInfo == null) {
            return;
        }
        if(nodeInfo.isClickable()) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            performClick(nodeInfo.getParent());
        }
    }
}
