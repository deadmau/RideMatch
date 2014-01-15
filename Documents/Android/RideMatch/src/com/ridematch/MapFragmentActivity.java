package com.ridematch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
 
public class MapFragmentActivity extends FragmentActivity implements LocationListener {
	
	// Initial offset for calculating the map bounds
	private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;
		  
	// Conversion from feet to meters
	private static final float METERS_PER_FEET = 0.3048f;
		
	// Accuracy for calculating the map bounds
	private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;
	
	private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
	
	public static final String LAT_KEY = "latitude";
	public static final String LNG_KEY = "longitude";
	public static final String ADDRESS_KEY = "address";
	
    GoogleMap googleMap;
    LocationManager locationManager;
    MarkerOptions markerOptions;
    LatLng latLng;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
 
        SupportMapFragment supportMapFragment = (SupportMapFragment)
        getSupportFragmentManager().findFragmentById(R.id.map);
        
        // Getting a reference to the map
        googleMap = supportMapFragment.getMap();
        googleMap.setMyLocationEnabled(true);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
 
        // Getting reference to btn_find of the layout activity_main
        Button find = (Button) findViewById(R.id.find_button);
 
        // Defining button click event listener for the find button
        OnClickListener findClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Getting reference to EditText to get the user input location
                EditText etLocation = (EditText) findViewById(R.id.address);
 
                // Getting user input location
                String location = etLocation.getText().toString();
 
                if(location!=null && !location.equals("")){
                    new GeocoderTask().execute(location);
                }
            }
        };
 
        // Setting button click event listener for the find button
        find.setOnClickListener(findClickListener);
    }
    
    // An AsyncTask class for accessing the GeoCoding Web Service
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>>{
 
        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;
 
            try {
                // Getting a maximum of 10 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 10);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }
 
        @Override
        protected void onPostExecute(List<Address> addresses) {
 
            if(addresses==null || addresses.size()==0){
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }
 
            // Clears all the existing markers on the map
            googleMap.clear();
 
            // Adding Markers on Google Map for each matching address
            for(int i=0;i<addresses.size();i++){
 
                final Address address = (Address) addresses.get(i);
 
                // Creating an instance of GeoPoint, to display in Google Map
                latLng = new LatLng(address.getLatitude(), address.getLongitude());
 
                final String addressText = String.format("%s %s, %s", address.getFeatureName(), address.getThoroughfare(), address.getLocality());
                markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(addressText);
                
                googleMap.addMarker(markerOptions);
                googleMap.setOnMarkerClickListener(new OnMarkerClickListener(){
					@Override
					public boolean onMarkerClick(Marker marker) {
						marker.showInfoWindow();
				        return true;
					}
                }); 
                
                googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener(){
					@Override
					public void onInfoWindowClick(Marker marker) {
						AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MapFragmentActivity.this);
						alert_confirm.setMessage("Is this your address?").setCancelable(false).setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
						    public void onClick(DialogInterface dialog, int which) {
						    	ParseUser user = ParseUser.getCurrentUser();
						    	ParseGeoPoint point = new ParseGeoPoint(address.getLatitude(), address.getLongitude());
						    	if(user!=null) {
						    		try {
						    			JSONObject json = user.getJSONObject("info");
										json.put("location", point);
										json.put("address", addressText);
										user.put("info", json);
									} catch (JSONException e) {
										e.printStackTrace();
									}
						    		user.saveInBackground();
						    	} else{
						    		int len = addressText.length();
							        char[] addr = new char[len];
							        for(int i=0; i < len; i++){
							        	addr[i] = addressText.charAt(i);
							        }
							        SignUp.point = point;
							        SignUp.addressView.setText(addr, 0, len);
						    	}
						        onBackPressed();
						    }
						}).setNegativeButton("No",
						new DialogInterface.OnClickListener() {
						    @Override
						    public void onClick(DialogInterface dialog, int which) {
						    	return;
						    }
						});
						AlertDialog alert = alert_confirm.create();
						alert.show();
					}
                });
                
                // Locate the first location
                if(i==0){
                	LatLngBounds bounds = calculateBoundsWithCenter(latLng);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
                }
            }
        }
    }
    
    /*
     * Helper method to calculate the offset for the bounds used in map zooming
     */
    private double calculateLatLngOffset(LatLng myLatLng, boolean bLatOffset) {
      // The return offset, initialized to the default difference
      double latLngOffset = OFFSET_CALCULATION_INIT_DIFF;
      // Set up the desired offset distance in meters
      float desiredOffsetInMeters = 400f * METERS_PER_FEET;
      // Variables for the distance calculation
      float[] distance = new float[1];
      boolean foundMax = false;
      double foundMinDiff = 0;
      // Loop through and get the offset
      do {
        // Calculate the distance between the point of interest
        // and the current offset in the latitude or longitude direction
        if (bLatOffset) {
          Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, myLatLng.latitude
              + latLngOffset, myLatLng.longitude, distance);
        } else {
          Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, myLatLng.latitude,
              myLatLng.longitude + latLngOffset, distance);
        }
        // Compare the current difference with the desired one
        float distanceDiff = distance[0] - desiredOffsetInMeters;
        if (distanceDiff < 0) {
          // Need to catch up to the desired distance
          if (!foundMax) {
            foundMinDiff = latLngOffset;
            // Increase the calculated offset
            latLngOffset *= 2;
          } else {
            double tmp = latLngOffset;
            // Increase the calculated offset, at a slower pace
            latLngOffset += (latLngOffset - foundMinDiff) / 2;
            foundMinDiff = tmp;
          }
        } else {
          // Overshot the desired distance
          // Decrease the calculated offset
          latLngOffset -= (latLngOffset - foundMinDiff) / 2;
          foundMax = true;
        }
      } while (Math.abs(distance[0] - desiredOffsetInMeters) > OFFSET_CALCULATION_ACCURACY);
      return latLngOffset;
    }

    /*
     * Helper method to calculate the bounds for map zooming
     */
    public LatLngBounds calculateBoundsWithCenter(LatLng myLatLng) {
      // Create a bounds
      LatLngBounds.Builder builder = LatLngBounds.builder();

      // Calculate east/west points that should to be included
      // in the bounds
      double lngDifference = calculateLatLngOffset(myLatLng, false);
      LatLng east = new LatLng(myLatLng.latitude, myLatLng.longitude + lngDifference);
      builder.include(east);
      LatLng west = new LatLng(myLatLng.latitude, myLatLng.longitude - lngDifference);
      builder.include(west);

      // Calculate north/south points that should to be included
      // in the bounds
      double latDifference = calculateLatLngOffset(myLatLng, true);
      LatLng north = new LatLng(myLatLng.latitude + latDifference, myLatLng.longitude);
      builder.include(north);
      LatLng south = new LatLng(myLatLng.latitude - latDifference, myLatLng.longitude);
      builder.include(south);

      return builder.build();
    }

    @Override
    public void onLocationChanged(Location location) {
        Geocoder geocoder = new Geocoder(getBaseContext());
        ArrayList<Address> addresses;
		try {
			addresses = (ArrayList<Address>) geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			final String addressText = String.format("%s %s, %s", addresses.get(0).getFeatureName(), addresses.get(0).getThoroughfare(), addresses.get(0).getLocality());
			new GeocoderTask().execute(addressText);
		} catch (IOException e) {
			e.printStackTrace();
		}
        locationManager.removeUpdates(this);
    }

	@Override
	public void onProviderDisabled(String arg0) {
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		
	}
}
