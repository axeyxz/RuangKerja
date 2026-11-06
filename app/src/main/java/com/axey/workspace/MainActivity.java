package com.axey.workspace;

import android.app.Activity;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.net.VpnService;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ImageView;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class MainActivity extends Activity {
    private static final int VPN_REQUEST_CODE = 7401;
    private int BG = Color.rgb(8, 8, 10);
    private int PANEL = Color.rgb(20, 20, 23);
    private int PANEL_2 = Color.rgb(32, 32, 36);
    private int BLUE = Color.rgb(45, 125, 245);
    private int MUTED = Color.rgb(174, 174, 182);
    private int TEXT = Color.WHITE;
    private boolean lightMode = false;
    private DevicePolicyManager dpm;
    private LinearLayout root;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        lightMode = getPreferences(MODE_PRIVATE).getBoolean("light_mode", false);
        applyTheme();
        render();
    }

    @Override protected void onResume() {
        super.onResume();
        if (dpm != null) { applyTheme(); render(); }
    }

    private void applyTheme() {
        if (lightMode) {
            BG = Color.rgb(248, 249, 251);
            PANEL = Color.WHITE;
            PANEL_2 = Color.rgb(239, 241, 245);
            BLUE = Color.rgb(35, 105, 220);
            MUTED = Color.rgb(95, 100, 110);
            TEXT = Color.rgb(20, 22, 27);
        } else {
            BG = Color.rgb(8, 8, 10);
            PANEL = Color.rgb(20, 20, 23);
            PANEL_2 = Color.rgb(32, 32, 36);
            BLUE = Color.rgb(45, 125, 245);
            MUTED = Color.rgb(174, 174, 182);
            TEXT = Color.WHITE;
        }
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        if (Build.VERSION.SDK_INT >= 23) {
            int flags = lightMode ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0;
            if (lightMode) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void toggleTheme() {
        lightMode = !lightMode;
        getPreferences(MODE_PRIVATE).edit().putBoolean("light_mode", lightMode).apply();
        applyTheme();
        render();
    }

    private GradientDrawable shape(int color, int radius, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        if (stroke != 0) d.setStroke(dp(1), stroke);
        return d;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void space(LinearLayout parent, int height) {
        View v = new View(this);
        parent.addView(v, new LinearLayout.LayoutParams(1, dp(height)));
    }

    private void render() {
        boolean owner = dpm != null && dpm.isProfileOwnerApp(getPackageName());
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(22), dp(22), dp(30));
        scroll.addView(root);

        LinearLayout brand = new LinearLayout(this);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        ImageView mark = new ImageView(this);
        mark.setImageResource(R.drawable.axey_avatar);
        mark.setScaleType(ImageView.ScaleType.CENTER_CROP);
        GradientDrawable avatarClip = new GradientDrawable();
        avatarClip.setShape(GradientDrawable.OVAL);
        avatarClip.setColor(Color.TRANSPARENT);
        mark.setBackground(avatarClip);
        mark.setClipToOutline(true);
        mark.setElevation(dp(5));
        brand.addView(mark, new LinearLayout.LayoutParams(dp(48), dp(48)));
        ObjectAnimator logoPulse = ObjectAnimator.ofFloat(mark, View.SCALE_X, 1f, 1.045f, 1f);
        logoPulse.setDuration(2400);
        logoPulse.setRepeatCount(ObjectAnimator.INFINITE);
        logoPulse.setInterpolator(new AccelerateDecelerateInterpolator());
        ObjectAnimator logoPulseY = ObjectAnimator.ofFloat(mark, View.SCALE_Y, 1f, 1.045f, 1f);
        logoPulseY.setDuration(2400);
        logoPulseY.setRepeatCount(ObjectAnimator.INFINITE);
        logoPulseY.setInterpolator(new AccelerateDecelerateInterpolator());
        mark.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View view) { logoPulse.start(); logoPulseY.start(); }
            @Override public void onViewDetachedFromWindow(View view) { logoPulse.cancel(); logoPulseY.cancel(); }
        });
        LinearLayout brandText = new LinearLayout(this);
        brandText.setOrientation(LinearLayout.VERTICAL);
        brandText.setPadding(dp(12), 0, 0, 0);
        brandText.addView(text("AXEY", 14, TEXT, true));
        brandText.addView(text("WORKSPACE", 11, MUTED, true));
        brand.addView(brandText, new LinearLayout.LayoutParams(0, -2, 1));
        ThemeToggleView themeToggle = new ThemeToggleView();
        themeToggle.setContentDescription(lightMode ? "Aktifkan mode gelap" : "Aktifkan mode terang");
        themeToggle.setBackground(shape(PANEL_2, 30, 0));
        themeToggle.setOnClickListener(v -> toggleTheme());
        brand.addView(themeToggle, new LinearLayout.LayoutParams(dp(44), dp(44)));
        root.addView(brand);
        space(root, 28);

        root.addView(text("Your work.", 32, TEXT, true));
        root.addView(text("Your space.", 32, TEXT, true));
        space(root, 8);
        root.addView(text("Ruang kerja terpisah, dikelola oleh Android.", 13, MUTED, false));
        space(root, 22);

        LinearLayout status = new LinearLayout(this);
        status.setOrientation(LinearLayout.VERTICAL);
        status.setPadding(dp(18), dp(17), dp(18), dp(17));
        status.setBackground(shape(PANEL, 22, owner ? 0xFF176B55 : 0xFF25486B));
        TextView pill = text(owner ? "●  WORK PROFILE AKTIF" : "●  WORK PROFILE BELUM AKTIF",
            12, owner ? 0xFF4BE0A4 : 0xFFFFC76A, true);
        status.addView(pill);
        space(status, 7);
        status.addView(text(owner
            ? "Axey terdaftar sebagai pengelola profil kerja."
            : "Buat profil kerja melalui proses resmi Android.", 13, MUTED, false));
        root.addView(status);

        space(root, 18);
        Button create = primary(owner ? "WORK PROFILE SUDAH AKTIF" : "+   BUAT WORK PROFILE");
        create.setEnabled(!owner);
        create.setAlpha(owner ? 0.55f : 1f);
        create.setOnClickListener(v -> provision());
        root.addView(create, fullWidth());
        Button refresh = secondary("↻   PERBARUI STATUS");
        refresh.setOnClickListener(v -> render());
        root.addView(refresh, fullWidth());

        space(root, 25);
        section("WORKSPACE TOOLS");
        addTool("apps", "App manager", "Lihat dan buka aplikasi pada ruang Android ini.",
            "BUKA DAFTAR APLIKASI", v -> showApps());
        addTool("proxy", "Proxy manager", "Simpan konfigurasi proxy SOCKS5 untuk tahap pengalihan trafik VPN.",
            "ATUR PROXY", v -> showProxyManager());
        addTool("settings", "Profile settings", "Perawatan, cache, aplikasi, dan pengelolaan Work Profile.",
            "KELOLA WORKSPACE", v -> showProfileSettingsMenu());
        addTool("diagnostics", "Device diagnostics", "Periksa versi Android dan kesiapan Work Profile.",
            "LIHAT DIAGNOSTIK", v -> deviceInfo(owner));
        addTool("help", "Help & safety", "Ketahui batasan dan cara kerja profil terkelola.",
            "BACA INFORMASI", v -> info("Work Profile adalah fitur Android. Axey tidak membuat mesin virtual dan tidak dapat melewati pembatasan sistem. Ketersediaan pembuatan profil ditentukan oleh perangkat dan kebijakan Android."));
        space(root, 18);
        TextView footer = text("AXEY WORKSPACE  •  BUILT FOR ANDROID", 10, 0xFF657B98, true);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer);
        setContentView(scroll);
    }

    private LinearLayout.LayoutParams fullWidth() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(52));
        p.topMargin = dp(7);
        return p;
    }

    private Button primary(String value) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextColor(Color.WHITE);
        b.setTextSize(13);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(shape(BLUE, 16, 0));
        return b;
    }

    private Button secondary(String value) {
        Button b = primary(value);
        b.setTextColor(TEXT);
        b.setBackground(shape(PANEL_2, 16, 0xFF24466A));
        return b;
    }

    private void section(String value) {
        TextView t = text(value, 11, BLUE, true);
        t.setLetterSpacing(0.12f);
        root.addView(t);
        space(root, 9);
    }

    private void addTool(String icon, String title, String desc, String action, View.OnClickListener click) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(13), dp(15), dp(13));
        card.setBackground(shape(PANEL, 19, 0));
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        ToolIconView badge = new ToolIconView(icon);
        badge.setBackgroundColor(Color.TRANSPARENT);
        top.addView(badge, new LinearLayout.LayoutParams(dp(42), dp(42)));
        ObjectAnimator motion;
        ObjectAnimator motionY = null;
        if ("settings".equals(icon)) {
            // Keep the tile steady; rotate only the colored gear drawn inside it.
            motion = ObjectAnimator.ofFloat(badge, View.ALPHA, 1f, 0.94f, 1f);
            motion.setDuration(2400);
            motion.setInterpolator(new AccelerateDecelerateInterpolator());
        } else if ("proxy".equals(icon)) {
            // The arrows animate independently inside the icon, following their own directions.
            motion = ObjectAnimator.ofFloat(badge, View.ALPHA, 1f, 0.96f, 1f);
            motion.setDuration(2400);
            motion.setInterpolator(new AccelerateDecelerateInterpolator());
        } else if ("diagnostics".equals(icon)) {
            motion = ObjectAnimator.ofFloat(badge, View.SCALE_X, 1f, 1.09f, 1f);
            motionY = ObjectAnimator.ofFloat(badge, View.SCALE_Y, 1f, 1.09f, 1f);
            motion.setDuration(2400);
            motionY.setDuration(2400);
            motion.setInterpolator(new AccelerateDecelerateInterpolator());
            motionY.setInterpolator(new AccelerateDecelerateInterpolator());
        } else if ("apps".equals(icon)) {
            motion = ObjectAnimator.ofFloat(badge, View.ALPHA, 0.88f, 1f, 0.88f);
            motion.setDuration(2400);
            motion.setInterpolator(new AccelerateDecelerateInterpolator());
        } else {
            // Help icon draws its question mark stroke-by-stroke in its own 2.4s loop.
            motion = ObjectAnimator.ofFloat(badge, View.ALPHA, 1f, 1f);
            motion.setDuration(2400);
            motion.setInterpolator(new AccelerateDecelerateInterpolator());
        }
        motion.setRepeatCount(ObjectAnimator.INFINITE);
        if (motionY != null) motionY.setRepeatCount(ObjectAnimator.INFINITE);
        motion.setStartDelay(Math.abs(title.hashCode() % 400));
        final ObjectAnimator extraMotion = motionY;
        badge.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View view) {
                motion.start();
                if (extraMotion != null) extraMotion.start();
            }
            @Override public void onViewDetachedFromWindow(View view) {
                motion.cancel();
                if (extraMotion != null) extraMotion.cancel();
                badge.setRotation(0f);
                badge.setScaleX(1f);
                badge.setScaleY(1f);
                badge.setTranslationX(0f);
                badge.setAlpha(1f);
            }
        });
        TextView heading = text(title, 16, TEXT, true);
        heading.setPadding(dp(12), 0, 0, 0);
        top.addView(heading);
        card.addView(top);
        space(card, 7);
        card.addView(text(desc, 12, MUTED, false));
        space(card, 9);
        Button actionButton = secondary(action);
        actionButton.setOnClickListener(click);
        card.addView(actionButton, new LinearLayout.LayoutParams(-1, dp(44)));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(11);
        root.addView(card, p);
    }

    // Crisp vector theme control based on the Lucide Sun and Moon icon geometry.
    private class ThemeToggleView extends View {
        private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path iconPath = new Path();
        ThemeToggleView() { super(MainActivity.this); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float unit = dp(24) / 24f;
            canvas.save();
            canvas.translate((getWidth() - 24 * unit) / 2f, (getHeight() - 24 * unit) / 2f);
            canvas.scale(unit, unit);
            iconPaint.setColor(TEXT);
            iconPaint.setStyle(Paint.Style.STROKE);
            iconPaint.setStrokeWidth(1.9f);
            iconPaint.setStrokeCap(Paint.Cap.ROUND);
            iconPaint.setStrokeJoin(Paint.Join.ROUND);
            iconPaint.setShader(null);
            if (!lightMode) {
                // Sun icon: centered disc and eight evenly spaced rays.
                canvas.drawCircle(12, 12, 4, iconPaint);
                canvas.drawLine(12, 2, 12, 4, iconPaint);
                canvas.drawLine(12, 20, 12, 22, iconPaint);
                canvas.drawLine(4.93f, 4.93f, 6.35f, 6.35f, iconPaint);
                canvas.drawLine(17.65f, 17.65f, 19.07f, 19.07f, iconPaint);
                canvas.drawLine(2, 12, 4, 12, iconPaint);
                canvas.drawLine(20, 12, 22, 12, iconPaint);
                canvas.drawLine(4.93f, 19.07f, 6.35f, 17.65f, iconPaint);
                canvas.drawLine(17.65f, 6.35f, 19.07f, 4.93f, iconPaint);
            } else {
                // Moon crescent: smooth open crescent, matching Lucide's moon outline.
                iconPath.reset();
                iconPath.moveTo(20.2f, 15.1f);
                iconPath.cubicTo(18.9f, 16.2f, 17.2f, 16.8f, 15.4f, 16.8f);
                iconPath.cubicTo(10.9f, 16.8f, 7.2f, 13.1f, 7.2f, 8.6f);
                iconPath.cubicTo(7.2f, 6.8f, 7.8f, 5.1f, 8.9f, 3.8f);
                iconPath.cubicTo(4.9f, 5.0f, 2.2f, 8.6f, 2.2f, 12.8f);
                iconPath.cubicTo(2.2f, 17.7f, 6.2f, 21.7f, 11.1f, 21.7f);
                iconPath.cubicTo(15.3f, 21.7f, 18.9f, 19.0f, 20.2f, 15.1f);
                canvas.drawPath(iconPath, iconPaint);
            }
            canvas.restore();
        }
    }

    private class ToolIconView extends View {
        private final String kind;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        ToolIconView(String kind) { super(MainActivity.this); this.kind = kind; }
        private void setup(float width) {
            paint.setShader(null);
            paint.setColor(0xFF27A5FF);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(width);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
        }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float scale = Math.min(getWidth(), getHeight()) / 42f;
            canvas.save();
            canvas.translate((getWidth()-36*scale)/2f,(getHeight()-36*scale)/2f);
            canvas.scale(scale,scale);
            setup(2.4f);
            if ("apps".equals(kind)) {
                // Brand-colored social app tiles; each white mark scales in on its own turn.
                long elapsed = SystemClock.uptimeMillis();
                int index = (int)((elapsed / 2400L) % 4L);
                float phase = (elapsed % 2400L) / 2400f;
                float zoom = 0.78f + 0.22f * (float)Math.sin(phase * Math.PI);
                canvas.save();
                canvas.translate(18,18);
                canvas.scale(zoom,zoom);
                canvas.translate(-18,-18);
                paint.setStyle(Paint.Style.FILL);
                paint.setShader(null);
                int tile = index == 0 ? 0xFF1877F2 : index == 1 ? 0xFF090909 :
                    index == 2 ? 0xFFE1306C : 0xFF25D366;
                paint.setColor(tile);
                canvas.drawRoundRect(3,3,33,33,9,9,paint);
                paint.setColor(Color.WHITE);
                if (index == 0) {
                    paint.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));
                    paint.setTextSize(28);
                    canvas.drawText("f",12,29,paint);
                } else if (index == 1) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(3.3f);
                    paint.setStrokeCap(Paint.Cap.SQUARE);
                    canvas.drawLine(10,10,26,26,paint);
                    canvas.drawLine(26,10,10,26,paint);
                } else if (index == 2) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(2.4f);
                    canvas.drawRoundRect(8,8,28,28,6,6,paint);
                    canvas.drawCircle(18,18,4.5f,paint);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(25,11,1.5f,paint);
                } else {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(2.1f);
                    canvas.drawCircle(18,17,10.5f,paint);
                    path.reset();
                    path.moveTo(10,25); path.lineTo(7,30); path.lineTo(16,27);
                    canvas.drawPath(path,paint);
                    paint.setStrokeWidth(2.8f);
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    path.reset();
                    path.moveTo(14,11); path.quadTo(11,14,16,20);
                    path.quadTo(21,25,25,20);
                    canvas.drawPath(path,paint);
                }
                canvas.restore();
                postInvalidateOnAnimation();
            } else if ("proxy".equals(kind)) {
                // Cyan-to-blue gradient keeps the icon family consistently colorful.
                paint.setShader(new android.graphics.LinearGradient(3,8,33,30,
                    new int[] { 0xFF25D7FF, 0xFF168BFF, 0xFF625CFF },
                    null, android.graphics.Shader.TileMode.CLAMP));
                // Upper arrow travels right; lower arrow travels left.
                float phase = (SystemClock.uptimeMillis() % 2400L) / 2400f;
                float travel = (float)Math.sin(phase * Math.PI * 2) * 2.0f;
                canvas.save();
                canvas.translate(travel, 0);
                path.reset();
                path.moveTo(4,12); path.lineTo(30,12);
                path.moveTo(24,6); path.lineTo(30,12); path.lineTo(24,18);
                canvas.drawPath(path,paint);
                canvas.restore();
                canvas.save();
                canvas.translate(-travel, 0);
                path.reset();
                path.moveTo(32,25); path.lineTo(6,25);
                path.moveTo(12,19); path.lineTo(6,25); path.lineTo(12,31);
                canvas.drawPath(path,paint);
                canvas.restore();
                postInvalidateOnAnimation();
            } else if ("settings".equals(kind)) {
                // Premium blue-violet gear rotates inside a steady dark tile.
                float phase = (SystemClock.uptimeMillis() % 2400L) / 2400f;
                canvas.save();
                canvas.translate(18,18);
                canvas.rotate(phase * 360f);
                canvas.translate(-18,-18);
                paint.setStyle(Paint.Style.FILL);
                paint.setShader(new android.graphics.LinearGradient(5,5,31,31,
                    new int[] { 0xFF25B8FF, 0xFF386BFF, 0xFF8A5CFF },
                    null, android.graphics.Shader.TileMode.CLAMP));
                path.reset();
                for (int i=0;i<24;i++) {
                    double angle = Math.PI * 2d * i / 24d - Math.PI/2d;
                    float radius = (i % 3 == 0 || i % 3 == 1) ? 14f : 11f;
                    float x = 18f + (float)Math.cos(angle) * radius;
                    float y = 18f + (float)Math.sin(angle) * radius;
                    if (i == 0) path.moveTo(x,y); else path.lineTo(x,y);
                }
                path.close();
                canvas.drawPath(path,paint);
                paint.setShader(null);
                paint.setColor(BG);
                canvas.drawCircle(18,18,6.2f,paint);
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.8f);
                canvas.drawCircle(18,18,3.1f,paint);
                canvas.restore();
                postInvalidateOnAnimation();
            } else if ("diagnostics".equals(kind)) {
                paint.setShader(new android.graphics.LinearGradient(5,5,32,32,
                    new int[] { 0xFF7C6CFF, 0xFF31B8FF, 0xFF40E0C0 },
                    null, android.graphics.Shader.TileMode.CLAMP));
                canvas.drawCircle(15,15,9,paint);
                canvas.drawLine(22,22,32,32,paint);
                canvas.drawLine(11,15,14,18,paint);
                canvas.drawLine(14,18,20,11,paint);
            } else {
                // Warm coral-to-gold help mark, matching the colorful icon treatment.
                paint.setShader(new android.graphics.LinearGradient(8,5,28,32,
                    new int[] { 0xFFFF6B9A, 0xFFFF8A55, 0xFFFFC857 },
                    null, android.graphics.Shader.TileMode.CLAMP));
                // Handwritten question mark: trace the curve, then draw its dot, then repeat.
                float phase = (SystemClock.uptimeMillis() % 2400L) / 2400f;
                Path question = new Path();
                question.moveTo(10f,11f);
                question.cubicTo(10f,3f,27f,3f,27f,12f);
                question.cubicTo(27f,18f,18f,18f,18f,24f);
                PathMeasure measure = new PathMeasure(question, false);
                float length = measure.getLength();
                float drawLength = length * Math.min(1f, phase / 0.78f);
                Path partial = new Path();
                measure.getSegment(0f, drawLength, partial, true);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2.8f);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeJoin(Paint.Join.ROUND);
                canvas.drawPath(partial, paint);
                if (phase > 0.78f) {
                    float dotProgress = Math.min(1f, (phase - 0.78f) / 0.12f);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(18f, 31f, 1.8f * dotProgress, paint);
                }
                postInvalidateOnAnimation();
            }
            canvas.restore();
        }
    }

    private void showApps() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(8), dp(18), 0);
        panel.addView(text("Cari berdasarkan nama atau package ID aplikasi.", 12, MUTED, false));
        EditText search = proxyInput("Cari aplikasi…", "");
        panel.addView(search);
        new android.app.AlertDialog.Builder(this)
            .setTitle("App Manager")
            .setView(panel)
            .setPositiveButton("CARI", (dialog, which) -> showAppsFiltered(search.getText().toString().trim()))
            .setNeutralButton("SEMUA APLIKASI", (dialog, which) -> showAppsFiltered(""))
            .setNegativeButton("APLIKASI TERSEMBUNYI", (dialog, which) -> showHiddenApps())
            .show();
    }

    private void showAppsFiltered(String filter) {
        Intent query = new Intent(Intent.ACTION_MAIN, null);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> all = getPackageManager().queryIntentActivities(query, 0);
        Collections.sort(all, new Comparator<ResolveInfo>() {
            @Override public int compare(ResolveInfo a, ResolveInfo b) {
                return String.valueOf(a.loadLabel(getPackageManager())).compareToIgnoreCase(
                    String.valueOf(b.loadLabel(getPackageManager())));
            }
        });
        final List<ResolveInfo> apps = new java.util.ArrayList<>();
        for (ResolveInfo app : all) {
            String label = String.valueOf(app.loadLabel(getPackageManager()));
            String pkg = app.activityInfo.packageName;
            if (filter.isEmpty() || label.toLowerCase(java.util.Locale.ROOT).contains(filter.toLowerCase(java.util.Locale.ROOT)) ||
                pkg.toLowerCase(java.util.Locale.ROOT).contains(filter.toLowerCase(java.util.Locale.ROOT))) apps.add(app);
        }
        final String[] names = new String[apps.size()];
        for (int i = 0; i < apps.size(); i++)
            names[i] = String.valueOf(apps.get(i).loadLabel(getPackageManager())) + "\\n" + apps.get(i).activityInfo.packageName;
        if (apps.isEmpty()) {
            info("Tidak ada aplikasi yang cocok dengan pencarian: " + filter);
            return;
        }
        new android.app.AlertDialog.Builder(this)
            .setTitle("Aplikasi ditemukan (" + apps.size() + ")")
            .setItems(names, (dialog, which) -> showAppActions(apps.get(which)))
            .setNeutralButton("CARI LAGI", (dialog, which) -> showApps())
            .setNegativeButton("Tutup", null).show();
    }

    private void showAppActions(ResolveInfo app) {
        String pkg = app.activityInfo.packageName;
        String label = String.valueOf(app.loadLabel(getPackageManager()));
        boolean owner = dpm != null && dpm.isProfileOwnerApp(getPackageName());
        boolean hidden = false;
        if (owner && !pkg.equals(getPackageName())) {
            try { hidden = dpm.isApplicationHidden(new ComponentName(this, WorkspaceAdminReceiver.class), pkg); }
            catch (Exception ignored) { }
        }
        final boolean isHidden = hidden;
        String[] actions = owner && !pkg.equals(getPackageName())
            ? new String[] { "Buka aplikasi", isHidden ? "Tampilkan kembali" : "Sembunyikan dari Work Profile" }
            : new String[] { "Buka aplikasi" };
        new android.app.AlertDialog.Builder(this)
            .setTitle(label)
            .setItems(actions, (d, choice) -> {
                if (choice == 0) {
                    launchApp(pkg);
                } else {
                    new android.app.AlertDialog.Builder(this)
                        .setTitle(isHidden ? "Tampilkan aplikasi?" : "Sembunyikan aplikasi?")
                        .setMessage(isHidden
                            ? "Aplikasi akan ditampilkan kembali di Work Profile."
                            : "Aplikasi tidak akan terlihat atau dapat dibuka oleh pengguna Work Profile sampai ditampilkan kembali.")
                        .setNegativeButton("Batal", null)
                        .setPositiveButton(isHidden ? "Tampilkan" : "Sembunyikan",
                            (confirm, which) -> setAppHidden(pkg, !isHidden))
                        .show();
                }
            })
            .setNegativeButton("Batal", null).show();
    }

    private void showHiddenApps() {
        if (dpm == null || !dpm.isProfileOwnerApp(getPackageName())) {
            info("Daftar aplikasi tersembunyi tersedia setelah Axey menjadi pengelola Work Profile.");
            return;
        }
        Set<String> hidden = getSharedPreferences("axey_hidden_apps", MODE_PRIVATE)
            .getStringSet("packages", new HashSet<String>());
        final String[] packages = hidden.toArray(new String[0]);
        if (packages.length == 0) {
            info("Belum ada aplikasi yang disembunyikan melalui Axey pada perangkat ini.");
            return;
        }
        String[] labels = new String[packages.length];
        for (int i = 0; i < packages.length; i++) {
            try {
                labels[i] = String.valueOf(getPackageManager().getApplicationLabel(
                    getPackageManager().getApplicationInfo(packages[i], 0)));
            } catch (Exception e) {
                labels[i] = packages[i];
            }
        }
        new android.app.AlertDialog.Builder(this)
            .setTitle("Aplikasi tersembunyi (" + packages.length + ")")
            .setItems(labels, (dialog, which) -> new android.app.AlertDialog.Builder(this)
                .setTitle("Tampilkan kembali " + labels[which] + "?")
                .setMessage("Aplikasi akan kembali terlihat di Work Profile.")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Tampilkan", (confirm, choice) -> setAppHidden(packages[which], false))
                .show())
            .setNegativeButton("Tutup", null).show();
    }

    private void launchApp(String pkg) {
        Intent launch = getPackageManager().getLaunchIntentForPackage(pkg);
        if (launch != null) {
            try { startActivity(launch); }
            catch (Exception e) { info("Aplikasi tidak dapat dibuka dari profil ini."); }
        } else info("Aplikasi ini tidak menyediakan layar utama.");
    }

    private void setAppHidden(String pkg, boolean hidden) {
        if (dpm == null || !dpm.isProfileOwnerApp(getPackageName())) {
            info("Fitur ini hanya tersedia saat Axey menjadi pengelola Work Profile.");
            return;
        }
        if (pkg.equals(getPackageName())) {
            info("Axey tidak dapat menyembunyikan dirinya sendiri.");
            return;
        }
        try {
            boolean changed = dpm.setApplicationHidden(
                new ComponentName(this, WorkspaceAdminReceiver.class), pkg, hidden);
            if (changed) {
                Set<String> saved = new HashSet<>(getSharedPreferences("axey_hidden_apps", MODE_PRIVATE)
                    .getStringSet("packages", new HashSet<String>()));
                if (hidden) saved.add(pkg); else saved.remove(pkg);
                getSharedPreferences("axey_hidden_apps", MODE_PRIVATE).edit()
                    .putStringSet("packages", saved).apply();
            }
            info(changed
                ? (hidden ? "Aplikasi disembunyikan dari Work Profile." : "Aplikasi ditampilkan kembali di Work Profile.")
                : "Android tidak mengubah status aplikasi tersebut.");
        } catch (SecurityException e) {
            info("Android menolak perubahan ini untuk aplikasi tersebut.");
        } catch (Exception e) {
            info("Tidak dapat mengubah visibilitas aplikasi: " + e.getMessage());
        }
    }


    private void showProxyManager() {
        android.content.SharedPreferences prefs = getSharedPreferences("axey_proxy", MODE_PRIVATE);
        String type = prefs.getString("type", "SOCKS5");
        String host = prefs.getString("host", "");
        String port = prefs.getString("port", "1080");
        String username = prefs.getString("username", "");
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(8), dp(18), 0);
        TextView note = text("Axey saat ini mendukung SOCKS5 melalui layanan VPN internal. Masukkan detail server; HTTP dan HTTPS belum didukung. Kredensial disimpan lokal pada perangkat.", 12, MUTED, false);
        panel.addView(note);
        space(panel, 12);
        final String[] types = new String[] { "SOCKS5", "HTTP", "HTTPS" };
        final String[] selectedType = new String[] { type };
        Button typeButton = secondary("PROTOKOL: " + type + "  ▾");
        typeButton.setOnClickListener(v -> new android.app.AlertDialog.Builder(this)
            .setTitle("Jenis proxy")
            .setSingleChoiceItems(types, Math.max(0, java.util.Arrays.asList(types).indexOf(selectedType[0])),
                (d, which) -> { selectedType[0] = types[which]; typeButton.setText("PROTOKOL: " + selectedType[0] + "  ▾"); d.dismiss(); })
            .setNegativeButton("BATAL", null).show());
        panel.addView(typeButton, new LinearLayout.LayoutParams(-1, dp(46)));
        space(panel, 8);
        EditText hostInput = proxyInput("Alamat server / IP", host);
        EditText portInput = proxyInput("Port (contoh: 1080)", port);
        EditText userInput = proxyInput("Username (opsional)", username);
        EditText passInput = proxyInput("Password (opsional)", "");
        passInput.setInputType(129);
        panel.addView(hostInput);
        panel.addView(portInput);
        panel.addView(userInput);
        panel.addView(passInput);
        android.app.AlertDialog proxyDialog = new android.app.AlertDialog.Builder(this)
            .setTitle("Proxy Manager")
            .setView(panel)
            .setNeutralButton("TES SERVER", null)
            .setPositiveButton("SIMPAN", (dialog, which) -> {
                String h = hostInput.getText().toString().trim();
                String po = portInput.getText().toString().trim();
                String u = userInput.getText().toString();
                String pw = passInput.getText().toString();
                int portNumber;
                try { portNumber = Integer.parseInt(po); }
                catch (Exception e) { info("Port harus berupa angka antara 1 sampai 65535."); return; }
                if (h.isEmpty() || portNumber < 1 || portNumber > 65535) {
                    info("Isi alamat server dan port yang valid terlebih dahulu.");
                    return;
                }
                if (!"SOCKS5".equals(selectedType[0])) {
                    info("Layanan VPN Axey saat ini hanya mendukung SOCKS5. Pilih SOCKS5 untuk melanjutkan.");
                    return;
                }
                prefs.edit().putString("type", selectedType[0]).putString("host", h)
                    .putString("port", po).putString("username", u).apply();
                if (!pw.isEmpty()) {
                    try { prefs.edit().putString("password_enc", encryptProxyPassword(pw)).apply(); }
                    catch (Exception e) { info("Password tidak dapat diamankan oleh Android Keystore."); return; }
                }
                Toast.makeText(this, "Konfigurasi SOCKS5 tersimpan", Toast.LENGTH_SHORT).show();
                startProxyVpn();
            })
            .setNegativeButton("BATAL", null)
            .create();
        proxyDialog.setOnShowListener(d -> proxyDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL)
            .setOnClickListener(v -> {
                String testHost = hostInput.getText().toString().trim();
                int testPort;
                try { testPort = Integer.parseInt(portInput.getText().toString().trim()); }
                catch (Exception e) { info("Port harus berupa angka yang valid."); return; }
                if (testHost.isEmpty() || testPort < 1 || testPort > 65535) {
                    info("Isi alamat server dan port yang valid terlebih dahulu.");
                    return;
                }
                Toast.makeText(this, "Menguji koneksi server…", Toast.LENGTH_SHORT).show();
                new Thread(() -> {
                    boolean reachable = false;
                    String detail = "";
                    java.net.Socket socket = new java.net.Socket();
                    try {
                        socket.connect(new java.net.InetSocketAddress(testHost, testPort), 5000);
                        reachable = true;
                    } catch (Exception e) {
                        detail = e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
                    } finally {
                        try { socket.close(); } catch (Exception ignored) { }
                    }
                    final boolean ok = reachable;
                    final String reason = detail;
                    runOnUiThread(() -> info(ok
                        ? "Server dapat dijangkau melalui TCP.\n\nCatatan: ini hanya tes port, belum memverifikasi autentikasi proxy atau IP keluar."
                        : "Server tidak dapat dijangkau.\n\n" + reason));
                }).start();
            }));
        proxyDialog.show();
    }


    private void startProxyVpn() {
        Intent permission = VpnService.prepare(this);
        if (permission != null) {
            startActivityForResult(permission, VPN_REQUEST_CODE);
        } else {
            launchProxyService();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) launchProxyService();
            else info("Izin VPN tidak diberikan. Proxy belum tersambung.");
        }
    }

    private void launchProxyService() {
        Intent service = new Intent(this, ProxyVpnService.class);
        service.setAction(ProxyVpnService.ACTION_START);
        try {
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(service);
            else startService(service);
            info("Permintaan koneksi VPN dikirim. Periksa notifikasi Axey Proxy. Setelah tersambung, uji IP dari aplikasi di Work Profile.");
        } catch (Exception e) {
            info("Axey tidak dapat memulai layanan VPN: " + e.getMessage());
        }
    }

    private EditText proxyInput(String hint, String value) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(value);
        input.setHint(hint);
        input.setTextColor(TEXT);
        input.setHintTextColor(MUTED);
        input.setPadding(dp(12), dp(8), dp(12), dp(8));
        input.setBackground(shape(PANEL_2, 12, 0xFF24466A));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(48));
        lp.bottomMargin = dp(8);
        input.setLayoutParams(lp);
        return input;
    }

    private String encryptProxyPassword(String password) throws Exception {
        java.security.KeyStore keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        String alias = "AxeyProxyKey";
        javax.crypto.SecretKey key;
        if (keyStore.containsAlias(alias)) {
            key = (javax.crypto.SecretKey) keyStore.getKey(alias, null);
        } else {
            javax.crypto.KeyGenerator generator = javax.crypto.KeyGenerator.getInstance(
                android.security.keystore.KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            generator.init(new android.security.keystore.KeyGenParameterSpec.Builder(alias,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT |
                android.security.keystore.KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());
            key = generator.generateKey();
        }
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        byte[] iv = cipher.getIV();
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return android.util.Base64.encodeToString(combined, android.util.Base64.NO_WRAP);
    }

    private void deviceInfo(boolean owner) {
        boolean managed = getPackageManager().hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS);
        boolean allowed = Build.VERSION.SDK_INT < 26 ||
            (dpm != null && dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE));
        String message = "Versi Android: " + Build.VERSION.RELEASE +
            "\nAPI level: " + Build.VERSION.SDK_INT +
            "\nManaged Profile didukung: " + (managed ? "Ya" : "Tidak terdeteksi") +
            "\nProvisioning diizinkan: " + (allowed ? "Ya" : "Tidak saat ini") +
            "\nAxey menjadi pengelola profil: " + (owner ? "Ya" : "Belum") +
            "\nPackage: " + getPackageName();
        info(message);
    }

    private void provision() {
        if (dpm == null) {
            info("Layanan Device Policy Manager tidak tersedia pada perangkat ini.");
            return;
        }
        if (dpm.isProfileOwnerApp(getPackageName())) {
            info("Axey Workspace sudah menjadi pengelola Work Profile.");
            render();
            return;
        }
        if (Build.VERSION.SDK_INT < 28) {
            info("Pembuatan Work Profile Axey memerlukan Android 9 atau lebih baru.");
            return;
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS)) {
            info("Perangkat ini tidak melaporkan dukungan Android Managed Profile. Axey tidak akan memaksakan pembuatan profil.");
            return;
        }
        boolean allowed;
        try {
            allowed = dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
        } catch (Exception checkError) {
            info("Axey tidak dapat memeriksa izin pembuatan profil dari Android.\n\n" +
                (checkError.getMessage() == null ? checkError.getClass().getSimpleName() : checkError.getMessage()));
            return;
        }
        if (!allowed) {
            info("Android sedang tidak mengizinkan pembuatan Work Profile. Kemungkinan sudah ada Work Profile, pengguna/perangkat dibatasi kebijakan, atau sistem belum siap. Periksa Pengaturan > Akun dan pastikan tidak ada profil kerja lama.");
            return;
        }
        new android.app.AlertDialog.Builder(this)
            .setTitle("Buat Work Profile Axey?")
            .setMessage("Android akan membuka penyiapan resmi untuk membuat ruang kerja terpisah. Ikuti petunjuk sistem sampai selesai.\n\n• Data pribadi tetap terpisah.\n• Axey menjadi pengelola profil kerja saja.\n• Pembuatan dapat ditolak oleh kebijakan atau sistem perangkat.\n\nLanjutkan ke penyiapan Android?")
            .setNegativeButton("NANTI", null)
            .setPositiveButton("LANJUTKAN", (dialog, which) -> launchProfileProvisioning())
            .show();
    }

    private void launchProfileProvisioning() {
        try {
            Intent intent = new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                new ComponentName(this, WorkspaceAdminReceiver.class));
            if (intent.resolveActivity(getPackageManager()) == null) {
                info("Layar penyiapan Work Profile tidak tersedia pada ROM perangkat ini. Axey tidak dapat menggantikan proses sistem tersebut.");
                return;
            }
            startActivity(intent);
        } catch (SecurityException e) {
            info("Android menolak permintaan pembuatan Work Profile. Pastikan tidak ada profil kerja lama atau kebijakan perangkat yang melarangnya.\n\nDetail: " +
                (e.getMessage() == null ? "Akses ditolak." : e.getMessage()));
        } catch (Exception e) {
            info("Android tidak dapat membuka layar penyiapan Work Profile.\n\nDetail: " +
                (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        }
    }

    private void showProfileSettingsMenu() {
        boolean owner = dpm != null && dpm.isProfileOwnerApp(getPackageName());
        String profileName = "Belum tersedia";
        if (owner) {
            profileName = getPreferences(MODE_PRIVATE).getString("work_profile_name", "Axey Workspace");
        }
        String summary = owner
            ? "STATUS: AKTIF\nNama profil: " + (profileName == null ? "Work Profile" : profileName) +
              "\nPengelola: Axey Workspace\n\nAtur identitas profil, periksa aplikasi, dan buka pengaturan resmi Android."
            : "STATUS: BELUM AKTIF\nBuat Work Profile terlebih dahulu agar pengaturan profil tersedia. Perawatan AXEY tetap dapat digunakan.";
        String[] actions = owner
            ? new String[] {
                "Identitas & nama Work Profile",
                "Tambahkan aplikasi ke Work Profile",
                "Jeda / lanjutkan Work Profile (pengaturan Android)",
                "Buka pengaturan Android Work Profile",
                "Periksa & perbaiki aplikasi",
                "Kelola aplikasi Workspace",
                "Bersihkan cache AXEY",
                "Diagnostik perangkat",
                "Hapus seluruh Work Profile",
                "Hapus data AXEY"
            }
            : new String[] {
                "Buat Work Profile",
                "Diagnostik perangkat",
                "Bersihkan cache AXEY",
                "Hapus data AXEY"
            };
        new android.app.AlertDialog.Builder(this)
            .setTitle("Profile Settings")
            .setMessage(summary)
            .setItems(actions, (dialog, which) -> {
                if (owner) {
                    if (which == 0) showProfileIdentitySettings();
                    else if (which == 1) showInstallExistingApp();
                    else if (which == 2) openWorkProfileSettings();
                    else if (which == 3) openWorkProfileSettings();
                    else if (which == 4) showAppCheck();
                    else if (which == 5) showApps();
                    else if (which == 6) clearAxeyCache();
                    else if (which == 7) deviceInfo(true);
                    else if (which == 8) confirmWipeWorkProfile();
                    else if (which == 9) confirmResetAxeyData();
                } else {
                    if (which == 0) provision();
                    else if (which == 1) deviceInfo(false);
                    else if (which == 2) clearAxeyCache();
                    else if (which == 3) confirmResetAxeyData();
                }
            })
            .setNegativeButton("TUTUP", null)
            .show();
    }

    private void confirmWipeWorkProfile() {
        if (dpm == null || !dpm.isProfileOwnerApp(getPackageName())) {
            info("Axey bukan Profile Owner. Penghapusan Work Profile harus dilakukan melalui Pengaturan Android.");
            return;
        }
        new android.app.AlertDialog.Builder(this)
            .setTitle("Hapus seluruh Work Profile?")
            .setMessage("PERINGATAN: semua aplikasi, akun, file, dan data di dalam Work Profile akan dihapus permanen oleh Android. Data di profil pribadi tidak termasuk. Tindakan ini tidak dapat dibatalkan. Pastikan data kerja sudah dicadangkan.")
            .setNegativeButton("BATAL", null)
            .setPositiveButton("LANJUTKAN", (dialog, which) ->
                new android.app.AlertDialog.Builder(this)
                    .setTitle("Konfirmasi terakhir")
                    .setMessage("Ketik persetujuan dengan menekan tombol HAPUS PROFIL. Android akan menghapus seluruh ruang kerja Axey.")
                    .setNegativeButton("BATAL", null)
                    .setPositiveButton("HAPUS PROFIL", (d, w) -> {
                        try {
                            dpm.wipeData(0);
                        } catch (SecurityException e) {
                            info("Android menolak penghapusan profil. Hapus Work Profile melalui Pengaturan Android.");
                        } catch (Exception e) {
                            info("Work Profile tidak dapat dihapus: " +
                                (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
                        }
                    }).show())
            .show();
    }

    private void showProfileIdentitySettings() {
        if (dpm == null || !dpm.isProfileOwnerApp(getPackageName())) {
            info("Axey harus menjadi Profile Owner untuk mengubah identitas Work Profile.");
            return;
        }
        String current = "Axey Workspace";
        current = getPreferences(MODE_PRIVATE).getString("work_profile_name", "Axey Workspace");
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(8), dp(18), 0);
        panel.addView(text("Nama ini digunakan Android untuk mengenali ruang kerja terkelola.", 12, MUTED, false));
        space(panel, 10);
        EditText nameInput = proxyInput("Nama Work Profile", current);
        nameInput.setSingleLine(true);
        panel.addView(nameInput);
        new android.app.AlertDialog.Builder(this)
            .setTitle("Identitas Work Profile")
            .setView(panel)
            .setNegativeButton("BATAL", null)
            .setNeutralButton("PENGATURAN ANDROID", (dialog, which) -> openWorkProfileSettings())
            .setPositiveButton("SIMPAN", (dialog, which) -> {
                String name = nameInput.getText().toString().trim();
                if (name.isEmpty() || name.length() > 40) {
                    info("Nama profil harus berisi 1 sampai 40 karakter.");
                    return;
                }
                try {
                    dpm.setProfileName(new ComponentName(this, WorkspaceAdminReceiver.class), name);
                    getPreferences(MODE_PRIVATE).edit().putString("work_profile_name", name).apply();
                    info("Nama Work Profile berhasil diperbarui menjadi: " + name);
                    showProfileSettingsMenu();
                } catch (SecurityException e) {
                    info("Android menolak perubahan nama profil.");
                } catch (Exception e) {
                    info("Nama profil tidak dapat diperbarui: " +
                        (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
                }
            }).show();
    }

    private void showInstallExistingApp() {
        if (Build.VERSION.SDK_INT < 28) {
            info("Penambahan aplikasi yang sudah terpasang memerlukan Android 9 atau lebih baru.");
            return;
        }
        if (dpm == null || !dpm.isProfileOwnerApp(getPackageName())) {
            info("Axey harus menjadi pengelola Work Profile untuk menambahkan aplikasi.");
            return;
        }
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(8), dp(18), 0);
        panel.addView(text("Masukkan package ID aplikasi yang sudah terpasang pada perangkat utama. Android akan mencoba memasang salinannya ke Work Profile tanpa menghapus aplikasi atau data pribadi.", 12, MUTED, false));
        space(panel, 10);
        EditText packageInput = proxyInput("Contoh: com.whatsapp", "");
        packageInput.setSingleLine(true);
        packageInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
            android.text.InputType.TYPE_TEXT_VARIATION_URI);
        panel.addView(packageInput);
        new android.app.AlertDialog.Builder(this)
            .setTitle("Tambahkan aplikasi")
            .setView(panel)
            .setNegativeButton("BATAL", null)
            .setPositiveButton("TAMBAHKAN", (dialog, which) -> {
                String packageName = packageInput.getText().toString().trim();
                if (!packageName.matches("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")) {
                    info("Package ID tidak valid. Contoh format: com.whatsapp");
                    return;
                }
                try {
                    boolean installed = dpm.installExistingPackage(
                        new ComponentName(this, WorkspaceAdminReceiver.class), packageName);
                    info(installed
                        ? "Permintaan pemasangan dikirim ke Android untuk " + packageName +
                          ". Periksa daftar aplikasi Work Profile beberapa saat lagi."
                        : "Android tidak memasang " + packageName +
                          ". Pastikan aplikasi tersedia pada perangkat utama dan kompatibel dengan Work Profile.");
                } catch (SecurityException e) {
                    info("Android menolak pemasangan aplikasi ini. Pastikan Axey masih menjadi Profile Owner.");
                } catch (Exception e) {
                    info("Tidak dapat menambahkan aplikasi: " +
                        (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
                }
            }).show();
    }

    private void openWorkProfileSettings() {
        if (dpm == null || !dpm.isProfileOwnerApp(getPackageName())) {
            info("Pengaturan Work Profile tersedia setelah Axey menjadi pengelola profil.");
            return;
        }
        try {
            Intent intent = new Intent("android.settings.MANAGED_PROFILE_SETTINGS");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Intent fallback = new Intent(android.provider.Settings.ACTION_SYNC_SETTINGS);
                startActivity(fallback);
            }
        } catch (Exception e) {
            info("Halaman pengaturan Work Profile tidak tersedia pada ROM ini. Gunakan Pengaturan Android > Akun > Work Profile.");
        }
    }

    private void showAppCheck() {
        if (dpm == null || !dpm.isProfileOwnerApp(getPackageName())) {
            info("Buka AXEY dari profil tempat aplikasi yang ingin diperiksa terpasang.");
            return;
        }
        Intent query = new Intent(Intent.ACTION_MAIN, null);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> apps = getPackageManager().queryIntentActivities(query, 0);
        Collections.sort(apps, new Comparator<ResolveInfo>() {
            @Override public int compare(ResolveInfo a, ResolveInfo b) {
                return String.valueOf(a.loadLabel(getPackageManager())).compareToIgnoreCase(
                    String.valueOf(b.loadLabel(getPackageManager())));
            }
        });
        final ResolveInfo[] repairable = apps.toArray(new ResolveInfo[0]);
        String[] labels = new String[repairable.length];
        for (int i = 0; i < repairable.length; i++) labels[i] = String.valueOf(repairable[i].loadLabel(getPackageManager()));
        if (labels.length == 0) {
            info("Tidak ditemukan aplikasi yang bisa diperbaiki di profil ini.");
            return;
        }
        new android.app.AlertDialog.Builder(this)
            .setTitle("Periksa aplikasi — pilih aplikasi")
            .setMessage("Pilih aplikasi untuk membukanya kembali, melihat info Android, atau memeriksa apakah aplikasi aktif. AXEY tidak akan menghapus data aplikasi tersebut.")
            .setItems(labels, (dialog, which) -> showAppCheckActions(repairable[which]))
            .setNegativeButton("TUTUP", null).show();
    }

    private void showAppCheckActions(ResolveInfo app) {
        String pkg = app.activityInfo.packageName;
        String label = String.valueOf(app.loadLabel(getPackageManager()));
        String[] actions = { "Buka ulang aplikasi", "Buka info aplikasi Android", "Periksa status aplikasi" };
        new android.app.AlertDialog.Builder(this)
            .setTitle("Pemeriksaan: " + label)
            .setItems(actions, (dialog, which) -> {
                if (which == 0) {
                    try {
                        Intent launch = getPackageManager().getLaunchIntentForPackage(pkg);
                        if (launch == null) info("Aplikasi tidak menyediakan aktivitas untuk dibuka.");
                        else { launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(launch); }
                    } catch (Exception e) { info("Aplikasi tidak dapat dibuka: " + e.getMessage()); }
                } else if (which == 1) {
                    try {
                        Intent details = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            android.net.Uri.parse("package:" + pkg));
                        startActivity(details);
                    } catch (Exception e) { info("Info aplikasi tidak tersedia pada perangkat ini."); }
                } else {
                    try {
                        android.content.pm.ApplicationInfo ai = getPackageManager().getApplicationInfo(pkg, 0);
                        info(ai.enabled ? label + " terdaftar aktif di Work Profile." : label + " sedang dinonaktifkan. Buka Info aplikasi untuk mengaktifkannya kembali.");
                    } catch (Exception e) { info("Status aplikasi tidak dapat dibaca: " + e.getMessage()); }
                }
            })
            .setNegativeButton("KEMBALI", null).show();
    }

    private void clearAxeyCache() {
        long before = directorySize(getCacheDir());
        boolean cleared = deleteDirectoryContents(getCacheDir());
        long after = directorySize(getCacheDir());
        long removed = Math.max(0L, before - after);
        info(cleared
            ? "Cache AXEY berhasil dibersihkan. Data aplikasi, konfigurasi proxy, dan data aplikasi lain di Work Profile tidak dihapus.\n\nRuang cache dibersihkan: " + android.text.format.Formatter.formatShortFileSize(this, removed)
            : "Sebagian cache AXEY tidak dapat dihapus. Coba tutup dan buka kembali AXEY.");
    }

    private long directorySize(java.io.File file) {
        if (file == null || !file.exists()) return 0L;
        if (file.isFile()) return file.length();
        long total = 0L;
        java.io.File[] children = file.listFiles();
        if (children != null) for (java.io.File child : children) total += directorySize(child);
        return total;
    }

    private boolean deleteDirectoryContents(java.io.File directory) {
        if (directory == null || !directory.exists()) return true;
        java.io.File[] children = directory.listFiles();
        if (children == null) return true;
        boolean success = true;
        for (java.io.File child : children) {
            if (child.isDirectory()) success &= deleteDirectoryContents(child);
            if (!child.delete()) success = false;
        }
        return success;
    }

    private void confirmResetAxeyData() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Hapus data AXEY saja?")
            .setMessage("Tindakan ini akan menghapus pengaturan, konfigurasi proxy, daftar lokal, database, dan file milik aplikasi AXEY saja. Aplikasi lain, akun, foto, dokumen, dan seluruh Work Profile tidak akan dihapus. AXEY akan memulai ulang seperti baru dipasang.")
            .setNegativeButton("BATAL", null)
            .setPositiveButton("HAPUS DATA AXEY", (dialog, which) -> resetAxeyData())
            .show();
    }

    private void resetAxeyData() {
        try {
            // Remove only AXEY-owned app data. Never call DevicePolicyManager.wipeData().
            deleteDirectoryContents(new java.io.File(getApplicationInfo().dataDir, "shared_prefs"));
            deleteDirectoryContents(new java.io.File(getApplicationInfo().dataDir, "databases"));
            deleteDirectoryContents(getFilesDir());
            deleteDirectoryContents(getCacheDir());
            deleteDirectoryContents(getCodeCacheDir());
            deleteDirectoryContents(getNoBackupFilesDir());
            if (getExternalCacheDir() != null) deleteDirectoryContents(getExternalCacheDir());

            android.content.Intent relaunch = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (relaunch != null) {
                relaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(relaunch);
            }
            finishAffinity();
        } catch (Exception e) {
            info("Sebagian data AXEY tidak dapat dihapus. Detail: " +
                (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        }
    }

    private void openSettings(String action) {
        try { startActivity(new Intent(action)); }
        catch (Exception e) { info("Halaman pengaturan ini tidak tersedia pada perangkat."); }
    }

    private void info(String message) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Axey Workspace")
            .setMessage(message)
            .setPositiveButton("MENGERTI", null).show();
    }
}