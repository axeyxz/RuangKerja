# RuangKerja — Android Work Profile Manager

RuangKerja adalah aplikasi Android native sederhana yang memulai dan mengelola **Work Profile resmi Android**. Ia bukan emulator/virtual Android dan tidak membuat perangkat Android kedua.

## Fitur versi 1.0

- Memulai provisioning Work Profile lewat layar resmi Android.
- Menamai profil sebagai **Ruang Kerja** dan mengaktifkannya setelah provisioning.
- Membuka Google Play Store yang berjalan di profil kerja.
- Menampilkan aplikasi launcher yang terpasang di profil kerja.
- Tekan-tahan aplikasi di daftar untuk membekukan atau mengaktifkannya kembali (menggunakan kebijakan Android).

## Batasan penting

- Pembuatan profil bergantung pada dukungan ROM/perangkat dan kebijakan Android. Android dapat menolak jika profil kerja sudah ada atau provisioning tidak diizinkan.
- Aplikasi yang ingin dipisahkan harus dipasang dari Play Store di dalam profil kerja. Data aplikasi pribadi tidak disalin ke profil kerja.
- Google Play Store harus tersedia/diaktifkan di profil kerja agar instalasi aplikasi berjalan mudah.
- Tidak mengubah IMEI, Android ID, serial number, alamat IP, atau identitas perangkat. API Android biasa tidak memberi aplikasi ini kemampuan tersebut.
- Tidak menjamin aplikasi pihak ketiga akan memperlakukan Work Profile sama seperti instalasi biasa.

## Build dengan Android Studio

1. Ekstrak ZIP.
2. Buka folder `RuangKerja` dari Android Studio.
3. Izinkan Gradle sync, gunakan JDK 17 dan Android SDK Platform 35.
4. Pilih **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. APK debug akan muncul di `app/build/outputs/apk/debug/app-debug.apk`.

## Build APK via GitHub Actions

Project ini juga menyertakan workflow `.github/workflows/build-apk.yml`. Upload folder project ini ke repository GitHub, buka tab **Actions**, jalankan workflow **Build RuangKerja APK**, lalu unduh artifact `RuangKerja-debug-apk`.

## Catatan implementasi

Provisioning memakai `DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE` (DPC-first Work Profile). Android tetap menampilkan dan mengendalikan layar persetujuan provisioning. Aplikasi tidak dapat membuat managed profile secara diam-diam.
