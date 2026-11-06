package com.axey.workspace;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class WorkspaceAdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        DevicePolicyManager dpm =
            (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm != null && dpm.isProfileOwnerApp(context.getPackageName())) {
            dpm.setProfileName(new android.content.ComponentName(context, WorkspaceAdminReceiver.class),
                "Axey Workspace");
            Toast.makeText(context, "Work Profile Axey siap digunakan", Toast.LENGTH_LONG).show();
        }
    }
}
