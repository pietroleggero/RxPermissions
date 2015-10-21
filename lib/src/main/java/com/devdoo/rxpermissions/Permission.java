package com.devdoo.rxpermissions;

/**
 * Permission is the model class used to describe a permission
 * A permission is defined as with a permission name and a permission status
 * that can be granted or not.
 */
public class Permission {
	public final String name;
	public final boolean isGranted;

	/**
	 * Create a new instance of the Permission
	 *
	 * @param name      the permission name, tha must be of the type defined
	 *                  in {@linkplain android.Manifest.permission}
	 * @param isGranted if the permission is granted or not
	 */
	public Permission(String name, boolean isGranted) {
		this.name = name;
		this.isGranted = isGranted;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Permission that = (Permission) o;
		return isGranted == that.isGranted && name.equals(that.name);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + (isGranted ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Permission{" +
				"name='" + name + '\'' +
				", granted=" + isGranted +
				'}';
	}
}
