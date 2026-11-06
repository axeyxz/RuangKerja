package com.axey.workspace;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class ProxyVpnService extends VpnService {
    public static final String ACTION_START = "com.axey.workspace.proxy.START";
    public static final String ACTION_STOP = "com.axey.workspace.proxy.STOP";
    private static final String CHANNEL = "axey_proxy_vpn";
    private static native boolean TProxyStartService(String configPath, int fd);
    private static native boolean TProxyStopService();
    private static native boolean TProxyIsRunning();
    private ParcelFileDescriptor tunnel;

    static { System.loadLibrary("hev-socks5-tunnel"); }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopTunnel();
            return START_NOT_STICKY;
        }
        startForegroundNotification();
        if (tunnel == null) startTunnel();
        return START_NOT_STICKY;
    }

    private void startTunnel() {
        android.content.SharedPreferences p = getSharedPreferences("axey_proxy", MODE_PRIVATE);
        String host = p.getString("host", "").trim();
        int port;
        try { port = Integer.parseInt(p.getString("port", "1080")); }
        catch (Exception e) { stopSelf(); return; }
        if (host.isEmpty() || port < 1 || port > 65535) { stopSelf(); return; }

        try {
            Builder b = new Builder();
            b.setSession("Axey SOCKS5");
            b.setMtu(1500);
            b.addAddress("10.77.0.2", 32);
            b.addRoute("0.0.0.0", 0);
            b.addDnsServer("1.1.1.1");
            b.addDisallowedApplication(getPackageName());
            tunnel = b.establish();
            if (tunnel == null) { stopSelf(); return; }

            String user = p.getString("username", "");
            String pass = decryptPassword(p.getString("password_enc", ""));
            String config = "tunnel:\n  mtu: 1500\n  ipv4: 10.77.0.2\n  icmp: 'reply'\n" +
                "socks5:\n  address: '" + yaml(host) + "'\n  port: " + port +
                "\n  udp: 'udp'\n";
            if (!user.isEmpty() && !pass.isEmpty())
                config += "  username: '" + yaml(user) + "'\n  password: '" + yaml(pass) + "'\n";
            config += "misc:\n  log-level: warn\n";
            File file = new File(getFilesDir(), "axey-tproxy.yml");
            try (FileOutputStream out = new FileOutputStream(file, false)) {
                out.write(config.getBytes(StandardCharsets.UTF_8));
            }
            if (!TProxyStartService(file.getAbsolutePath(), tunnel.getFd())) {
                tunnel.close(); tunnel = null; stopSelf();
            }
        } catch (Exception e) {
            stopTunnel();
        }
    }

    private String decryptPassword(String encoded) {
        if (encoded == null || encoded.isEmpty()) return "";
        try {
            byte[] all = android.util.Base64.decode(encoded, android.util.Base64.NO_WRAP);
            byte[] iv = java.util.Arrays.copyOfRange(all, 0, 12);
            byte[] data = java.util.Arrays.copyOfRange(all, 12, all.length);
            java.security.KeyStore ks = java.security.KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            javax.crypto.SecretKey key = (javax.crypto.SecretKey) ks.getKey("AxeyProxyKey", null);
            javax.crypto.Cipher c = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
            c.init(javax.crypto.Cipher.DECRYPT_MODE, key, new javax.crypto.spec.GCMParameterSpec(128, iv));
            return new String(c.doFinal(data), StandardCharsets.UTF_8);
        } catch (Exception e) { return ""; }
    }

    private String yaml(String value) { return value.replace("\\", "\\\\").replace("'", "''").replace("\n", ""); }

    private void stopTunnel() {
        try { if (TProxyIsRunning()) TProxyStopService(); } catch (Throwable ignored) { }
        if (tunnel != null) {
            try { tunnel.close(); } catch (Exception ignored) { }
            tunnel = null;
        }
        stopForeground(true);
        stopSelf();
    }

    @Override public void onRevoke() { stopTunnel(); super.onRevoke(); }
    @Override public void onDestroy() { stopTunnel(); super.onDestroy(); }

    private void startForegroundNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(new NotificationChannel(CHANNEL, "Axey Proxy", NotificationManager.IMPORTANCE_LOW));
        }
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, open,
            PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
        Notification n = new Notification.Builder(this, CHANNEL)
            .setContentTitle("Axey Proxy aktif")
            .setContentText("Trafik profil ini diarahkan melalui SOCKS5")
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentIntent(pi).setOngoing(true).build();
        if (Build.VERSION.SDK_INT >= 34)
            startForeground(77, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        else startForeground(77, n);
    }
}
