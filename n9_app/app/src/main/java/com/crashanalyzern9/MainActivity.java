package com.crashanalyzern9;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private WebView navigateur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navigateur = new WebView(this);

        WebSettings parametres = navigateur.getSettings();
        parametres.setJavaScriptEnabled(true);
        parametres.setDomStorageEnabled(true);

        navigateur.addJavascriptInterface(
            new ControleInternet(this),
            "AndroidInternet"
        );

        setContentView(navigateur);
        navigateur.loadUrl("file:///android_asset/index.html");
    }

    public class ControleInternet {

        private Context contexte;

        ControleInternet(Context contexte) {
            this.contexte = contexte;
        }

        @JavascriptInterface
        public boolean disponible() {
            ConnectivityManager gestionnaire =
                (ConnectivityManager) contexte.getSystemService(
                    Context.CONNECTIVITY_SERVICE
                );

            if (gestionnaire == null) return false;

            Network reseau = gestionnaire.getActiveNetwork();
            if (reseau == null) return false;

            NetworkCapabilities capacites =
                gestionnaire.getNetworkCapabilities(reseau);

            return capacites != null &&
                capacites.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) &&
                capacites.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                );
        }
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
