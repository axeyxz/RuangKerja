package com.axey.workspace;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private final int bg = Color.rgb(7, 13, 27);
    private final int panel = Color.rgb(17, 29, 51);
    private final int blue = Color.rgb(0, 153, 255);
    private DevicePolicyManager dpm;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        render();
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(radius); return d;
    }

    private TextView label(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(0, 8, 0, 8); return t;
    }

    private void render() {
        boolean owner = dpm != null && dpm.isProfileOwnerApp(getPackageName());
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bg);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24, 34, 24, 28);
        scroll.addView(root);
        root.addView(label("AXEY  /  WORKSPACE", 13, blue, true));
        root.addView(label("Your work.\nYour space.", 34, Color.WHITE, true));
        root.addView(label("WORK PROFILE CREATOR & MANAGER", 11, 0xFF9CB2D1, true));

        LinearLayout status = new LinearLayout(this);
        status.setOrientation(LinearLayout.VERTICAL); status.setPadding(18, 16, 18, 16);
        status.setBackground(shape(panel, 28));
        status.addView(label(owner ? "●  WORK PROFILE AKTIF" : "○  BELUM ADA WORK PROFILE", 14,
            owner ? 0xFF42E6A4 : 0xFFFFC45C, true));
        status.addView(label(owner ? "Axey Workspace terdaftar sebagai pengelola profil." :
            "Buat profil kerja melalui proses resmi Android.", 13, 0xFFD1DDF0, false));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.setMargins(0, 24, 0, 18); root.addView(status, sp);

        Button create = button(owner ? "Work Profile sudah disiapkan" : "＋  BUAT WORK PROFILE");
        create.setEnabled(!owner);
        create.setOnClickListener(v -> provision());
        root.addView(create, params());

        Button refresh = button("↻  PERBARUI STATUS");
        refresh.setOnClickListener(v -> render());
        root.addView(refresh, params());

        root.addView(label("PROFILE TOOLS", 12, blue, true));
        addCard(root, "App management", "Kelola aplikasi kerja melalui pengaturan Android.", "Buka pengaturan aplikasi", v -> openSettings("android.settings.MANAGE_APPLICATIONS_SETTINGS"));
        addCard(root, "Profile settings", "Lihat pengaturan dan status Work Profile.", "Buka pengaturan profil", v -> openSettings("android.settings.SYNC_SETTINGS"));
        addCard(root, "Safety first", "Pembuatan profil memerlukan dukungan perangkat dan persetujuan sistem.", "Tentang & bantuan", v -> info());
        root.addView(label("AXEY WORKSPACE  •  VERSION 1.1.0", 10, 0xFF6F83A2, true));
        setContentView(scroll);
    }

    private Button button(String text) {
        Button b = new Button(this); b.setText(text); b.setTextColor(Color.WHITE);
        b.setTextSize(14); b.setAllCaps(false); b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(shape(blue, 24)); return b;
    }
    private LinearLayout.LayoutParams params() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 7, 0, 7); return p;
    }
    private void addCard(LinearLayout root, String title, String desc, String action, View.OnClickListener click) {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 12, 16, 12); card.setBackground(shape(panel, 22));
        card.addView(label(title, 17, Color.WHITE, true));
        card.addView(label(desc, 12, 0xFFB8C8DF, false));
        Button b = button(action); b.setOnClickListener(click); card.addView(b);
        LinearLayout.LayoutParams p = params(); p.topMargin = 12; root.addView(card, p);
    }
    private void provision() {
        try {
            if (dpm == null) { info(); return; }
            if (Build.VERSION.SDK_INT >= 26 &&
                !dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)) {
                info(); return;
            }
            Intent intent = new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                new ComponentName(this, WorkspaceAdminReceiver.class));
            startActivity(intent);
        } catch (Exception e) {
            info();
        }
    }
    private void openSettings(String action) {
        try { startActivity(new Intent(action)); } catch (Exception e) { info(); }
    }
    private void info() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Informasi perangkat")
            .setMessage("Work Profile hanya dapat dibuat jika perangkat mendukung Managed Profile dan Android mengizinkan provisioning. Ketersediaan berbeda menurut versi Android, produsen, dan kebijakan perangkat.")
            .setPositiveButton("Mengerti", null).show();
    }
}
