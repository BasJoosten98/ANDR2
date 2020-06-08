package com.flamevision.findiro;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.flamevision.findiro.LoginAndSignup.TestLoginAndSignupActivity;
import com.flamevision.findiro.Profile.EditProfile_activity;
import com.flamevision.findiro.Profile.Login2_activity;
import com.flamevision.findiro.RealTimeLocation.RealTimeLocation;
import com.flamevision.findiro.UserAndGroup.Group;
import com.flamevision.findiro.UserAndGroup.SelectGroupFragment;
import com.flamevision.findiro.UserAndGroup.TestUserAndGroupActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, SelectGroupFragment.GroupReceiver {

    private final int USER_LOGIN_CODE = 1;

    private Button btnTestUserAndGroup;
    private Button btnTestLoginAndSignUp;
    private Button btnSelectGroup;

    //try new log in UI
    private Button btnTestTheo;

    //profile
    private Button btnProfile;

    private GoogleMap gm;
    LocationManager lm;
    private Marker m;


    private String userId = null;
    Fragment selectGroupFragment;

    final ArrayList<Group> groups = new ArrayList<>();

    RealTimeLocation realTimeLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();

        btnTestUserAndGroup = findViewById(R.id.mainTestUserAndGroupButton);
        btnTestUserAndGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, TestUserAndGroupActivity.class);
                startActivity(intent);
            }
        });

        btnTestLoginAndSignUp = findViewById(R.id.mainTestLoginAndSignup);
        btnTestLoginAndSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, TestLoginAndSignupActivity.class);
                startActivityForResult(intent, USER_LOGIN_CODE);
            }
        });

        //open new sign in
        btnTestTheo=findViewById(R.id.TestTheoSignin);
        btnTestTheo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, Login2_activity.class);
                startActivity(intent);
            }
        });

        //open profile
        btnProfile=findViewById(R.id.ProfileTest);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, EditProfile_activity.class);
                startActivity(intent);
            }
        });

        MapFragment mf = new MapFragment();
        getFragmentManager().beginTransaction().add(R.id.framelayout_main_fragmentcontainer, mf).commit();
        mf.getMapAsync(this);

        realTimeLocation = new RealTimeLocation();

        selectGroupFragment = new SelectGroupFragment(HomeActivity.this, realTimeLocation.getGroups());
        btnSelectGroup = findViewById(R.id.buttonSelectGroup);

        btnSelectGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().add(R.id.framelayout_main_fragmentcontainer, selectGroupFragment).commit();
            }
        });

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, this);

        //when app is launched, the user should become online (if logged in) in the database
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            DatabaseReference curUserOnlineRef = FirebaseDatabase.getInstance().getReference("Users/" + firebaseUser.getUid() + "/online");
            curUserOnlineRef.setValue(true);
            curUserOnlineRef.onDisconnect().setValue(false);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gm = googleMap;
        realTimeLocation.onMapReady(googleMap);

        Location location = null;

        Criteria criteria = new Criteria();
        String bestProvider = lm.getBestProvider(criteria, false);
        if (bestProvider != null)
            location = lm.getLastKnownLocation(bestProvider);

        LatLng coordinates;
        if (location != null)
            coordinates = new LatLng(location.getLatitude(), location.getLongitude());
        else
            coordinates = new LatLng(0, 0);

        m = gm.addMarker(new MarkerOptions().position(coordinates).title("My position"));
        gm.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));
    }

    @Override
    public void onLocationChanged(Location location) {
        if (m != null) {
            m.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            gm.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
            realTimeLocation.onLocationChanged(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted by the user
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, this);
                } else {
                    // permission was denied by the user
                }
                return;
        }
        // other 'case' lines to check for other permissions this app might request
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == USER_LOGIN_CODE) {
            if (resultCode == R.integer.LoggedIn) {
                userId = data.getStringExtra("userId");
                realTimeLocation.onLogin(userId);
            } else if (resultCode == R.integer.LoggedOut) {
                realTimeLocation.onLogout();
            }
        }
    }

    @Override
    public void GroupSelected(Group group) {
        realTimeLocation.groupSelected(group);
        getSupportFragmentManager().beginTransaction().remove(selectGroupFragment).commit();
    }
}