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
import android.graphics.Movie;
import android.os.SystemClock;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.net.VpnService;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class MainActivity extends Activity {
    private static final int VPN_REQUEST_CODE = 7401;
    private static final int BG = Color.rgb(5, 10, 20);
    private static final int PANEL = Color.rgb(14, 25, 43);
    private static final int PANEL_2 = Color.rgb(19, 37, 61);
    private static final int BLUE = Color.rgb(18, 151, 255);
    private static final int MUTED = Color.rgb(157, 178, 205);
    private DevicePolicyManager dpm;
    private LinearLayout root;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        render();
    }

    @Override protected void onResume() {
        super.onResume();
        if (dpm != null) render();
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
        AnimatedLogoView mark = new AnimatedLogoView();
        mark.setBackground(shape(Color.BLACK, 15, 0));
        brand.addView(mark, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout brandText = new LinearLayout(this);
        brandText.setOrientation(LinearLayout.VERTICAL);
        brandText.setPadding(dp(12), 0, 0, 0);
        brandText.addView(text("AXEY", 14, Color.WHITE, true));
        brandText.addView(text("WORKSPACE", 11, MUTED, true));
        brand.addView(brandText, new LinearLayout.LayoutParams(0, -2, 1));
        TextView version = text("1.2.6", 11, BLUE, true);
        version.setGravity(Gravity.CENTER);
        version.setPadding(dp(12), dp(7), dp(12), dp(7));
        version.setBackground(shape(PANEL_2, 30, 0));
        brand.addView(version);
        root.addView(brand);
        space(root, 28);

        root.addView(text("Your work.", 32, Color.WHITE, true));
        root.addView(text("Your space.", 32, BLUE, true));
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
        addTool("settings", "Profile settings", "Buka pengaturan aplikasi Android.",
            "BUKA PENGATURAN", v -> openSettings("android.settings.MANAGE_APPLICATIONS_SETTINGS"));
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
        b.setTextColor(0xFFDCEBFA);
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
        badge.setBackground(shape(PANEL_2, 12, 0));
        top.addView(badge, new LinearLayout.LayoutParams(dp(42), dp(42)));
        ObjectAnimator motion;
        if ("settings".equals(icon)) {
            motion = ObjectAnimator.ofFloat(badge, View.ROTATION, 0f, 360f);
            motion.setDuration(6500);
        } else if ("proxy".equals(icon)) {
            motion = ObjectAnimator.ofFloat(badge, View.TRANSLATION_X, -dp(2), dp(2), -dp(2));
            motion.setDuration(1500);
        } else if ("diagnostics".equals(icon)) {
            motion = ObjectAnimator.ofFloat(badge, View.SCALE_X, 1f, 1.10f, 1f);
            motion.setDuration(1700);
        } else {
            motion = ObjectAnimator.ofFloat(badge, View.ALPHA, 0.72f, 1f, 0.72f);
            motion.setDuration(1800);
        }
        motion.setRepeatCount(ObjectAnimator.INFINITE);
        motion.setInterpolator(new AccelerateDecelerateInterpolator());
        motion.setStartDelay(Math.abs(title.hashCode() % 500));
        badge.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View view) { motion.start(); }
            @Override public void onViewDetachedFromWindow(View view) { motion.cancel(); }
        });
        TextView heading = text(title, 16, Color.WHITE, true);
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

    private class AnimatedLogoView extends View {
        private final Movie movie;
        private final Paint logoPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final android.graphics.RectF bounds = new android.graphics.RectF();
        AnimatedLogoView() {
            super(MainActivity.this);
            Movie decoded = null;
            try { decoded = Movie.decodeStream(getResources().openRawResource(R.raw.axey_logo)); }
            catch (Exception ignored) { }
            movie = decoded;
        }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (movie == null || movie.width() <= 0 || movie.height() <= 0) return;
            int duration = movie.duration() > 0 ? movie.duration() : 900;
            movie.setTime((int)(SystemClock.uptimeMillis() % duration));
            float scale = Math.max(getWidth() / (float) movie.width(), getHeight() / (float) movie.height());
            float w = movie.width() * scale;
            float h = movie.height() * scale;
            float left = (getWidth() - w) / 2f;
            float top = (getHeight() - h) / 2f;
            int save = canvas.save();
            bounds.set(0, 0, getWidth(), getHeight());
            canvas.clipRoundRect(bounds, dp(15), dp(15));
            canvas.drawColor(Color.BLACK);
            canvas.translate(left, top);
            canvas.scale(scale, scale);
            movie.draw(canvas, 0, 0, logoPaint);
            canvas.restoreToCount(save);
            postInvalidateOnAnimation();
        }
    }

    private class ToolIconView extends View {
        private final String kind;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        ToolIconView(String kind) { super(MainActivity.this); this.kind = kind; }
        private void setup(float width) {
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
                paint.setStyle(Paint.Style.FILL);
                float[] x = {5,21,5,21}, y = {5,5,21,21};
                for (int i=0;i<4;i++) canvas.drawRoundRect(x[i],y[i],x[i]+10,y[i]+10,2,2,paint);
            } else if ("proxy".equals(kind)) {
                path.reset();
                path.moveTo(4,12); path.lineTo(30,12); path.moveTo(24,6); path.lineTo(30,12); path.lineTo(24,18);
                path.moveTo(32,25); path.lineTo(6,25); path.moveTo(12,19); path.lineTo(6,25); path.lineTo(12,31);
                canvas.drawPath(path,paint);
            } else if ("settings".equals(kind)) {
                canvas.drawCircle(18,18,7,paint);
                for (int i=0;i<8;i++) {
                    double angle = Math.PI*i/4;
                    float x=(float)Math.cos(angle), y=(float)Math.sin(angle);
                    canvas.drawLine(18+x*9,18+y*9,18+x*13,18+y*13,paint);
                }
                canvas.drawCircle(18,18,2,paint);
            } else if ("diagnostics".equals(kind)) {
                canvas.drawCircle(15,15,9,paint);
                canvas.drawLine(22,22,32,32,paint);
                canvas.drawLine(11,15,14,18,paint);
                canvas.drawLine(14,18,20,11,paint);
            } else {
                paint.setTextSize(31);
                paint.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));
                canvas.drawText("?",8,29,paint);
                canvas.drawCircle(18,18,16,paint);
            }
            canvas.restore();
        }
    }

    private void showApps() {
        Intent query = new Intent(Intent.ACTION_MAIN, null);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> apps = getPackageManager().queryIntentActivities(query, 0);
        Collections.sort(apps, new Comparator<ResolveInfo>() {
            @Override public int compare(ResolveInfo a, ResolveInfo b) {
                return String.valueOf(a.loadLabel(getPackageManager())).compareToIgnoreCase(
                    String.valueOf(b.loadLabel(getPackageManager())));
            }
        });
        final String[] names = new String[apps.size()];
        for (int i = 0; i < apps.size(); i++) names[i] = String.valueOf(apps.get(i).loadLabel(getPackageManager()));
        new android.app.AlertDialog.Builder(this)
            .setTitle("Aplikasi terpasang (" + apps.size() + ")")
            .setItems(names, (dialog, which) -> showAppActions(apps.get(which)))
            .setNeutralButton("Aplikasi tersembunyi", (dialog, which) -> showHiddenApps())
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
        input.setTextColor(Color.WHITE);
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
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS)) {
            info("Perangkat ini tidak melaporkan dukungan Android Managed Profile. Axey tidak akan memaksakan proses pembuatan profil.");
            return;
        }
        if (Build.VERSION.SDK_INT >= 26 &&
            !dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)) {
            info("Android belum mengizinkan provisioning Work Profile saat ini. Periksa apakah profil terkelola sudah ada atau ada kebijakan perangkat yang membatasi penambahan profil.");
            return;
        }
        try {
            Intent intent = new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                new ComponentName(this, WorkspaceAdminReceiver.class));
            startActivity(intent);
        } catch (Exception e) {
            String reason = e.getMessage();
            info("Android tidak dapat membuka layar penyiapan Work Profile." +
                (reason == null ? "" : "\n\nDetail: " + reason));
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