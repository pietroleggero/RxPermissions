package com.devdoo.rxpermissions;


import java.util.ArrayList;
import java.util.List;

/**
 * Permissions is an helper model
 * that contains a list of granted permissions and
 * denied permissions
 */
public class Permissions {
	private List<Permission> granted;
	private List<Permission> denied;


	/**
	 * Create an empty instance of Permissions
	 */
	public Permissions() {
		granted = new ArrayList<>();
		denied = new ArrayList<>();
	}

	/**
	 * Add a permission to the granted list
	 *
	 * @param permission {@linkplain Permission}
	 */
	public void addGranted(Permission permission) {
		granted.add(permission);
	}

	/**
	 * Add a permission to the denied list
	 *
	 * @param permission {@linkplain Permission}
	 */
	public void addDenied(Permission permission) {
		denied.add(permission);
	}

	/**
	 * @return the granted list
	 */
	public List<Permission> getGranted() {
		return granted;
	}

	/**
	 * @return the denied list
	 */
	public List<Permission> getDenied() {
		return denied;
	}
}
