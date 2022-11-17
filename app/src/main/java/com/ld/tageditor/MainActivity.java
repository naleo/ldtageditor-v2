package com.ld.tageditor;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    MainActivity activity = this;
    private IntentFilter[] mFilters;
    private PendingIntent mPendingIntent;
    private String[][] mTechLists;
    NfcAdapter nfc;
    public Tag tag;
    WebView webView;

    /* Access modifiers changed, original: protected */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(536870912), 0);
        try {
            new IntentFilter("android.nfc.action.NDEF_DISCOVERED").addDataType("*/*");
            this.mFilters = new IntentFilter[]{new IntentFilter("android.nfc.action.NDEF_DISCOVERED")};
            String[][] strArr = new String[1][];
            strArr[0] = new String[]{NfcA.class.getName()};
            this.mTechLists = strArr;
            this.nfc = NfcAdapter.getDefaultAdapter(this);
            this.webView = (WebView) findViewById(R.id.webView);
            this.webView.getSettings().setJavaScriptEnabled(true);
            this.webView.getSettings().setAllowFileAccess(true);
            this.webView.getSettings().setAllowFileAccessFromFileURLs(true);
            this.webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
            this.webView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    MainActivity.this.activity.setProgress(progress * 100);
                }
            });
            this.webView.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    String host = Uri.parse(url).getHost();
                    ArrayList<String> allowedHosts = new ArrayList();
                    allowedHosts.add("android_asset");
                    allowedHosts.add("127.0.0.1");
                    ArrayList<String> allowedSubstring = new ArrayList();
                    allowedSubstring.add("192.168.");
                    if (allowedHosts.contains(host)) {
                        return false;
                    }
                    Iterator i$ = allowedSubstring.iterator();
                    while (i$.hasNext()) {
                        if (host.startsWith((String) i$.next())) {
                            return false;
                        }
                    }
                    view.evaluateJavascript("(function(){ var err = 'ACCESS TO URL " + url + " DENIED'; console.error(err); if(window.appErrorHandler) window.appErrorHandler(err); })();", new ValueCallback<String>() {
                        public void onReceiveValue(String s) {
                        }
                    });
                    return true;
                }
            });
            WebView.setWebContentsDebuggingEnabled(true);
            this.webView.addJavascriptInterface(new JSAPI(this, this.webView), "AndroidApp");
            this.webView.loadUrl("file:///android_asset/index.html");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.nfc != null) {
            this.nfc.enableForegroundDispatch(this, this.mPendingIntent, this.mFilters, this.mTechLists);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.nfc != null) {
            this.nfc.disableForegroundDispatch(this);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.tag = (Tag) intent.getParcelableExtra("android.nfc.extra.TAG");
        Log.i("Foreground dispatch", "Discovered tag");
        callJavaScript("AndroidApp.tagDetected", new Object[0]);
    }

    private void callJavaScript(String methodName, Object... params) {
        Log.i(TAG, "CallJS Building string...");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("javascript:try{(window.");
        stringBuilder.append(methodName);
        stringBuilder.append("||console.warn.bind(console,'UNHANDLED','");
        stringBuilder.append(methodName);
        stringBuilder.append("'))(");
        for (Object param : params) {
            String param2 = "";
            if (!(param instanceof String)) {
                param2 = param.toString();
            }
            stringBuilder.append("'");
            stringBuilder.append(param2);
            stringBuilder.append("'");
            stringBuilder.append(",");
        }
        stringBuilder.append("''");
        stringBuilder.append(")}catch(error){console.error('ANDROID APP ERROR',error);}");
        this.webView.loadUrl(stringBuilder.toString());
        Log.i(TAG, "CallJS" + stringBuilder.toString());
    }
}
