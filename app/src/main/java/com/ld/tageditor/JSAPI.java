package com.ld.tageditor;

import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import java.io.IOException;

public class JSAPI {
    MainActivity activity;
    WebView web;

    public JSAPI(MainActivity paramActivity, WebView paramWeb) {
        this.activity = paramActivity;
        this.web = paramWeb;
    }

    @JavascriptInterface
    public String readTag(byte page) {
//        MifareUltralight mifare = MifareUltralight.get(this.activity.tag);
        NfcA nfcA = NfcA.get(this.activity.tag);
        try {
            Log.i("JSAPI", "Connecting");
            nfcA.connect();
//            mifare.connect();
            Log.i("JSAPI", "Connected");
            Log.i("JSAPI", "Read");

            byte[] message = new byte[] {
                    0x30,
                    (byte)(page & 0xFF)
            };

//            byte[] payload = mifare.readPages(page);
            byte[] payload = nfcA.transceive(message);
//            Log.i("JSAPI", String.format("Payload %02X%02X%02X%02X %02X%02X%02X%02X %02X%02X%02X%02X %02X%02X%02X%02X", new Object[]{Byte.valueOf(payload[0]), Byte.valueOf(payload[1]), Byte.valueOf(payload[2]), Byte.valueOf(payload[3]), Byte.valueOf(payload[4]), Byte.valueOf(payload[5]), Byte.valueOf(payload[6]), Byte.valueOf(payload[7]), Byte.valueOf(payload[8]), Byte.valueOf(payload[9]), Byte.valueOf(payload[10]), Byte.valueOf(payload[11]), Byte.valueOf(payload[12]), Byte.valueOf(payload[13]), Byte.valueOf(payload[14]), Byte.valueOf(mifare.readPages(page)[15])}));
            String encodeToString = Base64.encodeToString(payload, 0, 16, 0);
            Log.i("JSAPI", encodeToString);
            return encodeToString;
        } catch (IOException e) {
            Log.e("JSAPI", "IOException while writing MifareUltralight message...", e);
            Log.e("JSAPI", e.getMessage());
            if (nfcA != null) {
                return null;
            }
        } finally {
            if (nfcA!= null) {
                try {
                    nfcA.close();
                } catch (IOException e22) {
                    Log.e("JSAPI", "Error closing tag...", e22);
                }
            }
        }
        return null;
    }

    @JavascriptInterface
    public boolean writeTag(byte page, String payload) {
        byte[] data = Base64.decode(payload, 0);
        NfcA nfca = NfcA.get(this.activity.tag);
//        MifareUltralight ultralight = MifareUltralight.get(this.activity.tag);
        try {
            Log.i("JSAPI", "Connecting");
            nfca.connect();
//            ultralight.connect();
            Log.i("JSAPI", "Connected");
            Log.i("JSAPI", String.format("Writing %02X%02X%02X%02X", new Object[]{Byte.valueOf(data[0]), Byte.valueOf(data[1]), Byte.valueOf(data[2]), Byte.valueOf(data[3])}));
            byte[] message = new byte[] {
                    (byte) 0xA2,
                    (byte)(page & 0xFF),
                    data[0], data[1], data[2], data[3]
            };
            byte[] result = nfca.transceive(message);
            Log.i("JSAPI", "Writing Done");
            try {
                Log.i("JSAPI", "Closing");
                nfca.close();
                Log.i("JSAPI", "Closed");
                return true;
            } catch (IOException e) {
                Log.e("JSAPI", "IOException while closing MifareUltralight...", e);
                return false;
            }
        } catch (IOException e2) {
            Log.e("JSAPI", "IOException while closing MifareUltralight...", e2);
            try {
                Log.i("JSAPI", "Closing");
                nfca.close();
                Log.i("JSAPI", "Closed");
                return true;
            } catch (IOException e22) {
                Log.e("JSAPI", "IOException while closing MifareUltralight...", e22);
                return false;
            }
        } catch (Throwable th) {
            try {
                Log.i("JSAPI", "Closing");
                nfca.close();
                Log.i("JSAPI", "Closed");
                return true;
            } catch (IOException e222) {
                Log.e("JSAPI", "IOException while closing MifareUltralight...", e222);
                return false;
            }
        }
    }

    private void callJavaScript(String methodName, Object... params) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("javascript:try{(window.");
        stringBuilder.append(methodName);
        stringBuilder.append("||console.warn.bind(console,'UNHANDLED','");
        stringBuilder.append(methodName);
        stringBuilder.append("'))(");
        for (Object param : params) {
            Object param2 = "";
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
        this.web.loadUrl(stringBuilder.toString());
        Log.i("CallJS", stringBuilder.toString());
    }
}
