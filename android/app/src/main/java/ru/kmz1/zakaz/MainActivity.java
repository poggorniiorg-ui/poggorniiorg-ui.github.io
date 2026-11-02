package ru.kmz1.zakaz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.androidbrowserhelper.trusted.LauncherActivity;

/**
 * Точка входа: ярлыки с иконки, первое предложение напоминания,
 * экран «Нет связи», затем сам сайт заказов (Trusted Web Activity).
 */
public class MainActivity extends Activity {
    static final String EXTRA_SKIP_ASK = "skip_ask";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Reminder.schedule(this);

        Intent in = getIntent();
        Uri data = in != null ? in.getData() : null;

        if (data != null && "kmz1".equals(data.getScheme())) {
            String host = data.getHost();
            if ("call".equals(host)) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Prefs.PHONE)));
                finish();
                return;
            }
            if ("reminder".equals(host)) {
                startActivity(new Intent(this, ReminderActivity.class));
                finish();
                return;
            }
            data = null;
        }

        boolean skipAsk = in != null && in.getBooleanExtra(EXTRA_SKIP_ASK, false);
        if (!skipAsk && !Prefs.asked(this)) {
            Intent ask = new Intent(this, ReminderActivity.class)
                    .putExtra(ReminderActivity.EXTRA_FIRST, true);
            if (data != null) ask.setData(data);
            startActivity(ask);
            finish();
            return;
        }

        if (!online(this)) {
            Intent off = new Intent(this, OfflineActivity.class);
            if (data != null) off.setData(data);
            startActivity(off);
            finish();
            return;
        }

        openSite(this, data);
        finish();
    }

    static void openSite(Activity a, Uri data) {
        Uri url = data != null ? data : Uri.parse(Prefs.SITE);
        Intent twa = new Intent(a, LauncherActivity.class)
                .setAction(Intent.ACTION_VIEW)
                .setData(url);
        a.startActivity(twa);
        a.overridePendingTransition(0, 0);
    }

    @SuppressWarnings("deprecation")
    static boolean online(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return true;
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                Network n = cm.getActiveNetwork();
                if (n == null) return false;
                NetworkCapabilities nc = cm.getNetworkCapabilities(n);
                return nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        } catch (SecurityException e) {
            return true;
        }
    }
}
