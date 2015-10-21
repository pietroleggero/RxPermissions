package com.devdoo.rxpermissions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

@TargetApi(VERSION_CODES.HONEYCOMB)
public class RxPermission extends Fragment {
	private PublishSubject<Boolean> attachedSubject;
	private static final String TAG = RxPermission.class.getSimpleName();
	private static final int REQUEST_PERMISSIONS_CODE = 10;

	// Contains all the current permission requests.
	// Once granted or denied, they are removed from it.
	private Map<String, PublishSubject<Permission>> mSubjects = new HashMap<>();

	public static RxPermission with(FragmentManager fragmentManager) {

		RxPermission rxPermission2 = (RxPermission) fragmentManager.findFragmentByTag(TAG);
		if (rxPermission2 == null) {
			rxPermission2 = new RxPermission();
			fragmentManager.beginTransaction()
					.add(rxPermission2, TAG)
					.commit();
		}
		return rxPermission2;
	}

	public RxPermission() {
		attachedSubject = PublishSubject.create();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		attachedSubject.onNext(true);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		attachedSubject.onNext(true);
	}

	@TargetApi(VERSION_CODES.M)
	public void requestPermission(String... permissions) {
		requestPermissions(permissions, REQUEST_PERMISSIONS_CODE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		for (int i = 0, size = permissions.length; i < size; i++) {
			// Find the corresponding subject
			PublishSubject<Permission> subject = mSubjects.get(permissions[i]);
			if (subject == null) {
				// No subject found
				throw new IllegalStateException("RxPermissions.onRequestPermissionsResult invoked but didn't find the corresponding permission request.");
			}
			mSubjects.remove(permissions[i]);
			boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
			subject.onNext(new Permission(permissions[i], granted));
			subject.onCompleted();
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	/*
	* Register one or several permission requests and returns an observable that
	* emits a {@link Permission} for each requested permission.
	* <p>
	* For SDK &lt; 23, the observable will immediately emit true, otherwise
	* the user response to that request.
	* <p>
	* It handles multiple requests to the same permission, in that case the
	* same observable will be returned.
	*/
	public Observable<Permission> requestEach(final String... permissions) {
		if (permissions == null || permissions.length == 0) {
			throw new IllegalArgumentException("RxPermissions.request requires at least one input permission");
		}
		if (!isAdded()) {
			return attachedSubject.flatMap(new Func1<Boolean, Observable<Permission>>() {
				@Override
				public Observable<Permission> call(Boolean aBoolean) {
					return createRequestEach(permissions);
				}
			}).first();

		} else {
			return createRequestEach(permissions);
		}
	}

	private Observable<Permission> createRequestEach(String[] permissions) {
		if (hasPermissions(permissions)) {
			// Already granted, or not Android M
			// Map all requested permissions to granted Permission objects.
			return Observable.from(permissions)
					.map(new Func1<String, Permission>() {
						@Override
						public Permission call(String s) {
							return new Permission(s, true);
						}
					});
		}
		return createPermissionRequest(permissions);
	}

	/**
	 * Register one or several permission requests and returns an observable that
	 * emits an aggregation of the answers. If all  requested permissions were
	 * granted, it emits true, else false.
	 * <p>
	 * For SDK &lt; 23, the observable will immediately emit true, otherwise
	 * the user response to that request.
	 * <p>
	 * It handles multiple requests to the same permission, in that case the
	 * same observable will be returned.
	 */
	public Observable<Boolean> request(final String... permissions) {
		if (permissions == null || permissions.length == 0) {
			throw new IllegalArgumentException("RxPermissions request requires at least one input permission");
		}
		if (!isAdded()) {
			return attachedSubject.flatMap(new Func1<Boolean, Observable<Boolean>>() {
				@Override
				public Observable<Boolean> call(Boolean aBoolean) {
					return createRequest(permissions);
				}
			}).first();
		} else {
			return createRequest(permissions);
		}
	}

	private Observable<Boolean> createRequest(String[] permissions) {
		if (hasPermissions(permissions)) {
			// Already granted, or not Android M
			return Observable.just(true);
		}
		return createPermissionRequest(permissions)
				.toList()
				.map(new Func1<List<Permission>, Boolean>() {
					@Override
					public Boolean call(List<Permission> permissions) {
						for (Permission p : permissions) {
							if (!p.granted) {
								return false;
							}
						}
						return true;
					}
				});
	}

	@TargetApi(Build.VERSION_CODES.M)
	private Observable<Permission> createPermissionRequest(final String... permissions) {

		final List<Observable<Permission>> list = new ArrayList<>(permissions.length);
		List<String> unrequestedPermissions = new ArrayList<>();

		// In case of multiple permissions, we create a observable for each of them.
		// This helps to handle concurrent requests, for instance when there is one
		// request for CAMERA and STORAGE, and another request for CAMERA only, only
		// one observable will be create for the CAMERA.
		// At the end, the observables are combined to have a unique response.
		for (String permission : permissions) {
			PublishSubject<Permission> subject = mSubjects.get(permission);
			if (subject == null) {
				subject = PublishSubject.create();
				mSubjects.put(permission, subject);
				unrequestedPermissions.add(permission);
			}
			list.add(subject);
		}
		if (!unrequestedPermissions.isEmpty()) {
			requestPermission(permissions);
		}
		if (isAdded()) {
			return Observable.concat(Observable.from(list));
		} else {
			return attachedSubject.flatMap(new Func1<Boolean, Observable<Permission>>() {
				@Override
				public Observable<Permission> call(Boolean aBoolean) {
					return Observable.concat(Observable.from(list));
				}
			});
		}
	}

	/**
	 * Returns true if the permissions is already granted.
	 * <p>
	 * Always true if SDK &lt; 23.
	 */
	private boolean hasPermissions(String... permissions) {
		return !isMarshmallow() || hasRuntimePermissions(permissions);
	}

	private boolean isMarshmallow() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
	}

	@TargetApi(Build.VERSION_CODES.M)
	private boolean hasRuntimePermissions(String... permissions) {
		for (String permission : permissions) {
			if (getContext().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}
}
