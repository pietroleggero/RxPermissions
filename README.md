# RxPermissions
An easy way to request Android M permission using rxjava

Requesting permissions now is very easy, you can do it with one line.
Here is how you do it.

# Requesting permissions

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
