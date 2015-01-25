package bpr10.git.allstarandroidchallenge;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends ActionBarActivity implements
		OnMapReadyCallback {
	private String tag = getClass().getSimpleName();
	private String resultString;
	private double lat, lng;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		resultString = getIntent().getStringExtra("SearchResults");
		lat = Double.parseDouble(getIntent().getStringExtra("CurrentLocation")
				.split(",")[0]);
		lng = Double.parseDouble(getIntent().getStringExtra("CurrentLocation")
				.split(",")[1]);

		MapFragment mapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private List<DisplayObject> makeResultsList(String resultString) {
		List<DisplayObject> list = new ArrayList<DisplayObject>();
		try {
			JSONObject response = new JSONObject(resultString);
			JSONArray results = response.getJSONArray("results");
			for (int i = 0; i < results.length(); i++) {
				list.add(new DisplayObject(results.getJSONObject(i)
						.getJSONObject("geometry").getJSONObject("location")
						.getDouble("lat"), results.getJSONObject(i)
						.getJSONObject("geometry").getJSONObject("location")
						.getDouble("lng"), results.getJSONObject(i).getString(
						"name"), results.getJSONObject(i).getString("vicinity")));
			}
			Log.d(tag, "Result List size " + list.size());

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	private List<MarkerOptions> makeMarkers(List<DisplayObject> objectList) {
		List<MarkerOptions> markerList = new ArrayList<MarkerOptions>();
		for (int i = 0; i < objectList.size(); i++) {
			markerList.add(new MarkerOptions()
					.position(
							new LatLng(objectList.get(i).lat,
									objectList.get(i).lng))
					.title(objectList.get(i).name)
					.snippet("Tap to listen to audio Review"));
		}
		return markerList;
	}

	@Override
	public void onMapReady(GoogleMap map) {
		map.setMyLocationEnabled(true);

		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng),
				0));
		// Zoom in, animating the camera.
		map.animateCamera(CameraUpdateFactory.zoomIn());
		// Zoom out to zoom level 10, animating with a duration of 2 seconds.
		map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
		List<MarkerOptions> markerList = makeMarkers(makeResultsList(resultString));
		for (int i = 0; i < markerList.size(); i++) {
			map.addMarker(markerList.get(i));
		}
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				AppController.playSound(getApplicationContext(), R.raw.pling);
			}
		});
		map.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				if (marker.isInfoWindowShown()) {
					marker.hideInfoWindow();
				}
				return false;
			}
		});

	}
}
