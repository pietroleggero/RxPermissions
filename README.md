# RxPermissions
![device-2015-10-22-005828](https://cloud.githubusercontent.com/assets/1311551/10652589/30502624-7858-11e5-8139-771378cea7e3.png)
An easy way to request Android M permission using rxjava

Requesting permissions now is very easy, you can do it with one line.
Here is how you do it.

# Step 1: Dependencies
To use the library you have to add the dependence in your app, do it in your `build.gradle` file.
`
```groovy
dependencies {
	compile 'com.devdoo.rxpermissions:lib:1.0.1'
}
```

# Step 2 Target
In your app `build.gradle` file set at least:

```groovy
targetSdkVersion 23
```

# Step 3: Add the permission
Add the permission you want to request in `AndroidManifest.xml`
Like in this example:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.devdoo.sample" >
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.READ_CALENDAR" />


    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/AppTheme" >
        <activity
            android:name=".MainActivity"
            android:label="@string/app_name"
            android:theme="@style/AppTheme.NoActionBar" >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

# Step 4: Requesting permissions

`request` to request one or more permission and receive only one return callback.

```java
	public void readContacts(View v) {
		RxPermission.with(getFragmentManager()).request(permission.READ_CONTACTS)
				.subscribe(isGranted -> showResult(v, isGranted));
	}

	public void readCalendar(View v) {
		RxPermission.with(getFragmentManager()).request(permission.READ_CALENDAR)
				.subscribe(isGranted -> showResult(v, isGranted));
	}
```
`request` allows to receive a callback for the global state: if all permission are granted
the boolean value isGranted is true, if one permission is not granted isGranted is false.
NB: Only one result is received.

```java
	public void requestAll(View v) {
		RxPermission.with(getFragmentManager())
				.request(permission.CAMERA,
						permission.ACCESS_FINE_LOCATION,
						permission.READ_CONTACTS,
						permission.READ_CALENDAR)
				.subscribe(isGranted -> showResult(v, isGranted));
	}
```

`requestEach` allows to get the result of the request for each permission.
In this case you will receive 4 individual result.

```java
	public void requestEach(View v) {
		RxPermission.with(getFragmentManager())
				.requestEach(permission.CAMERA,
						permission.ACCESS_FINE_LOCATION,
						permission.READ_CONTACTS,
						permission.READ_CALENDAR)
				.subscribe(this::showResult);
	}
```

# Credits
This library was inspired by https://github.com/tbruyelle/RxPermissions, but it uses a bit different
design that uses headless Fragment to request Android M run-time permissions
