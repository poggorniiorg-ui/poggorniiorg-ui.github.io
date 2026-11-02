package ru.kmz1.zakaz;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Простые экраны в стиле сайта заказов. */
final class Ui {
    static final int BG = Color.parseColor("#F3F5F8");
    static final int INK = Color.parseColor("#0C1726");
    static final int INK2 = Color.parseColor("#4A5669");
    static final int ACC = Color.parseColor("#2350E6");

    private Ui() {}

    static int dp(Activity a, float v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                a.getResources().getDisplayMetrics());
    }

    static LinearLayout screen(Activity a) {
        if (Build.VERSION.SDK_INT >= 21) {
            a.getWindow().setStatusBarColor(BG);
            a.getWindow().setNavigationBarColor(BG);
            if (Build.VERSION.SDK_INT >= 23) {
                a.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        ScrollView sv = new ScrollView(a);
        sv.setBackgroundColor(BG);
        sv.setFillViewport(true);
        LinearLayout box = new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        int p = dp(a, 24);
        box.setPadding(p, dp(a, 48), p, p);
        sv.addView(box, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        a.setContentView(sv);
        return box;
    }

    static void logo(Activity a, LinearLayout box) {
        ImageView iv = new ImageView(a);
        iv.setImageResource(R.drawable.splash);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(a, 96), dp(a, 96));
        lp.bottomMargin = dp(a, 24);
        box.addView(iv, lp);
    }

    static TextView title(Activity a, LinearLayout box, String s) {
        TextView t = new TextView(a);
        t.setText(s);
        t.setTextColor(INK);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = wide(a);
        lp.bottomMargin = dp(a, 10);
        box.addView(t, lp);
        return t;
    }

    static TextView text(Activity a, LinearLayout box, String s) {
        TextView t = new TextView(a);
        t.setText(s);
        t.setTextColor(INK2);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        t.setGravity(Gravity.CENTER);
        t.setLineSpacing(0, 1.2f);
        LinearLayout.LayoutParams lp = wide(a);
        lp.bottomMargin = dp(a, 20);
        box.addView(t, lp);
        return t;
    }

    static Button button(Activity a, LinearLayout box, String s, boolean primary) {
        Button b = new Button(a);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(primary ? Color.WHITE : INK);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(a, 16));
        bg.setColor(primary ? ACC : Color.WHITE);
        if (!primary) bg.setStroke(dp(a, 1), Color.parseColor("#D3DAE5"));
        b.setBackground(bg);
        if (Build.VERSION.SDK_INT >= 21) b.setStateListAnimator(null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(a, 54));
        lp.bottomMargin = dp(a, 10);
        box.addView(b, lp);
        return b;
    }

    static LinearLayout.LayoutParams wide(Activity a) {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
