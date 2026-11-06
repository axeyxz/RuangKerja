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

public class MainActivity extends Activity {
    private DevicePolicyManager dpm;
    private ComponentName admin;
    private LinearLayout root, appList;
    private SharedPreferences prefs;
    private boolean dark;
    private int bg, surface, text, muted, accent, line;

    @Override public void onCreate(Bundle state) {
        prefs=getSharedPreferences("axey_work",MODE_PRIVATE);
        dark=prefs.getBoolean("dark_mode",true);
        super.onCreate(state);
        applyPalette();
        dpm=(DevicePolicyManager)getSystemService(DEVICE_POLICY_SERVICE);
        admin=new ComponentName(this,WorkAdminReceiver.class);
        renderHome();
    }
    private void applyPalette(){
        bg=dark?0xff101014:0xfff7f7fa;
        surface=dark?0xff1c1c23:Color.WHITE;
        text=dark?0xfff5f3f5:0xff17171d;
        muted=dark?0xffaaa6b0:0xff77737e;
        accent=0xfff04455;
        line=dark?0xff34313a:0xffe8e5eb;
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        if(android.os.Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(dark?0:(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR));
        }
    }
    private boolean owner(){return dpm!=null&&dpm.isProfileOwnerApp(getPackageName());}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private LinearLayout.LayoutParams lp(int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(top);return p;}
    private TextView label(String s,int size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;
    }
    private GradientDrawable shape(int color,int radius){
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;
    }
    private LinearLayout card(){
        LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(16),dp(16),dp(16),dp(16));c.setBackground(shape(surface,20));return c;
    }
    private void page(String title,String subtitle){
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(bg);
        root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(20),dp(24),dp(20),dp(30));sc.addView(root);setContentView(sc);
        TextView brand=label("AXEY WORK  /  WORK PROFILE MANAGER",11,accent,true);brand.setLetterSpacing(.09f);root.addView(brand);
        root.addView(label(title,29,text,true),lp(10));
        root.addView(label(subtitle,14,muted,false),lp(5));
    }
    private void renderHome(){
        page("Halo, Axey 👋","Kelola ruang kerja terpisah dengan mudah.");
        LinearLayout hero=card();hero.setOrientation(1);
        TextView mark=label("AW",32,accent,true);mark.setGravity(Gravity.CENTER);mark.setBackground(shape(dark?0xff352027:0xffffe8eb,18));
        LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(dp(64),dp(64));hero.addView(mark,mp);
        hero.addView(label("Axey Work",22,text,true),lp(12));
        hero.addView(label("Work Profile Manager",13,muted,false),lp(3));
        root.addView(hero,lp(20));
        LinearLayout status=card();status.setOrientation(1);
        status.addView(label("STATUS WORK PROFILE",11,muted,true));
        status.addView(label(owner()?"Profil kerja siap digunakan":"Profil kerja belum dibuat",19,text,true),lp(8));
        status.addView(label(owner()?"Ruang kerja Android terdeteksi pada aplikasi ini.":"Buat profil kerja resmi Android untuk memisahkan aplikasi dan datanya.",13,muted,false),lp(5));
        root.addView(status,lp(14));
        if(owner()){
            action("Kelola aplikasi kerja","Lihat dan buka aplikasi di profil kerja",()->renderApps(),true);
            action("Pengaturan tampilan","Mode gelap, terang, dan preferensi",()->renderSettings(),false);
            action("Info Work Profile","Status dan cara mengelola profil",()->renderAbout(),false);
            TextView tip=label("Untuk menjeda profil kerja, gunakan tombol Work Profile di panel Setelan Cepat Android.",12,muted,false);root.addView(tip,lp(18));
        }else{
            action("Buat Work Profile","Mulai penyiapan profil kerja Android",()->createProfile(),true);
            action("Pengaturan tampilan","Mode gelap, terang, dan preferensi",()->renderSettings(),false);
            action("Tentang Axey Work","Informasi aplikasi",()->renderAbout(),false);
            TextView tip=label("Catatan: Android hanya mengizinkan satu Work Profile pada sebagian besar perangkat. Profil dibuat melalui proses resmi Android.",12,muted,false);root.addView(tip,lp(18));
        }
        root.addView(label("AXEY WORK  •  VERSION 1.2",10,muted,true),lp(28));
    }
    private void action(String title,String sub,Runnable run,boolean primary){
        LinearLayout c=card();c.setOrientation(0);c.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout col=new LinearLayout(this);col.setOrientation(1);
        col.addView(label(title,15,text,true));col.addView(label(sub,12,muted,false),lp(4));
        c.addView(col,new LinearLayout.LayoutParams(0,-2,1));
        c.addView(label("›",28,accent,true));
        c.setOnClickListener(v->run.run());
        if(primary){c.setBackground(shape(dark?0xff382027:0xffffe9ec,20));}
        root.addView(c,lp(12));
    }
    private Button button(String title,boolean primary,Runnable action){
        Button b=new Button(this);b.setText(title);b.setAllCaps(false);b.setTextSize(14);b.setTypeface(null,Typeface.BOLD);
        b.setTextColor(primary?Color.WHITE:text);b.setBackground(shape(primary?accent:surface,15));
        b.setOnClickListener(v->action.run());return b;
    }
    private void renderSettings(){
        page("Pengaturan","Atur tampilan Axey Work.");
        LinearLayout c=card();c.setOrientation(1);
        c.addView(label("TEMA APLIKASI",12,muted,true));
        themeOption(c,"Gelap",true);
        themeOption(c,"Terang",false);
        root.addView(c,lp(20));
        action("Tentang Axey Work","Versi aplikasi dan informasi",()->renderAbout(),false);
        root.addView(button("Kembali ke beranda",false,()->renderHome()),lp(22));
    }
    private void themeOption(LinearLayout parent,String name,boolean value){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(13),0,dp(13));
        row.addView(label((dark==value?"●  ":"○  ")+name,15,text,dark==value),new LinearLayout.LayoutParams(0,-2,1));
        row.addView(label(dark==value?"Aktif":"Pilih",12,dark==value?accent:muted,true));
        row.setOnClickListener(v->{prefs.edit().putBoolean("dark_mode",value).apply();dark=value;applyPalette();renderSettings();});
        parent.addView(row);View divider=new View(this);divider.setBackgroundColor(line);parent.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));
    }
    private void renderAbout(){
        page("Tentang Axey Work","Pengelola Work Profile Android.");
        LinearLayout c=card();c.addView(label("Axey Work",22,text,true));
        c.addView(label("Aplikasi untuk membuat dan mengelola profil kerja Android. Aplikasi tidak membuat emulator dan tidak mengubah IMEI atau identitas perangkat.",14,muted,false),lp(10));
        c.addView(label("Privasi: aplikasi hanya mengelola fitur Work Profile melalui API resmi Android. Data pribadi dan data profil kerja tetap dipisahkan oleh sistem.",13,muted,false),lp(12));
        root.addView(c,lp(20));root.addView(button("Kembali",false,()->renderHome()),lp(20));
    }
    private void createProfile(){
        if(!getPackageManager().hasSystemFeature("android.software.managed_users")){toast("Perangkat tidak mendukung Work Profile.");return;}
        if(android.os.Build.VERSION.SDK_INT>=24&&!dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)){
            new AlertDialog.Builder(this).setTitle("Work Profile tidak tersedia").setMessage("Android saat ini tidak mengizinkan pembuatan profil kerja. Periksa apakah profil kerja lain sudah ada atau fitur dibatasi perangkat.").setPositiveButton("OK",null).show();return;
        }
        Intent i=new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
        i.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,admin);
        i.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED,true);
        try{startActivityForResult(i,1001);}catch(Exception e){toast("Tidak bisa membuka penyiapan: "+e.getMessage());}
    }
    private LauncherApps launcher(){return (LauncherApps)getSystemService(Context.LAUNCHER_APPS_SERVICE);}
    private void renderApps(){
        page("Aplikasi kerja","Aplikasi yang tersedia di Work Profile.");
        appList=new LinearLayout(this);appList.setOrientation(1);root.addView(appList,lp(18));
        List<LauncherActivityInfo> list;
        try{list=launcher().getActivityList(null,Process.myUserHandle());}
        catch(Exception e){appList.addView(label("Tidak dapat membaca daftar aplikasi.",13,muted,false));return;}
        List<LauncherActivityInfo> items=new ArrayList<>();
        for(LauncherActivityInfo a:list)if(!a.getApplicationInfo().packageName.equals(getPackageName()))items.add(a);
        Collections.sort(items,Comparator.comparing(a->String.valueOf(a.getLabel()).toLowerCase()));
        if(items.isEmpty())appList.addView(label("Belum ada aplikasi di profil kerja. Pasang aplikasi melalui Play Store kerja.",13,muted,false));
        for(LauncherActivityInfo a:items){
            LinearLayout c=card();c.addView(label(String.valueOf(a.getLabel()),15,text,true));
            c.addView(label(a.getApplicationInfo().packageName,11,muted,false),lp(3));
            c.setOnClickListener(v->{try{launcher().startMainActivity(a.getComponentName(),a.getUser(),null,null);}catch(Exception e){toast("Tidak dapat membuka aplikasi.");}});
            c.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle(String.valueOf(a.getLabel())).setItems(new String[]{"Bekukan aplikasi","Aktifkan aplikasi"},(d,n)->suspend(a.getApplicationInfo().packageName,n==0)).show();return true;});
            appList.addView(c,lp(8));
        }
        root.addView(button("Kembali",false,()->renderHome()),lp(20));
    }
    private void suspend(String pkg,boolean value){
        try{String[] failed=dpm.setPackagesSuspended(admin,new String[]{pkg},value);toast(failed!=null&&failed.length>0?"Android menolak perubahan.":value?"Aplikasi dibekukan.":"Aplikasi diaktifkan.");renderApps();}
        catch(Exception e){toast("Tidak dapat mengubah status aplikasi.");}
    }
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
