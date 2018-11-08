package ekoolab.com.show.fragments.subhomes;

import android.graphics.Bitmap;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import ekoolab.com.show.R;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.utils.ViewHolder;

public class WebFragment extends BaseFragment {
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_web;
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        progressBar = holder.get(R.id.progress_bar);
        webView = holder.get(R.id.web_view);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url){
                progressBar.setVisibility(View.GONE);
            }

        });
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                if(progress == 100){
                    progressBar.setVisibility(View.GONE);
                }

            }
        });
        webView.loadUrl("http://www.zscsg.com/");
    }
}
