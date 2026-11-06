# Axey Workspace

A dark-blue Android Work Profile helper and Device Policy Controller starter.

## Build
Run the **Build Axey Workspace APK** workflow from GitHub Actions, or build locally with Gradle 8.9, JDK 17 and Android SDK 35.

## Important
- Work Profile provisioning is performed by Android's system provisioning flow.
- The device must support managed profiles and permit provisioning.
- This starter does not bypass Android policy restrictions or modify IMEI/Android ID.
- The app must be installed and launched in the personal profile before provisioning.
- After provisioning, Android may show system setup screens and require user confirmation.

## Package
`com.axey.workspace`
