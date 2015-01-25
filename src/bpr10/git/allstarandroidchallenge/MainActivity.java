package bpr10.git.allstarandroidchallenge;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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
	private Button showPlacesButton;
	private AutoCompleteTextView searchField;
	private ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		buildGoogleApiClient();

		mGoogleApiClient.connect();
		distanceField = (EditText) findViewById(R.id.distance_field);
		showPlacesButton = (Button) findViewById(R.id.show_places_button);
		searchField = (AutoCompleteTextView) findViewById(R.id.search_keyword_field);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, COUNTRIES);
		adapter.setNotifyOnChange(true);
		searchField.setAdapter(adapter);
		searchField.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				try {
					if (count % 2 == 1) {
						String url = "http://suggestqueries.google.com/complete/search?q="
								+ s + "&client=chrome";
						StringRequest autoCompleteRequest = new StringRequest(
								url, new Response.Listener<String>() {

									@Override
									public void onResponse(String arg0) {
										try {
											JSONArray resultsArray = new JSONArray(
													arg0);
											JSONArray queryResults = resultsArray
													.getJSONArray(1);
											adapter.clear();
											Log.d(tag, "Predictions " + arg0);
//											JSONObject predictions = new JSONObject(
//													arg0);
											// JSONArray ja = new JSONArray(
											// predictions
											// .getString("predictions"));

											for (int i = 0; i < queryResults
													.length(); i++) {
												// JSONObject jo = (JSONObject)
												// ja
												// .get(i);
												// adapter.add(jo
												// .getString("description"));
												// adapter.notifyDataSetChanged();
												adapter.add(queryResults
														.getString(i));
												adapter.notifyDataSetChanged();
											}
										} catch (JSONException e) {
											e.printStackTrace();
										}

									}
								}, new Response.ErrorListener() {

									@Override
									public void onErrorResponse(VolleyError arg0) {
										Log.d(tag, "Response Error " + arg0);
									}
								});
						AppController.getInstance().addToRequestQueue(
								autoCompleteRequest);
					}

				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		showPlacesButton.setOnClickListener(new OnClickListener() {

			private ProgressDialog mProgressDialog;

			@Override
			public void onClick(View v) {
				if (mLastLocation != null) {
					if (searchField.getText().toString().length() != 0) {
						double filterRadius = Double.parseDouble(distanceField
								.getText().toString() + "0");
						if (filterRadius == 0) {
							filterRadius = 1000;
						}
						String searchQuery;
						if(searchField.getText().toString().toLowerCase(Locale.getDefault()).contains("near me")){
							searchQuery = searchField.getText().toString();
						}else{
							searchQuery = searchField.getText().toString()+" near me";
						}
						mProgressDialog = new ProgressDialog(MainActivity.this);
						mProgressDialog.setMessage("Retrieving your Results");
						mProgressDialog.setIndeterminate(false);
						mProgressDialog
								.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						mProgressDialog.setCancelable(false);
						mProgressDialog.show();
						String url = "https://maps.googleapis.com/maps/api/place/search/json?keyword="
								+ searchQuery.replace(" ", "+")
								+ "&location="
								+ mLastLocation.getLatitude()
								+ ","
								+ mLastLocation.getLongitude()
								+ "&radius="
								+ (int) filterRadius
								* 1000
								+ "&key=AIzaSyA9zOSPtdouVFMlbvOu_5GIgRpH_uWbBIA";
						Log.d(tag, url);
						StringRequest getPlacesrequest = new StringRequest(url,
								new Response.Listener<String>() {

									@Override
									public void onResponse(String arg0) {
										if (mProgressDialog.isShowing()) {
											mProgressDialog.dismiss();
										}
										try {
											JSONObject response = new JSONObject(arg0);
											JSONArray results = response.getJSONArray("results");
											if (results.length()== 0 )
											{
												
											}
										} catch (JSONException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
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
		getSupportActionBar().hide();
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
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {
		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	private static final String[] COUNTRIES = new String[] {};
}
