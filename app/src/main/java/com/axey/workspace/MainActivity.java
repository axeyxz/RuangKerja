package com.axey.workspace;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {
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
        TextView mark = text("A", 23, Color.WHITE, true);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(shape(BLUE, 15, 0));
        brand.addView(mark, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout brandText = new LinearLayout(this);
        brandText.setOrientation(LinearLayout.VERTICAL);
        brandText.setPadding(dp(12), 0, 0, 0);
        brandText.addView(text("AXEY", 14, Color.WHITE, true));
        brandText.addView(text("WORKSPACE", 11, MUTED, true));
        brand.addView(brandText, new LinearLayout.LayoutParams(0, -2, 1));
        TextView version = text("1.2.4", 11, BLUE, true);
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
        addTool("▦", "App manager", "Lihat dan buka aplikasi pada ruang Android ini.",
            "BUKA DAFTAR APLIKASI", v -> showApps());
        addTool("⚙", "Profile settings", "Buka pengaturan aplikasi Android.",
            "BUKA PENGATURAN", v -> openSettings("android.settings.MANAGE_APPLICATIONS_SETTINGS"));
        addTool("⌁", "Device diagnostics", "Periksa versi Android dan kesiapan Work Profile.",
            "LIHAT DIAGNOSTIK", v -> deviceInfo(owner));
        addTool("盾", "Help & safety", "Ketahui batasan dan cara kerja profil terkelola.",
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
        TextView badge = text(icon, 19, BLUE, true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(shape(PANEL_2, 12, 0));
        top.addView(badge, new LinearLayout.LayoutParams(dp(42), dp(42)));
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
        if (apps.isEmpty()) {
            info("Tidak ditemukan aplikasi yang bisa dibuka pada profil Android saat ini.");
            return;
        }
        final String[] names = new String[apps.size()];
        for (int i = 0; i < apps.size(); i++) names[i] = String.valueOf(apps.get(i).loadLabel(getPackageManager()));
        new android.app.AlertDialog.Builder(this)
            .setTitle("Aplikasi terpasang (" + apps.size() + ")")
            .setItems(names, (dialog, which) -> {
                String pkg = apps.get(which).activityInfo.packageName;
                Intent launch = getPackageManager().getLaunchIntentForPackage(pkg);
                if (launch != null) {
                    try { startActivity(launch); }
                    catch (Exception e) { info("Aplikasi tidak dapat dibuka dari profil ini."); }
                } else info("Aplikasi ini tidak menyediakan layar utama.");
            }).setNegativeButton("Tutup", null).show();
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