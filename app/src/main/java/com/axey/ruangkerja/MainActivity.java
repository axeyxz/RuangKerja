package com.axey.ruangkerja;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final int NAVY=0xff17233f, BLUE=0xff5277e8, BG=0xfff5f6fa, MUTED=0xff697085;
    private DevicePolicyManager dpm;
    private ComponentName admin;
    private LinearLayout root, apps;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        dpm=(DevicePolicyManager)getSystemService(DEVICE_POLICY_SERVICE);
        admin=new ComponentName(this,WorkAdminReceiver.class);
        renderHome();
    }
    @Override protected void onResume(){super.onResume();}

    private boolean owner(){return dpm!=null&&dpm.isProfileOwnerApp(getPackageName());}

    private void basePage(String eyebrow,String heading,String subtitle){
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(BG);
        root=new LinearLayout(this); root.setOrientation(1); root.setPadding(dp(20),dp(24),dp(20),dp(32)); scroll.addView(root); setContentView(scroll);
        TextView brand=txt(eyebrow,12,BLUE,true); brand.setLetterSpacing(.12f); root.addView(brand);
        root.addView(txt(heading,28,NAVY,true),lp(8));
        root.addView(txt(subtitle,14,MUTED,false),lp(6));
    }

    private void renderHome(){
        basePage("RUANGKERJA","Ruang terpisah,\nlebih tertata.","Pengelola Work Profile Android");
        LinearLayout status=card(); status.setOrientation(1); status.addView(txt("STATUS PROFIL",11,MUTED,true));
        status.addView(txt(owner()?"Profil kerja aktif":"Belum terhubung ke profil kerja",20,NAVY,true),lp(8));
        status.addView(txt(owner()?"Aplikasi di sini terpisah dari ruang pribadi.":"Buat profil kerja resmi Android untuk memisahkan aplikasi dan datanya.",13,MUTED,false),lp(6));
        root.addView(status,lp(22));
        button("Profil perangkat",false,v->renderDeviceProfile());
        if(owner()){
            button("Buka Play Store kerja",true,v->openWorkApp("com.android.vending"));
            button("Muat ulang aplikasi",false,v->loadApps());
            root.addView(txt("APLIKASI DI PROFIL KERJA",12,BLUE,true),lp(24));
            apps=new LinearLayout(this); apps.setOrientation(1); root.addView(apps,lp(10)); loadApps();
        }else{
            button("Buat profil kerja",true,v->createProfile());
            root.addView(txt("Sebelum mulai",16,NAVY,true),lp(24));
            root.addView(txt("• Android akan menampilkan proses persetujuan resmi.\n• Profil kedua mungkin ditolak jika profil kerja lain sudah ada.\n• Pasang aplikasi dari Play Store di profil kerja.\n• Data aplikasi pribadi tidak disalin.",13,MUTED,false),lp(8));
        }
        root.addView(txt("RuangKerja 1.1 · Informasi perangkat hanya-baca.",11,MUTED,false),lp(28));
    }

    private void renderDeviceProfile(){
        basePage("DEVICE PROFILE","Profil perangkat","Informasi perangkat yang dapat dibaca aplikasi");
        LinearLayout notice=card(); notice.setOrientation(1);
        notice.addView(txt("CATATAN KEAMANAN",12,NAVY,true));
        notice.addView(txt("Halaman ini hanya menampilkan informasi yang diizinkan Android. IMEI, serial, Android ID, dan alamat MAC sistem tidak dapat diubah oleh aplikasi biasa.",13,MUTED,false),lp(8));
        root.addView(notice,lp(18));

        LinearLayout details=card(); details.setOrientation(1);
        details.addView(txt("INFORMASI PERANGKAT",12,BLUE,true));
        addInfo(details,"Brand",Build.BRAND);
        addInfo(details,"Manufacturer",Build.MANUFACTURER);
        addInfo(details,"Model",Build.MODEL);
        addInfo(details,"Product",Build.PRODUCT);
        addInfo(details,"Device",Build.DEVICE);
        addInfo(details,"Android",Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")");
        addInfo(details,"Build",Build.DISPLAY);
        root.addView(details,lp(14));

        SharedPreferences prefs=getSharedPreferences("device_profile_ui",MODE_PRIVATE);
        String profileTag=prefs.getString("profile_tag",null);
        if(profileTag==null){profileTag=UUID.randomUUID().toString().substring(0,8).toUpperCase();prefs.edit().putString("profile_tag",profileTag).apply();}
        LinearLayout local=card();local.setOrientation(1);
        local.addView(txt("ID PROFIL LOKAL",12,BLUE,true));
        local.addView(txt(profileTag,19,NAVY,true),lp(10));
        local.addView(txt("Penanda internal RuangKerja saja — bukan IMEI, Android ID, atau identitas perangkat.",12,MUTED,false),lp(5));
        root.addView(local,lp(14));
        LinearLayout actions=new LinearLayout(this);actions.setOrientation(0);actions.setGravity(Gravity.CENTER);actions.setPadding(0,dp(4),0,0);
        Button restore=smallButton("Pulihkan",false);restore.setOnClickListener(v->{prefs.edit().remove("profile_tag").apply();renderDeviceProfile();toast("Penanda lokal dipulihkan.");});
        Button random=smallButton("Acak penanda",true);random.setOnClickListener(v->{prefs.edit().putString("profile_tag",UUID.randomUUID().toString().substring(0,8).toUpperCase()).apply();renderDeviceProfile();toast("Penanda lokal baru dibuat.");});
        LinearLayout.LayoutParams half=new LinearLayout.LayoutParams(0,dp(50),1);half.setMargins(0,dp(14),dp(6),0);actions.addView(restore,half);
        LinearLayout.LayoutParams half2=new LinearLayout.LayoutParams(0,dp(50),1);half2.setMargins(dp(6),dp(14),0,0);actions.addView(random,half2);
        root.addView(actions);
        button("Kembali",false,v->renderHome());
    }

    private void addInfo(LinearLayout parent,String label,String value){
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(12),0,dp(12));
        TextView left=txt(label,14,MUTED,false);TextView right=txt(value==null||value.isEmpty()?"Tidak tersedia":value,14,NAVY,true);right.setGravity(Gravity.END);
        row.addView(left,new LinearLayout.LayoutParams(0,-2,1));row.addView(right,new LinearLayout.LayoutParams(0,-2,1));parent.addView(row);
        View line=new View(this);line.setBackgroundColor(0xffe8eaf0);parent.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
    }

    private void createProfile(){
        if(!getPackageManager().hasSystemFeature("android.software.managed_users")){toast("Perangkat tidak melaporkan dukungan Work Profile.");return;}
        if(android.os.Build.VERSION.SDK_INT>=24&&!dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)){
            new AlertDialog.Builder(this).setTitle("Belum bisa membuat profil").setMessage("Android saat ini tidak mengizinkan Work Profile. Periksa apakah profil kerja lain sudah ada atau fitur dibatasi perangkat.").setPositiveButton("OK",null).show();return;
        }
        Intent i=new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
        i.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,admin);
        i.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED,true);
        try{startActivityForResult(i,1001);}catch(Exception e){toast("Setup tidak bisa dibuka: "+e.getMessage());}
    }

    private LauncherApps launcher(){return (LauncherApps)getSystemService(Context.LAUNCHER_APPS_SERVICE);}
    private void openWorkApp(String pkg){
        List<LauncherActivityInfo> list=launcher().getActivityList(pkg,Process.myUserHandle());
        if(list==null||list.isEmpty()){toast("Aplikasi tidak ditemukan di profil kerja.");return;}
        try{LauncherActivityInfo a=list.get(0);launcher().startMainActivity(a.getComponentName(),a.getUser(),null,null);}catch(Exception e){toast("Tidak bisa membuka aplikasi.");}
    }
    private void loadApps(){
        if(!owner()||apps==null)return; apps.removeAllViews();
        List<LauncherActivityInfo> list;
        try{list=launcher().getActivityList(null,Process.myUserHandle());}catch(Exception e){apps.addView(txt("Gagal membaca aplikasi.",13,MUTED,false));return;}
        List<LauncherActivityInfo> unique=new ArrayList<>();
        for(LauncherActivityInfo a:list){if(!a.getApplicationInfo().packageName.equals(getPackageName()))unique.add(a);}
        Collections.sort(unique,Comparator.comparing(a->String.valueOf(a.getLabel()).toLowerCase()));
        if(unique.isEmpty()){apps.addView(txt("Belum ada aplikasi. Pasang dari Play Store kerja.",13,MUTED,false));return;}
        for(LauncherActivityInfo a:unique){
            LinearLayout row=card();row.setOrientation(1);
            row.addView(txt(String.valueOf(a.getLabel()),15,NAVY,true));
            row.addView(txt(a.getApplicationInfo().packageName,11,MUTED,false),lp(3));
            row.setOnClickListener(v->{try{launcher().startMainActivity(a.getComponentName(),a.getUser(),null,null);}catch(Exception e){toast("Tidak bisa membuka aplikasi.");}});
            row.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle(String.valueOf(a.getLabel())).setItems(new String[]{"Bekukan aplikasi","Aktifkan aplikasi"},(d,n)->suspend(a.getApplicationInfo().packageName,n==0)).show();return true;});
            apps.addView(row,lp(7));
        }
    }
    private void suspend(String pkg,boolean value){
        try{String[] failed=dpm.setPackagesSuspended(admin,new String[]{pkg},value);toast(failed!=null&&failed.length>0?"Android menolak perubahan.":value?"Aplikasi dibekukan.":"Aplikasi diaktifkan.");loadApps();}catch(Exception e){toast("Tidak bisa mengubah status aplikasi.");}
    }
    private void button(String label,boolean primary,View.OnClickListener click){
        Button b=smallButton(label,primary);b.setOnClickListener(click);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(54));p.topMargin=dp(14);root.addView(b,p);
    }
    private Button smallButton(String label,boolean primary){
        Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(14);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setTextColor(primary?Color.WHITE:NAVY);
        GradientDrawable bg=new GradientDrawable();bg.setColor(primary?BLUE:Color.WHITE);bg.setCornerRadius(dp(14));if(!primary)bg.setStroke(dp(1),0xffdce0ea);b.setBackground(bg);return b;
    }
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(16),dp(16),dp(16),dp(16));GradientDrawable b=new GradientDrawable();b.setColor(Color.WHITE);b.setCornerRadius(dp(18));c.setBackground(b);return c;}
    private TextView txt(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private LinearLayout.LayoutParams lp(int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(top);return p;}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
