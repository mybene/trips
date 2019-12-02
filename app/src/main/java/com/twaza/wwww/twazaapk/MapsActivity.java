package com.twaza.wwww.twazaapk;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.twaza.wwww.twazaapk.Common.Common;

import com.twaza.wwww.twazaapk.R;
import com.twaza.wwww.twazaapk.Remote.IGoogleAPI;
import com.twaza.wwww.twazaapk.mode.Token;

import com.google.android.gms.location.LocationListener;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.maps.android.SphericalUtil;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,

        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener

{

    private GoogleMap mMap;
    private static final int My_PERMISION_REQUESTY_CODE =7000;
    private static  final int  PLAY_SERVICE_RES_REQUEST =7001;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Button sendsms;
    public static int UPDATE_INTERVAL =5000;
    public static  int FATEST_INTERVAL =3000;
    public static int DISOLACEMENT =10;
    public FirebaseAuth Auth;
    public  DatabaseReference drivar;
     public GeoFire geoFire;
     Marker mCurrent;
    private List<LatLng> polyLineList;
    private Marker carMaker;
    private float v;
    private  double lat,lng;
    private  LatLng startPostion ,endpostion ,currentPostion;
    private Handler hander;
    private int index;
    private int next;
    private PlaceAutocompleteFragment  places;
    AutocompleteFilter typeFilter;
    private String destinationt;
    private PolylineOptions polylineOptions,blackpolylineoption;
    private Polyline blackpolyline,grayPoline;
    private IGoogleAPI mService;
    DatabaseReference onlineRef ,currentUserRef;



    Runnable drawpathRunnable = new Runnable() {
        @Override
        public void run() {
            if (index<polyLineList.size()-1)
            {
                index++;
                next =index+1;
            }
            if (index < polyLineList.size()-1)
            {
                startPostion = polyLineList.get(index);
                endpostion = polyLineList.get(next);
            }
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator( new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
            {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator valueAnimator)
                                    {
                                         v = valueAnimator.getAnimatedFraction();
                                         lng = v*endpostion.longitude+(1-v)*startPostion.longitude;
                                        lat= v*endpostion.latitude+(1-v)*startPostion.latitude;
                                         LatLng newpos = new LatLng(lat,lng);
                                        carMaker.setPosition(newpos);
                                        carMaker.setAnchor(0.5f,0.5f);
                                        carMaker.setRotation(getBearing(startPostion,newpos));
                                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                        new CameraPosition.Builder()
                                        .target(newpos)
                                        .zoom(15.5f)
                                        .build()
                                         ));
                                    }
              });
            valueAnimator.start();
            hander.postDelayed(this,3000);

        }
    };

    private float getBearing(LatLng startPostion, LatLng endpostion)
    {
        double lat = Math.abs(startPostion.latitude-endpostion.latitude);
        double lng = Math.abs(startPostion.longitude-endpostion.longitude);

        if (startPostion.latitude < endpostion.latitude && startPostion.longitude < endpostion.longitude)

            return (float)(Math.toDegrees(Math.atan(lng/lat)));

        else  if (startPostion.latitude >= endpostion.latitude&&startPostion.longitude < endpostion.longitude)
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+90);

        else if (startPostion.latitude >= endpostion.latitude&&startPostion.longitude >= endpostion.longitude)
            return (float)(Math.toDegrees(Math.atan(lng/lat))+180);

        else if (startPostion.latitude < endpostion.latitude&&startPostion.longitude >= endpostion.longitude)
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+270);
        return -1;
    }


    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        sendsms =(Button)findViewById(R.id.smssend);
        mapFragment.getMapAsync(this);
       final Handler hander = new Handler();


        sendsms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri SMS_URI = Uri.parse("smsto:+250788522501"); //Replace the phone number
                Intent sms = new Intent(Intent.ACTION_VIEW,SMS_URI);
                sms.putExtra("sms_body","This is test message"); //Replace the message witha a vairable
                startActivity(sms);

            }
        });
               onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected ");
               currentUserRef =FirebaseDatabase.getInstance().getReference(Common.driver_tbl)
                       .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

               onlineRef.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       currentUserRef.onDisconnect().removeValue();
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {

                   }
               });



        location_switch =(MaterialAnimatedSwitch)findViewById(R.id.location_switch);
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isonline) {
                if (isonline)
                {
                    FirebaseDatabase.getInstance().goOnline();

                    startLocationUpdate();
                    displayLocation();
                    Snackbar.make(mapFragment.getView(),"you are online",Snackbar.LENGTH_SHORT)
                            .show();
                }
                else
                {
                    FirebaseDatabase.getInstance().goOffline();
                    stopLocationUpDate();
                    mCurrent.remove();
                    mMap.clear();
                   hander.removeCallbacks(drawpathRunnable);
                    Snackbar.make(mapFragment.getView(),"you are offline",Snackbar.LENGTH_SHORT)
                            .show();
                }

            }
        });
        polyLineList = new ArrayList<>();

        typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();

        places = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (location_switch.isChecked())
                {
                    destinationt = place.getAddress().toString();
                    destinationt =destinationt.replace("","+");

                    getDirection();
                }
                else
                {
                    Toast.makeText(MapsActivity.this, "plase change your stutas online", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onError(Status status) {
 Toast.makeText(MapsActivity.this,""+status.toString(),Toast.LENGTH_SHORT).show();

            }
        });


        drivar = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        geoFire = new GeoFire(drivar);

        setUpLocation();

        mService = Common.getGoogleApi();
        updateFirebaseToken();
    }

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();

        DatabaseReference tokens =db.getReference(Common.tokeni_tbl);

        Token token= new Token(FirebaseInstanceId.getInstance().getToken());
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token);
    }

    private void getDirection()
    {
        currentPostion = new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude());
        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+currentPostion.latitude+","+currentPostion.longitude+"&"+
                    "destination="+destinationt+"&"+"Key="+
                    getResources().getString(R.string.google_direction_api);

            Log.d("EDMTDEV",requestApi); //print URL for debug

            mService.getPath( requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response)
                        {

                            try {


                                JSONObject jsonObject= new JSONObject(response.body().toString());
                                JSONArray jsonArray =jsonObject.getJSONArray("routes");
                                for (int i=0; i<jsonArray.length();i++)
                                {
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly =route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polyLineList = decodePoly(polyline);
                                }

                              LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                for (LatLng LatLng:polyLineList)
                                    builder.include(LatLng);
                                LatLngBounds bounds = builder.build();
                                CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
                                mMap.animateCamera(mCameraUpdate);

                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.GRAY);
                                polylineOptions.width(5);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.endCap(new SquareCap());
                                polylineOptions.jointType(JointType.ROUND);
                                polylineOptions.addAll(polyLineList);
                                grayPoline = mMap.addPolyline(polylineOptions);


                                blackpolylineoption = new PolylineOptions();
                                blackpolylineoption.color(Color.BLACK);
                                blackpolylineoption.width(5);
                                blackpolylineoption.startCap(new SquareCap());
                                blackpolylineoption.endCap(new SquareCap());
                                blackpolylineoption.jointType(JointType.ROUND);
                                blackpolyline = mMap.addPolyline(blackpolylineoption);

                                mMap.addMarker(new MarkerOptions()
                                        .position(polyLineList.get(polyLineList.size()-1))
                                     .title("pick Location"));

//                                ANIMATION
                                ValueAnimator polyLinesAnimater = ValueAnimator.ofInt(0,100);
                                polyLinesAnimater.setDuration(2000);
                                polyLinesAnimater.setInterpolator(new LinearInterpolator());
                                polyLinesAnimater.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                        List<LatLng> points = grayPoline.getPoints();
                                        int percentValue = (int)valueAnimator.getAnimatedValue();
                                        int size = points.size();
                                        int newpoints  = (int)(size*(percentValue/100.0f));
                                        List<LatLng> p = points.subList(0,newpoints);
                                        blackpolyline.setPoints(p);



                                    }
                                });


                                polyLinesAnimater.start();
                                carMaker = mMap.addMarker(new MarkerOptions().position(currentPostion)
                                        .flat(true)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                                        hander = new Handler();
                                        index = -1;
                                        next =1;
                                        hander.postDelayed(drawpathRunnable,3000);



                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t)
                        {
                            Toast.makeText(MapsActivity.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
//    Because we request  runtime permissin ,we need override on Request permistion methods


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case My_PERMISION_REQUESTY_CODE:
                if (grantResults.length> 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED)
                {

                    if (ckeckplayServices())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
                        if (location_switch.isChecked())
                            displayLocation();
                    }

                }
                break;
        }
    }

    private void setUpLocation()

    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
