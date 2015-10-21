package com.devdoo.sample;

import android.Manifest.permission;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.devdoo.rxpermissions.RxPermission;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(this::openSettings);
	}

	private void openSettings(View v) {
		Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		Uri uri = Uri.fromParts("package", getPackageName(), null);
		intent.setData(uri);
		startActivityForResult(intent, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
					"https://github.com/pietroleggero/RxPermissions")));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	public void enableCamera(View v) {
		RxPermission.with(getFragmentManager()).request(permission.CAMERA)
				.subscribe(isGranted -> showResult(v, isGranted));
	}


	public void enableLocation(View v) {
		RxPermission.with(getFragmentManager()).request(permission.ACCESS_FINE_LOCATION)
				.subscribe(isGranted -> showResult(v, isGranted));
	}

	public void readContacts(View v) {
		RxPermission.with(getFragmentManager()).request(permission.READ_CONTACTS)
				.subscribe(isGranted -> showResult(v, isGranted));
	}

	public void readCalendar(View v) {
		RxPermission.with(getFragmentManager()).request(permission.READ_CALENDAR)
				.subscribe(isGranted -> showResult(v, isGranted));
	}


	/**
	 * You will receive the callback for the global state: if all permission are granted
	 * the isGranted boolean is true, if one permission is not granted the callback is false
	 */
	public void requestAll(View v) {
		RxPermission.with(getFragmentManager()).request(permission.CAMERA, permission.ACCESS_FINE_LOCATION, permission.READ_CONTACTS, permission.READ_CALENDAR)
				.subscribe(isGranted -> showResult(v, isGranted));
	}

	/**
	 * You will receive the callback for each permission
	 */
	public void requestEach(View v) {
		RxPermission.with(getFragmentManager()).requestEach(permission.CAMERA, permission.ACCESS_FINE_LOCATION, permission.READ_CONTACTS, permission.READ_CALENDAR)
				.subscribe(p -> {
							Toast.makeText(MainActivity.this,p.name+" " +p.isGranted,Toast.LENGTH_SHORT).show();
						}
				);
	}

	private void showResult(View v, Boolean granted) {
		Snackbar.make(v,"Permission granted: "+ granted, Snackbar.LENGTH_SHORT).show();
	}

}
