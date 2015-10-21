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
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * RxPermission fragment is used to request a set of runtime permissions
 */
@TargetApi(VERSION_CODES.HONEYCOMB)
public class RxPermission extends Fragment {
	private static final String TAG = RxPermission.class.getSimpleName();
	private static final int REQUEST_PERMISSIONS_CODE = 10;
	private PublishSubject<Boolean> attachedSubject;
	// Contains all the current permission requests.
	// Once granted or denied, they are removed from it.
	private Map<String, PublishSubject<Permission>> mSubjects = new HashMap<>();

	public RxPermission() {
		attachedSubject = PublishSubject.create();
	}

	/**
	 * Create an instance of the shadow fragment
	 *
	 * @param fragmentManager the {@linkplain FragmentManager}, child fragment manager
	 *                        cannot be used because the instance cannot be retained
	 * @return an instance of the fragment
	 */
	public static RxPermission with(FragmentManager fragmentManager) {

		RxPermission rxPermission = (RxPermission) fragmentManager.findFragmentByTag(TAG);
		if (rxPermission == null) {
			rxPermission = new RxPermission();
			fragmentManager.beginTransaction()
					.add(rxPermission, TAG)
					.commit();
		}
		return rxPermission;
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
		attachedSubject.onCompleted();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		attachedSubject.onNext(true);
		attachedSubject.onCompleted();
	}

	@TargetApi(VERSION_CODES.M)
	public void requestPermission(String... permissions) {
		requestPermissions(permissions, REQUEST_PERMISSIONS_CODE);
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
			return attachedSubject.flatMap(isAttached -> createRequestEach(permissions));

		} else {
			return createRequestEach(permissions);
		}
	}

	private Observable<Permission> createRequestEach(String[] permissionArray) {
		Permissions permissions = toPermission(permissionArray);
		return createPermissionRequest(permissions.getDenied()).mergeWith(Observable.from(permissions.getGranted()));
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
	public Observable<Boolean> request(final String... permission) {
		if (permission == null || permission.length == 0) {
			throw new IllegalArgumentException("RxPermissions request requires at least one input permission");
		}
		if (!isAdded()) {
			return attachedSubject.flatMap(isAttached -> createRequest(permission));
		} else {
			return createRequest(permission);
		}
	}

	private Observable<Boolean> createRequest(final String... permissionArray) {
		Permissions permissions = toPermission(permissionArray);
		return createPermissionRequest(permissions.getDenied()).mergeWith(Observable.from(permissions.getGranted()))
				.toList()
				.map(permissionList -> {
					for (Permission p : permissionList) {
						if (!p.isGranted) {
							return false;
						}
					}
					return true;
				});
	}

	@TargetApi(Build.VERSION_CODES.M)
	private Observable<Permission> createPermissionRequest(final List<Permission> permissions) {

		final List<Observable<Permission>> list = new ArrayList<>(permissions.size());
		List<String> unrequestedPermissions = new ArrayList<>();
		for (Permission permission : permissions) {
			PublishSubject<Permission> subject = mSubjects.get(permission.name);
			if (subject == null) {
				subject = PublishSubject.create();
				mSubjects.put(permission.name, subject);
				unrequestedPermissions.add(permission.name);
			}
			list.add(subject);
		}
		if (!unrequestedPermissions.isEmpty()) {
			requestPermission(unrequestedPermissions.toArray(new String[1]));
		}
		if (isAdded()) {
			return Observable.concat(Observable.from(list));
		} else {
			return attachedSubject.flatMap(isAttached -> Observable.concat(Observable.from(list)));
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		for (int i = 0, size = permissions.length; i < size; i++) {
			PublishSubject<Permission> subject = mSubjects.get(permissions[i]);
			if (subject == null) {
				//This can happen if the activity is destroyed and the fragment is not more retained
				//in this case no callback is sent back
				//TODO: How to solve?
				return;
			}
			mSubjects.remove(permissions[i]);
			boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
			subject.onNext(new Permission(permissions[i], granted));
			subject.onCompleted();
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	private Permissions toPermission(String[] permissionArray) {
		if (PermissionUtils.isMarshmallow()) {
			return PermissionUtils.toPermissions(getContext(), permissionArray);
		}
		return PermissionUtils.toPermissions(permissionArray);
	}
}