//            request  runtime permission
            ActivityCompat.requestPermissions( this , new String[]
                    {

                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION

                    },My_PERMISION_REQUESTY_CODE);

        }
        else
        {
            if (ckeckplayServices())
            {
                buildGoogleApiClient();
                createLocationRequest();
                if (location_switch.isChecked())
                    displayLocation();
            }
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

    private void stopLocationUpDate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED   )
        {

            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }

    private void displayLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED   )
                Common.mLastLocation =LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (Common.mLastLocation !=null)
        {
            if (location_switch.isChecked())
            {
                final double latitude =Common.mLastLocation.getLatitude();
                final double longitude = Common.mLastLocation.getLongitude();

                //up date to fire base twazafirebase
                LatLng  Center = new LatLng(latitude,longitude);
                LatLng northside = SphericalUtil.computeOffset(Center,100000,0);
                LatLng Southside = SphericalUtil.computeOffset(Center,100000,180);
                LatLngBounds bounds= LatLngBounds.builder()
                        .include(northside)
                        .include(Southside)
                        .build();
                places.setBoundsBias(bounds);
                places.setFilter(typeFilter);


                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error)
                    {
//                         add marker
                        if (mCurrent != null)
                            mCurrent.remove();// remove already mark
                        mCurrent = mMap.addMarker(new MarkerOptions()
//
                                .position(new LatLng(latitude,longitude))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                .title("TWAZA Driver YOUR LOCATION"));

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(latitude,longitude),15.0f));

                    }
                });
//                Log.d("EDMTDEV",String.format("your location was changed:%f/%f",latitude,longitude));
            }

        }
            else {
            Log.d("ERROR","can not get your location");
        }
    }

    private void rotateMarker(final Marker mCurrent, final float i, GoogleMap mMap)
    {
        final Handler hander = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = mCurrent.getRotation();
        final long duration = 1500;

        final LinearInterpolator interpolator  = new LinearInterpolator();
        hander.post(new Runnable() {
            @Override
            public void run()
            {
                long elapsed  = SystemClock.uptimeMillis() -start;
                float t = interpolator.getInterpolation((float)elapsed/duration);
                float rot = t*i+(1-t)*startRotation;
                mCurrent.setRotation(-rot>180?rot/2:rot);
                if (t<1.0)
                {
                    hander.postDelayed(this,16);
                }

            }
        });
    }

    private void startLocationUpdate()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED   )
        {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,  this);
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onLocationChanged(Location location)
    {
        Common.mLastLocation = location;
        displayLocation();

    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();

    }

    @Override
    public void onConnectionSuspended(int i)
    {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.mai_menu,null);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logoubotton)
        {
            Auth.signOut();

        }
        return true;
    }
}
