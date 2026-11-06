package com.axey.virtual;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int BLUE=0xff159cff, BG=0xff080e16, CARD=0xff121d2b, WHITE=0xfff3f8ff, MUTED=0xff91a4b9;
    private LinearLayout root, list;
    private SharedPreferences prefs;
    private boolean light=false;

    @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("axey_virtual",MODE_PRIVATE);light=prefs.getBoolean("light",false);render();}
    private int bg(){return light?0xfff3f7fc:BG;}
    private int cardColor(){return light?Color.WHITE:CARD;}
    private int fg(){return light?0xff142235:WHITE;}
    private int sub(){return light?0xff60748a:MUTED;}
    private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(16),dp(15),dp(16),dp(15));c.setBackground(shape(cardColor(),19));return c;}
    private LinearLayout.LayoutParams top(int n){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(n);return p;}
    private void render(){
        getWindow().setStatusBarColor(bg());getWindow().setNavigationBarColor(bg());
        if(android.os.Build.VERSION.SDK_INT>=23)getWindow().getDecorView().setSystemUiVisibility(light?(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR):0);
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(bg());
        root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(20),dp(24),dp(20),dp(30));sc.addView(root);setContentView(sc);
        TextView brand=text("AXEY LABS  /  VIRTUAL SPACE",11,BLUE,true);brand.setLetterSpacing(.12f);root.addView(brand);
        root.addView(text("Axey Virtual",30,fg(),true),top(10));
        root.addView(text("Ruang aplikasi terpisah dalam satu tempat.",14,sub(),false),top(4));
        LinearLayout hero=card();hero.setOrientation(1);
        TextView logo=text("AX",30,Color.WHITE,true);logo.setGravity(Gravity.CENTER);logo.setBackground(shape(BLUE,18));hero.addView(logo,new LinearLayout.LayoutParams(dp(62),dp(62)));
        hero.addView(text("Ruang virtual kamu",19,fg(),true),top(12));
        hero.addView(text("Kelola pustaka APK dan persiapkan aplikasi untuk ruang terisolasi.",13,sub(),false),top(5));
        root.addView(hero,top(20));
        LinearLayout status=card();status.addView(text("STATUS MESIN VIRTUAL",11,BLUE,true));
        status.addView(text("Mesin runtime belum terpasang",17,fg(),true),top(8));
        status.addView(text("Versi awal ini menyiapkan antarmuka dan pustaka APK. Aplikasi yang diimpor belum bisa dijalankan di dalam sandbox sampai mesin virtual benar-benar terintegrasi.",12,sub(),false),top(6));
        root.addView(status,top(13));
        LinearLayout actions=new LinearLayout(this);actions.setOrientation(0);
        Button add=button("+  Impor APK",true);add.setOnClickListener(v->pickApk());
        actions.addView(add,new LinearLayout.LayoutParams(0,dp(52),1));
        Button theme=button(light?"Mode gelap":"Mode terang",false);theme.setOnClickListener(v->{light=!light;prefs.edit().putBoolean("light",light).apply();render();});
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,dp(52),1);tp.leftMargin=dp(10);actions.addView(theme,tp);
        root.addView(actions,top(16));
        root.addView(text("PUSTAKA APK",12,BLUE,true),top(24));
        list=new LinearLayout(this);list.setOrientation(1);root.addView(list,top(10));showImports();
        root.addView(text("Axey Virtual · Experimental build",11,sub(),false),top(28));
    }
    private Button button(String title,boolean primary){Button b=new Button(this);b.setText(title);b.setAllCaps(false);b.setTextSize(13);b.setTypeface(null,Typeface.BOLD);b.setTextColor(primary?Color.WHITE:fg());b.setBackground(shape(primary?BLUE:cardColor(),15));return b;}
    private void pickApk(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/vnd.android.package-archive");try{startActivityForResult(i,41);}catch(Exception e){toast("Pemilih file tidak tersedia.");}}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==41&&result==RESULT_OK&&data!=null&&data.getData()!=null){Uri u=data.getData();String name=displayName(u);Set<String> old=new HashSet<>(prefs.getStringSet("imports",new HashSet<>()));old.add(name);prefs.edit().putStringSet("imports",old).apply();toast(name+" ditambahkan ke daftar persiapan.");showImports();}}
    private String displayName(Uri u){String name="APK dipilih";try(Cursor c=getContentResolver().query(u,null,null,null,null)){if(c!=null&&c.moveToFirst()){int n=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(n>=0)name=c.getString(n);}}catch(Exception ignored){}return name;}
    private void showImports(){list.removeAllViews();Set<String> names=prefs.getStringSet("imports",new HashSet<>());if(names.isEmpty()){LinearLayout empty=card();empty.addView(text("Belum ada APK",15,fg(),true));empty.addView(text("Tekan Impor APK untuk memilih file dari penyimpanan.",12,sub(),false),top(5));list.addView(empty);}else{for(String name:names){LinearLayout c=card();c.addView(text(name,14,fg(),true));c.addView(text("Diimpor ke daftar persiapan · belum berjalan di sandbox",11,sub(),false),top(5));list.addView(c,top(8));}}}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}