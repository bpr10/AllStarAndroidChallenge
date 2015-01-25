package bpr10.git.allstarandroidchallenge;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends ActionBarActivity implements
		ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	private GoogleApiClient mGoogleApiClient;
	private Location mLastLocation;
	private String tag = getClass().getSimpleName();
	private EditText distanceField;
	private Spinner searchField;
	private Button showPlacesButton;
	ProgressDialog pDialog;
	private String categoryKeyword = "DEFAULT";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		buildGoogleApiClient();
		mGoogleApiClient.connect();
		searchField = (Spinner) findViewById(R.id.search_keyword_field);
		distanceField = (EditText) findViewById(R.id.distance_field);
		showPlacesButton = (Button) findViewById(R.id.show_places_button);
		SpinerApapter spinerAdapter = new SpinerApapter(this,
				R.layout.filter_spinner_item, AppController.CATEGORIES);
		searchField.setAdapter(spinerAdapter);
		searchField.setClickable(true);
		searchField
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						if (searchField.getSelectedItemPosition() != 0) {
							categoryKeyword = AppController.CATEGORIES[searchField
									.getSelectedItemPosition()];

						} else {
							categoryKeyword = "DEFAULT";

						}

					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						categoryKeyword = "DEFAULT";

					}
				});

		showPlacesButton.setOnClickListener(new OnClickListener() {

			private ProgressDialog mProgressDialog;

			@Override
			public void onClick(View v) {
				if (mLastLocation != null) {
					if (categoryKeyword != "DEFAULT") {
						double filterRadius = Double.parseDouble(distanceField
								.getText().toString() + "0");
						if (filterRadius == 0) {
							filterRadius = 100;
						}
						mProgressDialog = new ProgressDialog(MainActivity.this);
						mProgressDialog.setMessage("Retrieving your Results");
						mProgressDialog.setIndeterminate(false);
						mProgressDialog
								.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						mProgressDialog.setCancelable(false);
						mProgressDialog.show();
						String url = "https://maps.googleapis.com/maps/api/place/search/json?keyword="
								+ categoryKeyword.replace(" ", "+")
								+ "&location="
								+ mLastLocation.getLatitude()
								+ ","
								+ mLastLocation.getLongitude()
								+ "&radius="
								+ (int) filterRadius
								* 100
								+ "&key=AIzaSyA9zOSPtdouVFMlbvOu_5GIgRpH_uWbBIA";
						Log.d(tag, url);
						StringRequest getPlacesrequest = new StringRequest(url,
								new Response.Listener<String>() {

									@Override
									public void onResponse(String arg0) {
										if (mProgressDialog.isShowing()) {
											mProgressDialog.dismiss();
										}
										Log.d(tag, "Places Response : " + arg0);
										try {
											Intent i = new Intent(
													MainActivity.this,
													MapActivity.class);
											i.putExtra("SearchResults", arg0);
											i.putExtra(
													"CurrentLocation",
													mLastLocation.getLatitude()
															+ ","
															+ mLastLocation
																	.getLongitude());
											startActivity(i);
										} catch (Exception e) {
											e.printStackTrace();
										}

									}
								}, new Response.ErrorListener() {

									@Override
									public void onErrorResponse(VolleyError arg0) {
										if (mProgressDialog.isShowing()) {
											mProgressDialog.dismiss();
										}
										Log.e(tag, "Places Response Error: "
												+ arg0);
									}
								});
						AppController.getInstance().addToRequestQueue(
								getPlacesrequest);

					} else {
						// Category Not Selected
						Toast.makeText(getApplicationContext(),
								"Please Select a Category", Toast.LENGTH_SHORT)
								.show();
					}

				} else {
					// Location Disabled
					new SweetAlertDialog(MainActivity.this,
							SweetAlertDialog.WARNING_TYPE)
							.setTitleText("GPS Disabled!")
							.setContentText(
									"Do you want us to open settings to turn on Location Services")
							.setConfirmText("Yes,take me there!")
							.setConfirmClickListener(
									new SweetAlertDialog.OnSweetClickListener() {
										@Override
										public void onClick(
												SweetAlertDialog sDialog) {
											sDialog.dismissWithAnimation();
											Intent i = new Intent(
													new Intent(
															Settings.ACTION_LOCATION_SOURCE_SETTINGS));
											startActivity(i);
										}
									})
							.showCancelButton(true)
							.setCancelClickListener(
									new SweetAlertDialog.OnSweetClickListener() {
										@Override
										public void onClick(
												SweetAlertDialog sDialog) {
											sDialog.cancel();
										}
									}).show();

				}

			}
		});

	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(tag, "Connection failed: ConnectionResult.getErrorCode() = "
				+ result.getErrorCode());
	}

	@Override
	public void onConnected(Bundle arg0) {
		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);
		if (mLastLocation != null) {
			Toast.makeText(getApplicationContext(),
					mLastLocation.getLatitude() + "", 1000).show();
		}

	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.i(tag, "Connection suspended");
		mGoogleApiClient.connect();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	@Override
	protected void onResume() {
		buildGoogleApiClient();
		mGoogleApiClient.connect();
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}

	}

	@Override
	public void onLocationChanged(Location location) {
		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);
		if (mLastLocation != null) {
			Toast.makeText(getApplicationContext(),
					mLastLocation.getLatitude() + "", 1000).show();
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {
		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);
		if (mLastLocation != null) {
			Toast.makeText(getApplicationContext(),
					mLastLocation.getLatitude() + "", 1000).show();
		}
	}

	@Override
	public void onProviderDisabled(String provider) {

	}
}
