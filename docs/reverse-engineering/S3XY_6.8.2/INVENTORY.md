# XAPK / APK inventory

Input: `S3XY_6.8.2.xapk`

The supplied XAPK is a container with three APKs: `base.apk`, `split_0.apk`, and `split_1.apk`.

## base.apk
- ZIP entries: 807
- Uncompressed payload: 19,986,768 bytes
- SHA-256: `d6c9e5717c3721cc098c3d23ff78573a03260125cb0c1a795776356caf580be7`
- `classes.dex`: 8,494,696 bytes
- `classes2.dex`: 2,496 bytes
- `classes3.dex`: 9,455,332 bytes
- `classes4.dex`: 289,728 bytes
- `resources.arsc`: present
- `AndroidManifest.xml`: 22,956 bytes

## split_0.apk
- ZIP entries: 102
- Uncompressed payload: 418,867,890 bytes
- SHA-256: `c47e469a84949481848ccca57e9491c55818f8824e733dbebb3025d5ee08241b`
- Native libraries: 97
- Native library payload: 418,842,304 bytes
- Primary native module: `lib/arm64-v8a/libS3XYButtons_arm64-v8a.so` (299,688,392 bytes)

## split_1.apk
- ZIP entries: 52
- Uncompressed payload: 57,624 bytes
- SHA-256: `e587adeafdb77a6a06dd19d6ad4de1df3fddf444104938c19c3cbdd7adf8a3ee`
- `resources.arsc`: present

## Manifest identifiers observed from binary XML string pool
- package: `com.enhauto.buttons`
- application label: `S3XY`
- application: `com.enhance.EnhanceApplication`
- launcher activity: `com.enhance.EnhanceActivity`
- Qt version string: `6.8.2`

## Permissions observed
- `android.permission.ACCESS_ADSERVICES_AD_ID`
- `android.permission.ACCESS_ADSERVICES_ATTRIBUTION`
- `android.permission.ACCESS_FINE_LOCATION`
- `android.permission.ACCESS_NETWORK_STATE`
- `android.permission.BIND_JOB_SERVICE`
- `android.permission.BLUETOOTH`
- `android.permission.BLUETOOTH_ADMIN`
- `android.permission.BLUETOOTH_ADVERTISE`
- `android.permission.BLUETOOTH_CONNECT`
- `android.permission.BLUETOOTH_SCAN`
- `android.permission.DUMP`
- `android.permission.INTERNET`
- `android.permission.POST_NOTIFICATIONS`
- `android.permission.WRITE_EXTERNAL_STORAGE`
- `android.permission.WRITE_SETTINGS`

## Signature certificate
The APK contains a `BNDLTOOL.RSA` certificate using SHA-256 with RSA. The certificate subject/issuer is the generic Android signing identity `C=US, ST=California, L=Mountain View, O=Google Inc., OU=Android, CN=Android`, valid 2021-09-08 through 2051-09-08. SHA-256 certificate fingerprint:
`7E:39:BD:5F:39:D7:4D:9F:2A:2D:88:85:8B:06:C6:65:88:D6:18:44:D3:14:DA:5E:E1:C4:81:CD:97:45:A8:EB`
