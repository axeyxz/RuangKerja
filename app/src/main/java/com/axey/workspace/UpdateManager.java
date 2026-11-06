package com.axey.workspace;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

public final class UpdateManager {
    private static final String TAG = "AxeyUpdater";
    private static final String API = "https://api.github.com/repos/axeyxz/RuangKerja/releases/latest";
    private static final String DOWNLOAD_PREFIX = "https://github.com/axeyxz/RuangKerja/releases/download/";
    private static final String PREFS = "axey_update_state";
    private static final String PENDING_FILE = "pending_install_path";
    private static final long CHECK_INTERVAL = 6L * 60L * 60L * 1000L;
    private static volatile boolean checking = false;

    private UpdateManager() { }

    public static void checkForUpdate(Activity activity) {
        if (activity == null) return;
        try {
            String installedVersion = activity.getPackageManager()
                .getPackageInfo(activity.getPackageName(), 0).versionName;
            checkForUpdate(activity, installedVersion == null ? "0" : installedVersion);
        } catch (Exception e) {
            Log.w(TAG, "Cannot read installed app version", e);
        }
    }

    public static void checkForUpdate(Activity activity, String currentVersion) {
        checkForUpdate(activity, currentVersion, false);
    }

    public static void checkForUpdateNow(Activity activity) {
        if (activity == null) return;
        try {
            String installedVersion = activity.getPackageManager()
                .getPackageInfo(activity.getPackageName(), 0).versionName;
            checkForUpdate(activity, installedVersion == null ? "0" : installedVersion, true);
        } catch (Exception e) {
            Log.w(TAG, "Cannot read installed app version", e);
        }
    }

