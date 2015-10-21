package com.devdoo.rxpermissions;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;

/**
 * Utils class to request permissions
 */
public class PermissionUtils {

	/**
	 * @return true if android version is > of M
	 */
	public static boolean isMarshmallow() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
	}

	/**
	 * @param context     the context of the activity or fragment
	 * @param permissions the array of {@linkplain Permission} to grant
	 * @return {@linkplain Permissions}
	 */
	@TargetApi(VERSION_CODES.M)
	public static Permissions toPermissions(Context context, String... permissions) {
		Permissions perms = new Permissions();
		for (String permission : permissions) {
			if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
				perms.addGranted(new Permission(permission, true));
			} else {
				perms.addDenied(new Permission(permission, false));
			}
		}
		return perms;
	}

	public static Permissions toPermissions(String... permissions) {
		Permissions perms = new Permissions();
		for (String permission : permissions) {
			perms.addGranted(new Permission(permission, true));
		}
		return perms;
	}
}
