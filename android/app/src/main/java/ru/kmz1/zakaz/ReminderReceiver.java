package ru.kmz1.zakaz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Срабатывание напоминания и восстановление после перезагрузки телефона. */
public class ReminderReceiver extends BroadcastReceiver {
    static final String ACTION_FIRE = "ru.kmz1.zakaz.REMINDER";

    @Override
    public void onReceive(Context c, Intent intent) {
        String a = intent != null ? intent.getAction() : null;
        if (ACTION_FIRE.equals(a) && Prefs.on(c)) {
            Reminder.show(c);
        }
        // в любом случае ставим следующее (перезагрузка, смена времени, обновление приложения)
        Reminder.schedule(c);
    }
}
