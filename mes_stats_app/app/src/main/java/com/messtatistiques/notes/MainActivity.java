package com.messtatistiques.notes;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class MainActivity extends Activity {

    private WebView navigateur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navigateur = new WebView(this);

        WebSettings parametres = navigateur.getSettings();
        parametres.setJavaScriptEnabled(true);
        parametres.setDomStorageEnabled(true);

        setContentView(navigateur);

        navigateur.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (navigateur.canGoBack()) {
            navigateur.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
