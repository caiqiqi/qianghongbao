package com.codeboy.qianghongbao.job;

import android.content.Context;

import com.codeboy.qianghongbao.Config;
import com.codeboy.qianghongbao.QHBAccessibilityService;

/**
 * <p>Created 16/1/16 上午12:38.</p>
 * <p><a href="mailto:codeboy2013@gmail.com">Email:codeboy2013@gmail.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
public abstract class BaseAccessbilityJob implements AccessbilityJob {

    /*持有的QIangHongbaoService对象*/
	private QHBAccessibilityService service;

	/*就是将Service对象传过来*/
    @Override
    public void onCreateJob(QHBAccessibilityService service) {
        this.service = service;
    }

    public Context getApplicationContext() {
        return service.getApplicationContext();
    }

    public Config getConfig() {
        return service.getConfig();
    }

    public QHBAccessibilityService getQHBAccessibilityService() {
        return service;
    }
}
