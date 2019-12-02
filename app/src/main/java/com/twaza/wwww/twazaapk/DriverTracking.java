package com.twaza.wwww.twazaapk;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.twaza.wwww.twazaapk.Common.Common;
import com.twaza.wwww.twazaapk.Helper.DirectionsJSONParser;

import com.twaza.wwww.twazaapk.R;
import com.twaza.wwww.twazaapk.Remote.IFMCService;
import com.twaza.wwww.twazaapk.Remote.IGoogleAPI;
import com.twaza.wwww.twazaapk.mode.FCMResponse;
import com.twaza.wwww.twazaapk.mode.Notification;
import com.twaza.wwww.twazaapk.mode.Sender;
import com.twaza.wwww.twazaapk.mode.Token;

public class DriverTracking extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient
        .OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks,
        LocationListener
{

    private GoogleMap mMap;
    double riderlat,riderlng;
   String customerId;
    private static  final int  PLAY_SERVICE_RES_REQUEST =7001;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    public static int UPDATE_INTERVAL =5000;
    public static  int FATEST_INTERVAL =3000;
    public static int DISOLACEMENT =10;
    private Circle riderMark;
    private Marker driverMarker;
    private Polyline direction;
    IGoogleAPI mService;
    IFMCService mFCMService;
    GeoFire geoFire;
    Button btnstrattrips;
    Location pickeruplocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (getIntent() !=null)
        {
            riderlat= getIntent().getDoubleExtra("lat",-1.0);
            riderlng= getIntent().getDoubleExtra("lng",-1.0);
            customerId = getIntent().getStringExtra("customerId");
        }
        mService = Common.getGoogleApi();
        mFCMService = Common.getFCMService();
        setUpLocation();
        btnstrattrips = (Button)findViewById(R.id.btnstrartTrip);
        btnstrattrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnstrattrips.getText().equals("TANGIRA URUGENDO"))
                {
                    pickeruplocation =Common.mLastLocation;
                    btnstrattrips.setText("DROP OFF HERE");
                }
                else if (btnstrattrips.getText().equals("TANGIRA URUGENDO"))
                {
                    calculatecashfee(pickeruplocation,Common.mLastLocation);
                }
            }
        });
    }

    private void calculatecashfee(final Location pickeruplocation, Location mLastLocation) {

        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+pickeruplocation.getLatitude()+","+pickeruplocation.getLongitude()+"&"+
                    "destination="+mLastLocation.getLatitude()+","+mLastLocation.getLongitude()+"&"+"Key="+
                    getResources().getString(R.string.google_direction_api);


            mService.getPath( requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response)
                        {

                            try {
                          JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");

                                JSONObject object = routes.getJSONObject(0);
                                JSONArray legs = object.getJSONArray("legs");
                                JSONObject legsobject = legs.getJSONObject(0);
                                JSONObject distance = legsobject.getJSONObject("distance");
                                String distance_text = distance.getString("text");
                                Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+",""));

                                JSONObject timeobject = legsobject.getJSONObject("destination");
                                String time_text = timeobject.getString("text");
                                Double time_value = Double.parseDouble(time_text.replaceAll("[^0-9\\\\.]+",""));
                                Intent intent = new Intent(DriverTracking.this,TripDitailes.class);
                                intent.putExtra("start_address",legsobject.getString("start_address"));
                                intent.putExtra("end_address",legsobject.getString("end_address"));
                                intent.putExtra("time",String.valueOf(time_value));
                                intent.putExtra("distance",String.valueOf(distance_value));
                                intent.putExtra("total",Common.formulaprice(distance_value,time_value));
                                intent.putExtra("location_start",String.format("%f,%f",pickeruplocation.getLatitude(),pickeruplocation.getLongitude()));
                                intent.putExtra("location_end",String.format("%f,%f",Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));

                                startActivity(intent);
                                finish();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t)
                        {
                            Toast.makeText(DriverTracking.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void setUpLocation()

    {
        if (ckeckplayServices())
        {
            buildGoogleApiClient();
            createLocationRequest();

        }

    }
    private void createLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISOLACEMENT);


    }

    private void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    private boolean ckeckplayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)

        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RES_REQUEST).show();
            else {
                Toast.makeText(this, "This divice is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }


        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            boolean isSuccess =googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this,R.raw.twaza_style_map)
            );
            if (!isSuccess)
                Log.e("ERROR","Map style load failed");
        }
        catch (Resources.NotFoundException ex)
        {
            ex.printStackTrace();
        }
        mMap = googleMap;
        riderMark = mMap.addCircle(new CircleOptions()
                .center(new LatLng(riderlat,riderlng))
                .radius(50)
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f));
        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.driver_tbl));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(riderlat,riderlng),0.05f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendArriveNotification(customerId);
                btnstrattrips.setEnabled(true);

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    private void sendArriveNotification(String customerId) {
        Token token = new Token(customerId);
        Notification notification = new Notification("ArravedF",String.format("Twaba menye shaga ko imodoka mwasabye ihageze ",Common.currentUser.getFname()));
        Sender sender = new Sender(token.getToken(),notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success!=1)
                {
                    Toast.makeText(DriverTracking.this, "habayemo ikosa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void startLocationUpdate()
    {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED   )
        {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,  this);
    }
    private void displayLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED   )
            Common.mLastLocation =LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (Common.mLastLocation !=null)
        {

            final double latitude =Common.mLastLocation.getLatitude();
            final double longitude = Common.mLastLocation.getLongitude();
            if (driverMarker != null)
                driverMarker.remove();
            driverMarker=mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude))
                    .title("Twaza")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),17.0f));

            if (direction !=null)
                direction.remove();
            getDirection();
        }


        else {
            Log.d("ERROR","can not get your location");
        }
    }

    private void getDirection()
    {
        LatLng   currentPostion = new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude());
        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+currentPostion.latitude+","+currentPostion.longitude+"&"+
                    "destination="+riderlat+","+riderlng+"&"+"Key="+
                    getResources().getString(R.string.google_direction_api);

            Log.d("EDMTDEV",requestApi); //print URL for debug

            mService.getPath( requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response)
                        {

                            try {

                                new  parserTask().execute(response.body().toString());



                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t)
                        {
                            Toast.makeText(DriverTracking.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Common.mLastLocation = location;
        displayLocation();

    }

    private class parserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>> {
        ProgressDialog mDialog = new ProgressDialog(DriverTracking.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("mwihangane gakeya.......");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jobject;
            List<List<HashMap<String, String>>> routes =null;
            try {
                jobject = new JSONObject(strings[0]);
                DirectionsJSONParser parser = new  DirectionsJSONParser();
                routes = parser.parse(jobject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points = new ArrayList<LatLng>();;
            PolylineOptions lineOptions = new PolylineOptions();;
            lineOptions.width(10);
            lineOptions.color(Color.BLUE);
            MarkerOptions markerOptions = new MarkerOptions();
            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                mDialog.dismiss();

            }
            // Drawing polyline in the Google Map for the i-th route
            if(points.size()!=0)mMap.addPolyline(lineOptions);//to avoid crash
        }

    }

}




