package com.codeboy.qianghongbao;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * <p>Created 16/2/6 下午2:05.</p>
 * <p><a href="mailto:730395591@qq.com">Email:730395591@qq.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 *
 * ActionBar栏的“关于CodeBoy抢红包”在哪加载的？
 */
public class AboutMeActivity extends BaseActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        mWebView = (WebView) findViewById(R.id.webview);

        //下面几个Settings好像不写也行
        WebSettings settings = mWebView.getSettings();
        settings.setUseWideViewPort(true);    // it loads the WebView with the attributes defined in the meta tag of the webpage. So it scales the webpage as defined in the html.
        settings.setLoadWithOverviewMode(true);    //使WebView显示的界面显示完全缩小。loads the WebView completely zoomed out
		/*设置webview加载的页面的模式,这方法可以让你的页面适应手机屏幕的分辨率，完整的显示在屏幕上，可以放大缩小。*/
//        settings.setBuiltInZoomControls(true);//设置使支持缩放(不过好像不好使)

        /*不写这个的话，就会提示你要不要在浏览器中打开，所以这句还是必需的*/
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.startsWith("http")) {
                    //使用当前WebView处理跳转
                    view.loadUrl(url);
                    return true;//true表示此事件已在这里处理完成，不需要再向外扩展
                }
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return false;
            }
        });

        mWebView.loadUrl(getString(R.string.about_url));

		/*修改ActionBar，在左上角添加一个表示返回的左箭头“<”符号*/
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
			//注意：但仅仅这样，点击的时候是不会有任何效果的
			//要想实现点击之后的效果，1.可通过manifest文件实现。2.重写onSupportNavigateUp()方法，实现Fragment的向上导航
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override   //重写onSupportNavigateUp()方法，实现Fragment的向上导航
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     *  2016-2-29加入新特性：返回键按下之后，在WebView中回退到上一个网页(既然可以点左上角来结束掉Activity，
     *  那么WebView里面点返回键应该要不一样)
     * @param keyCode
     * @param event
     * @return
     */
    @Override   //默认点回退键，会退出Activity，需监听按键操作，使回退在WebView内发生
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