    private static void checkForUpdate(Activity activity, String currentVersion, boolean force) {
        if (activity == null || activity.isFinishing() || checking) return;
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
        long last = prefs.getLong("last_check", 0L);
        if (!force && System.currentTimeMillis() - last < CHECK_INTERVAL) return;
        checking = true;
        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(API).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setRequestProperty("User-Agent", "Axey-Workspace-Updater");
                int status = connection.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK) {
                    throw new IllegalStateException("GitHub returned HTTP " + status);
                }
                StringBuilder body = new StringBuilder();
                try (InputStream in = connection.getInputStream()) {
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = in.read(buffer)) != -1) body.append(new String(buffer, 0, count, "UTF-8"));
                } finally {
                    connection.disconnect();
                }
                JSONObject release = new JSONObject(body.toString());
                if (release.optBoolean("draft", true) || release.optBoolean("prerelease", true)) return;
                String tag = release.optString("tag_name", "");
                String latestVersion = tag.startsWith("v") ? tag.substring(1) : tag;
                if (latestVersion.isEmpty() || compareVersions(latestVersion, currentVersion) <= 0) {
                    prefs.edit().putLong("last_check", System.currentTimeMillis()).apply();
                    return;
                }
                JSONArray assets = release.optJSONArray("assets");
                JSONObject apk = null;
                if (assets != null) {
                    for (int i = 0; i < assets.length(); i++) {
                        JSONObject candidate = assets.optJSONObject(i);
                        if (candidate != null && "app-release.apk".equals(candidate.optString("name"))) {
                            apk = candidate;
                            break;
                        }
                    }
                }
                if (apk == null) throw new IllegalStateException("Release has no app-release.apk asset");
                String url = apk.optString("browser_download_url", "");
                String expectedPrefix = DOWNLOAD_PREFIX + tag + "/";
                if (!url.startsWith(expectedPrefix) || !url.startsWith("https://")) {
                    throw new SecurityException("Unexpected update download URL");
                }
                String digest = apk.optString("digest", "");
                prefs.edit().putLong("last_check", System.currentTimeMillis()).apply();
                JSONObject finalApk = apk;
                activity.runOnUiThread(() -> {
                    if (activity.isFinishing()) return;
                    new AlertDialog.Builder(activity)
                        .setTitle("Pembaruan AXEY tersedia")
                        .setMessage("Versi " + latestVersion + " tersedia. Versi saat ini " + currentVersion +
                            ". AXEY akan mengunduh APK resmi dan meminta persetujuan Android untuk memasangnya. Data aplikasi tidak sengaja dihapus oleh proses pembaruan.")
                        .setNegativeButton("NANTI", null)
                        .setPositiveButton("UNDUH & PASANG", (dialog, which) ->
                            downloadAndInstall(activity, url, digest))
                        .show();
                });
            } catch (Exception e) {
                Log.w(TAG, "Update check failed", e);
            } finally {
                checking = false;
            }
        }, "Axey-Update-Check").start();
    }

    private static int compareVersions(String left, String right) {
        String[] a = left.split("\\.");
        String[] b = right.split("\\.");
        int length = Math.max(a.length, b.length);
        for (int i = 0; i < length; i++) {
            int av = i < a.length ? number(a[i]) : 0;
            int bv = i < b.length ? number(b[i]) : 0;
            if (av != bv) return Integer.compare(av, bv);
        }
        return 0;
    }

    private static int number(String value) {
        String digits = value.replaceAll("[^0-9].*$", "");
        try { return Integer.parseInt(digits); }
        catch (Exception ignored) { return 0; }
    }

    private static void downloadAndInstall(Activity activity, String downloadUrl, String expectedDigest) {
        ProgressDialog progress = new ProgressDialog(activity);
        progress.setMessage("Mengunduh pembaruan AXEY...");
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
        new Thread(() -> {
            File apkFile = null;
            Exception failure = null;
            try {
                URL url = new URL(downloadUrl);
                if (!"https".equalsIgnoreCase(url.getProtocol()) ||
                    !"github.com".equalsIgnoreCase(url.getHost())) {
                    throw new SecurityException("Update host is not trusted");
                }
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestProperty("User-Agent", "Axey-Workspace-Updater");
                int status = connection.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK) {
                    throw new IllegalStateException("APK download returned HTTP " + status);
                }
                File directory = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (directory == null) throw new IllegalStateException("Private download directory unavailable");
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new IllegalStateException("Cannot create private download directory");
                }
                File temporary = new File(directory, "axey-update.apk.part");
                apkFile = new File(directory, "axey-update.apk");
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                long total = 0L;
                try (InputStream in = connection.getInputStream();
                     FileOutputStream out = new FileOutputStream(temporary)) {
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = in.read(buffer)) != -1) {
                        total += count;
                        if (total > 50L * 1024L * 1024L) {
                            throw new IllegalStateException("Update APK exceeds the 50 MB safety limit");
                        }
                        sha.update(buffer, 0, count);
                        out.write(buffer, 0, count);
                    }
                    out.getFD().sync();
                } finally {
                    connection.disconnect();
                }
                String actual = toHex(sha.digest());
                if (expectedDigest != null && expectedDigest.startsWith("sha256:") &&
                    !actual.equalsIgnoreCase(expectedDigest.substring(7))) {
                    throw new SecurityException("APK SHA-256 does not match GitHub release metadata");
                }
                if (apkFile.exists() && !apkFile.delete()) {
                    throw new IllegalStateException("Cannot replace previous update file");
                }
                if (!temporary.renameTo(apkFile)) {
                    throw new IllegalStateException("Cannot finalize downloaded APK");
                }
            } catch (Exception e) {
                failure = e;
                if (apkFile != null) {
                    File partial = new File(apkFile.getParentFile(), "axey-update.apk.part");
                    if (partial.exists()) partial.delete();
                }
            }
            final File downloaded = apkFile;
            final Exception error = failure;
            activity.runOnUiThread(() -> {
                if (progress.isShowing()) progress.dismiss();
                if (error != null || downloaded == null || !downloaded.isFile()) {
                    Toast.makeText(activity, "Pembaruan gagal diunduh: " +
                        (error == null ? "file tidak tersedia" : error.getMessage()), Toast.LENGTH_LONG).show();
                    return;
                }
                installOrRequestPermission(activity, downloaded);
            });
        }, "Axey-Update-Download").start();
    }

    private static String toHex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) out.append(String.format("%02x", b & 0xff));
        return out.toString();
    }

    private static void installOrRequestPermission(Activity activity, File apk) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= 26 &&
            !activity.getPackageManager().canRequestPackageInstalls()) {
            prefs.edit().putString(PENDING_FILE, apk.getAbsolutePath()).apply();
            try {
                Intent settings = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(settings);
                Toast.makeText(activity, "Izinkan AXEY memasang pembaruan, lalu kembali ke AXEY.", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(activity, "Aktifkan izin instalasi aplikasi untuk AXEY di Pengaturan Android.", Toast.LENGTH_LONG).show();
            }
            return;
        }
        launchInstaller(activity, apk);
    }

    public static void resumePendingInstall(Activity activity) {
        if (activity == null || activity.isFinishing() || Build.VERSION.SDK_INT < 26 ||
            !activity.getPackageManager().canRequestPackageInstalls()) return;
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
        String path = prefs.getString(PENDING_FILE, null);
        if (path == null) return;
        prefs.edit().remove(PENDING_FILE).apply();
        File apk = new File(path);
        if (apk.isFile()) launchInstaller(activity, apk);
    }

    private static void launchInstaller(Activity activity, File apk) {
        try {
            Uri uri = FileProvider.getUriForFile(activity,
                activity.getPackageName() + ".fileprovider", apk);
            Intent install = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            install.setDataAndType(uri, "application/vnd.android.package-archive");
            install.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(install);
        } catch (Exception e) {
            Log.e(TAG, "Unable to launch Android package installer", e);
            Toast.makeText(activity, "Android tidak dapat membuka pemasang APK: " +
                e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
