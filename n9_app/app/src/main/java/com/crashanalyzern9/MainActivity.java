package com.crashanalyzern9;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.ValueCallback;
import android.widget.Toast;
import java.io.OutputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    private WebView navigateur;
    private String sauvegardeEnAttente;
    private ValueCallback<Uri[]> selectionFichier;

    private static final int DEMANDE_SAUVEGARDE = 1001;
    private static final int DEMANDE_RESTAURATION = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navigateur = new WebView(this);

        WebSettings parametres = navigateur.getSettings();
        parametres.setJavaScriptEnabled(true);
        parametres.setDomStorageEnabled(true);

        navigateur.addJavascriptInterface(
            new SauvegardeAndroid(),
            "AndroidBackup"
        );

        navigateur.addJavascriptInterface(
            new ControleInternet(this),
            "AndroidInternet"
        );

        navigateur.setWebViewClient(new WebViewClient());

        navigateur.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                WebView vue,
                ValueCallback<Uri[]> callback,
                FileChooserParams params
            ) {
                if (selectionFichier != null) {
                    selectionFichier.onReceiveValue(null);
                }

                selectionFichier = callback;

                Intent choix = new Intent(Intent.ACTION_GET_CONTENT);
                choix.addCategory(Intent.CATEGORY_OPENABLE);
                choix.setType("application/json");

                startActivityForResult(
                    Intent.createChooser(choix, "Choisir une sauvegarde"),
                    DEMANDE_RESTAURATION
                );

                return true;
            }
        });

        setContentView(navigateur);
        navigateur.loadUrl("file:///android_asset/index.html");
    }

    public class SauvegardeAndroid {

        @JavascriptInterface
        public void enregistrer(String contenu) {
            runOnUiThread(() -> {
                sauvegardeEnAttente = contenu;

                Intent choix = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                choix.addCategory(Intent.CATEGORY_OPENABLE);
                choix.setType("application/json");
                choix.putExtra(
                    Intent.EXTRA_TITLE,
                    "crash-analyzer-n9-sauvegarde.json"
                );

                startActivityForResult(choix, DEMANDE_SAUVEGARDE);
            });
        }
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
    protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DEMANDE_SAUVEGARDE) {
            if (resultCode == RESULT_OK && data != null &&
                data.getData() != null && sauvegardeEnAttente != null) {

                try {
                    Uri destination = data.getData();
                    OutputStream sortie =
                        getContentResolver().openOutputStream(destination);

                    if (sortie == null) {
                        throw new IOException("Ouverture impossible");
                    }

                    sortie.write(
                        sauvegardeEnAttente.getBytes("UTF-8")
                    );
                    sortie.close();

                    Toast.makeText(
                        this,
                        "Sauvegarde enregistrée.",
                        Toast.LENGTH_LONG
                    ).show();

                } catch (Exception e) {
                    Toast.makeText(
                        this,
                        "Erreur pendant l'enregistrement.",
                        Toast.LENGTH_LONG
                    ).show();
                }
            }

            sauvegardeEnAttente = null;
        }

        if (requestCode == DEMANDE_RESTAURATION) {
            if (selectionFichier != null) {
                Uri[] result = null;

                if (resultCode == RESULT_OK &&
                    data != null && data.getData() != null) {
                    result = new Uri[]{data.getData()};
                }

                selectionFichier.onReceiveValue(result);
                selectionFichier = null;
            }
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
