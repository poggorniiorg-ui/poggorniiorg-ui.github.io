package ru.kmz1.zakaz;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Locale;

/** Включение напоминания о заявке и выбор времени. */
public class ReminderActivity extends Activity {
    static final String EXTRA_FIRST = "first";
    private static final int REQ_NOTIF = 7;
    private static final int REQ_TEST = 8;

    private boolean first;
    private int hour;
    private int minute;
    private Switch sw;
    private Button timeBtn;
    private android.widget.TextView nextTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        first = getIntent() != null && getIntent().getBooleanExtra(EXTRA_FIRST, false);
        hour = Prefs.hour(this);
        minute = Prefs.minute(this);

        LinearLayout box = Ui.screen(this);
        Ui.logo(this, box);
        Ui.title(this, box, "Напоминание о заявке");
        Ui.text(this, box, "В дни сбора заявок — воскресенье, вторник и четверг — пришлём "
                + "уведомление: «Заявка на завтра — приём до 14:00».");

        sw = new Switch(this);
        sw.setText("Напоминать");
        sw.setTextSize(18);
        sw.setTextColor(Ui.INK);
        sw.setChecked(first || Prefs.on(this));
        int pad = Ui.dp(this, 8);
        sw.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams lp = Ui.wide(this);
        lp.bottomMargin = Ui.dp(this, 12);
        box.addView(sw, lp);

        timeBtn = Ui.button(this, box, "", false);
        updateTime();
        timeBtn.setOnClickListener(v -> new TimePickerDialog(this, (tp, h, m) -> {
            hour = h;
            minute = m;
            updateTime();
        }, hour, minute, true).show());
        sw.setOnCheckedChangeListener((b, on) -> timeBtn.setEnabled(on));
        timeBtn.setEnabled(sw.isChecked());

        Ui.button(this, box, first ? "Включить и продолжить" : "Сохранить", true)
                .setOnClickListener(v -> save());
        nextTv = Ui.text(this, box, Reminder.nextText(this));

        Ui.button(this, box, "Проверить уведомление сейчас", false).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 33
                    && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_TEST);
                return;
            }
            Reminder.show(this);
            Toast.makeText(this, "Уведомление отправлено — проверьте шторку", Toast.LENGTH_SHORT).show();
        });
        Ui.text(this, box, "Не приходит? На Xiaomi, Redmi, POCO, Huawei, Honor: в настройках приложения "
                + "включите «Автозапуск» и «Уведомления», а в «Контроль активности» / «Батарея» "
                + "выберите «Нет ограничений».");
        Ui.button(this, box, "Открыть настройки приложения", false).setOnClickListener(v -> {
            Intent s = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivity(s);
        });
        if (first) {
            Ui.button(this, box, "Не сейчас", false).setOnClickListener(v -> {
                Prefs.save(this, false, hour, minute);
                Reminder.schedule(this);
                next();
            });
            Ui.text(this, box, "Включить позже: долгое нажатие на значок приложения → «Напоминание».");
        }
    }

    private void updateTime() {
        timeBtn.setText(String.format(Locale.US, "Время: %02d:%02d", hour, minute));
    }

    private void save() {
        boolean on = sw.isChecked();
        if (on && Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
            return;
        }
        apply(on);
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        if (code == REQ_TEST) {
            if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) Reminder.show(this);
            else Toast.makeText(this, "Уведомления запрещены в настройках", Toast.LENGTH_LONG).show();
            return;
        }
        if (code != REQ_NOTIF) return;
        boolean granted = res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED;
        if (!granted) {
            Toast.makeText(this, "Без разрешения на уведомления напоминание не придёт", Toast.LENGTH_LONG).show();
        }
        apply(granted);
    }

    private void apply(boolean on) {
        Prefs.save(this, on, hour, minute);
        Reminder.ensureChannel(this);
        Reminder.schedule(this);
        if (on) {
            Toast.makeText(this, String.format(Locale.US,
                    "Напоминание включено: вс, вт, чт в %02d:%02d", hour, minute), Toast.LENGTH_LONG).show();
        }
        if (nextTv != null) nextTv.setText(Reminder.nextText(this));
        if (first) next();
    }

    private void next() {
        if (first) {
            Intent i = new Intent(this, MainActivity.class).putExtra(MainActivity.EXTRA_SKIP_ASK, true);
            Uri data = getIntent() != null ? getIntent().getData() : null;
            if (data != null) i.setData(data);
            startActivity(i);
        }
        finish();
    }
}
