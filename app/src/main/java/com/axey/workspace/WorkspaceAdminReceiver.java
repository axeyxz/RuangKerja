package com.axey.workspace;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.Toast;

public class WorkspaceAdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        DevicePolicyManager dpm =
            (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm == null) {
            Toast.makeText(context, "Work Profile dibuat, tetapi layanan pengelola tidak tersedia.", Toast.LENGTH_LONG).show();
            return;
        }

        ComponentName admin = new ComponentName(context, WorkspaceAdminReceiver.class);
        try {
            if (!dpm.isProfileOwnerApp(context.getPackageName())) {
                Toast.makeText(context, "Penyiapan selesai, tetapi Axey belum menjadi pengelola profil.", Toast.LENGTH_LONG).show();
                return;
            }

            // Explicitly enable the managed profile after Android completes provisioning.
            // This uses the official, profile-scoped DevicePolicyManager API.
            dpm.setProfileEnabled(admin);

            // Apply only profile-scoped branding and support information.
            dpm.setProfileName(admin, "Axey Workspace");
            dpm.setOrganizationColor(admin, Color.rgb(45, 125, 245));
            dpm.setShortSupportMessage(admin, "Dikelola oleh Axey Workspace");
            dpm.setLongSupportMessage(admin,
                "Profil kerja ini dikelola oleh Axey Workspace. Data pribadi di luar profil kerja tetap terpisah.");
            Toast.makeText(context, "Work Profile Axey berhasil disiapkan.", Toast.LENGTH_LONG).show();
        } catch (SecurityException e) {
            Toast.makeText(context, "Work Profile aktif, tetapi sebagian pengaturan tampilan ditolak Android.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Work Profile aktif. Beberapa pengaturan awal tidak dapat diterapkan.", Toast.LENGTH_LONG).show();
        }
    }
}
