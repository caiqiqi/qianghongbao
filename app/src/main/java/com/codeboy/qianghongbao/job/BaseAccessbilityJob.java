package com.codeboy.qianghongbao.job;

import android.content.Context;

import com.codeboy.qianghongbao.Config;
import com.codeboy.qianghongbao.QiangHongBaoService;

/**
 * <p>Created 16/1/16 上午12:38.</p>
 * <p><a href="mailto:codeboy2013@gmail.com">Email:codeboy2013@gmail.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
public abstract class BaseAccessbilityJob implements AccessbilityJob {

    /*持有的QIangHongbaoService对象*/
	private QiangHongBaoService service;

	/*就是将Service对象传过来*/
    @Override
    public void onCreateJob(QiangHongBaoService service) {
        this.service = service;
    }

    public Context getApplicationContext() {
        return service.getApplicationContext();
    }

    public Config getConfig() {
        return service.getConfig();
    }

    public QiangHongBaoService getQiangHongBaoService() {
        return service;
    }
}
