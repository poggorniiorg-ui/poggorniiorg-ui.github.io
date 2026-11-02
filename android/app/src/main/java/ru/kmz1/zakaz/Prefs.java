package ru.kmz1.zakaz;

import android.content.Context;
import android.content.SharedPreferences;

/** Настройки напоминания о заявке. */
final class Prefs {
    static final String SITE = "https://poggorniiorg-ui.github.io/kmz-zakaz/";
    static final String PHONE = "+79676505089";

    private static final String FILE = "kmz_prefs";
    private static final String K_ASKED = "reminder_asked";
    private static final String K_ON = "reminder_on";
    private static final String K_HOUR = "reminder_hour";
    private static final String K_MIN = "reminder_min";

    private Prefs() {}

    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    static boolean asked(Context c) { return p(c).getBoolean(K_ASKED, false); }
    static void setAsked(Context c) { p(c).edit().putBoolean(K_ASKED, true).apply(); }

    static boolean on(Context c) { return p(c).getBoolean(K_ON, false); }
    static int hour(Context c) { return p(c).getInt(K_HOUR, 12); }
    static int minute(Context c) { return p(c).getInt(K_MIN, 0); }

    static void save(Context c, boolean on, int hour, int minute) {
        p(c).edit().putBoolean(K_ON, on).putInt(K_HOUR, hour).putInt(K_MIN, minute)
                .putBoolean(K_ASKED, true).apply();
    }
}
