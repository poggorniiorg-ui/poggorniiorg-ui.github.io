package ru.kmz1.zakaz;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.util.Calendar;

/**
 * Напоминание в дни сбора заявок: воскресенье, вторник, четверг
 * (доставка пн / ср / пт, приём до 14:00 накануне).
 */
final class Reminder {
    static final String CHANNEL = "zayavka";
    private static final int REQ = 1001;
    private static final int[] DAYS = {Calendar.SUNDAY, Calendar.TUESDAY, Calendar.THURSDAY};

    private Reminder() {}

    private static int flags() {
        return PendingIntent.FLAG_UPDATE_CURRENT
                | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);
    }

    private static PendingIntent alarmIntent(Context c) {
        Intent i = new Intent(c, ReminderReceiver.class).setAction(ReminderReceiver.ACTION_FIRE);
        return PendingIntent.getBroadcast(c, REQ, i, flags());
    }

    /** Ближайший момент напоминания строго после "сейчас". */
    static long next(int hour, int minute) {
        Calendar now = Calendar.getInstance();
        for (int add = 0; add <= 7; add++) {
            Calendar t = (Calendar) now.clone();
            t.add(Calendar.DAY_OF_YEAR, add);
            t.set(Calendar.HOUR_OF_DAY, hour);
            t.set(Calendar.MINUTE, minute);
            t.set(Calendar.SECOND, 0);
            t.set(Calendar.MILLISECOND, 0);
            int dow = t.get(Calendar.DAY_OF_WEEK);
            boolean day = false;
            for (int d : DAYS) if (d == dow) day = true;
            if (day && t.after(now)) return t.getTimeInMillis();
        }
        return now.getTimeInMillis() + 24L * 3600 * 1000;
    }

    /** Поставить (или снять) будильник по текущим настройкам. */
    static void schedule(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = alarmIntent(c);
        am.cancel(pi);
        if (!Prefs.on(c)) return;
        long at = next(Prefs.hour(c), Prefs.minute(c));
        boolean exact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms();
        try {
            if (exact && Build.VERSION.SDK_INT >= 23) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            } else if (exact) {
                am.setExact(AlarmManager.RTC_WAKEUP, at, pi);
            } else {
                am.setWindow(AlarmManager.RTC_WAKEUP, at, 10 * 60 * 1000L, pi);
            }
        } catch (SecurityException e) {
            am.setWindow(AlarmManager.RTC_WAKEUP, at, 10 * 60 * 1000L, pi);
        }
    }

    /** Текст «следующее напоминание: вт 03.11 в 09:52». */
    static String nextText(Context c) {
        if (!Prefs.on(c)) return "Напоминание выключено";
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("EEEE, d MMMM 'в' HH:mm",
                new java.util.Locale("ru"));
        return "Следующее напоминание: " + f.format(new java.util.Date(next(Prefs.hour(c), Prefs.minute(c))));
    }

    static void ensureChannel(Context c) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm == null || nm.getNotificationChannel(CHANNEL) != null) return;
        NotificationChannel ch = new NotificationChannel(CHANNEL, "Напоминание о заявке",
                NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("В дни сбора заявок: воскресенье, вторник, четверг");
        nm.createNotificationChannel(ch);
    }

    static void show(Context c) {
        ensureChannel(c);
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        Intent open = new Intent(c, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(c, 1, open, flags());

        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(c, CHANNEL)
                : new Notification.Builder(c);
        b.setSmallIcon(R.drawable.ic_notify)
                .setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.drawable.ic_notify_big))
                .setContentTitle("Заявка на завтра")
                .setContentText("Приём заявок до 14:00. Нажмите, чтобы оформить.")
                .setContentIntent(pi)
                .setAutoCancel(true);
        if (Build.VERSION.SDK_INT < 26) b.setPriority(Notification.PRIORITY_HIGH);
        try {
            nm.notify(2001, b.build());
        } catch (SecurityException ignored) {
            // нет разрешения на уведомления — молча пропускаем
        }
    }
}
