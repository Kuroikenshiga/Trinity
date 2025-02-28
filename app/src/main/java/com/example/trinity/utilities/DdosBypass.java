package com.example.trinity.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebViewCompat;

import com.example.trinity.Interfeces.Extensions;

public abstract class DdosBypass {
    @Deprecated
    public static void bypass(Context context, Handler handler, String url){
        WebView webView = new WebView(context);
        final int[] countRequestCalls = {0};
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                countRequestCalls[0]++;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(countRequestCalls[0] == 2){
                    Message message = Message.obtain();
                    message.what = Extensions.RESPONSE_REQUEST_NEW_CONTENT_CALL;
                    handler.sendMessage(message);
                    webView.destroy();

                }
            }
        });
        webView.loadUrl(url);
    }
}
