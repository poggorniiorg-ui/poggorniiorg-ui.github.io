package ru.kmz1.zakaz;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

/** Экран «Нет связи» вместо ошибки браузера. */
public class OfflineActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout box = Ui.screen(this);
        Ui.logo(this, box);
        Ui.title(this, box, "Нет интернета");
        Ui.text(this, box, "Черновик заявки сохранён в телефоне и никуда не пропадёт.\n"
                + "Включите мобильный интернет или Wi‑Fi и нажмите «Повторить».");

        Ui.button(this, box, "Повторить", true).setOnClickListener(v -> {
            if (MainActivity.online(this)) {
                Uri data = getIntent() != null ? getIntent().getData() : null;
                MainActivity.openSite(this, data);
                finish();
            } else {
                Toast.makeText(this, "Связи пока нет", Toast.LENGTH_SHORT).show();
            }
        });
        Ui.button(this, box, "Позвонить торговому представителю", false).setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Prefs.PHONE))));
        Ui.text(this, box, Prefs.PHONE.replaceFirst("\\+7(\\d{3})(\\d{3})(\\d{2})(\\d{2})", "+7 $1 $2-$3-$4"));
    }
}
