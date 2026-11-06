package com.axey.ruangkerja;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
public class WorkAdminReceiver extends DeviceAdminReceiver {
 @Override public void onProfileProvisioningComplete(Context context, Intent intent) {
  super.onProfileProvisioningComplete(context,intent);
  DevicePolicyManager p=(DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
  ComponentName admin=new ComponentName(context,WorkAdminReceiver.class);
  try { p.setProfileName(admin,"Ruang Kerja"); p.setProfileEnabled(admin); }
  catch(RuntimeException e){Log.e("RuangKerja","Profile setup failed",e);}
 }
}
