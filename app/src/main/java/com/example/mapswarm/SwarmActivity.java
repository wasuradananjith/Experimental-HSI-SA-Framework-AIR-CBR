package com.example.mapswarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SwarmActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap swarmMap;
    private PyObject coppeliaSimApi;
    private PyObject sim = null;
    private boolean isSimStopped = false;
    private Map<String, ArrayList<Float>> locations;
    private LatLng bottomLeftLatLng = new LatLng(-35.290891, 149.168508);
    private LatLng bottomRightLatLng = new LatLng(-35.290891, 149.169253);
    private LatLng basketBallCourt = new LatLng(-35.290575, 149.168852);
    private Point leftPointBound = null;
    private Point rightPointBound = null;
    Handler handler = new Handler();
    Runnable updateMarker = new Runnable() {
        @Override
        public void run() {
            periodicWork();
            handler.postDelayed(this, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swarm);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        swarmMap = googleMap;

        // Add a marker in UNSW Canberra basketball court and move the camera
        swarmMap.addMarker(new MarkerOptions()
                .position(basketBallCourt)
                .title("Marker in UNSW Canberra Basketball court"));
        swarmMap.moveCamera(CameraUpdateFactory.newLatLngZoom(basketBallCourt, 20.1f));
        swarmMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        Python py = Python.getInstance();
        coppeliaSimApi = py.getModule("coppeliaSimApi");
        sim = coppeliaSimApi.callAttr("connect");
        Log.i("SIM: ", sim.toString());

        if (sim != null)
            handler.post(updateMarker);
    }

    private void periodicWork() {
        swarmMap.clear();
        leftPointBound = swarmMap.getProjection().toScreenLocation(bottomLeftLatLng);
        rightPointBound = swarmMap.getProjection().toScreenLocation(bottomRightLatLng);

        int width = rightPointBound.x - leftPointBound.x;
        Log.i("SIM: WIDTH",Double.toString(width)); // 1193
        Log.i("SIM: Bottom Left x",Double.toString(leftPointBound.x));
        Log.i("SIM: Bottom Left y",Double.toString(leftPointBound.y));
        Log.i("SIM: Bottom Right x",Double.toString(rightPointBound.x));
        Log.i("SIM: Bottom Right y",Double.toString(rightPointBound.y));

        // Check the status of the simulation
        PyObject pyObject = null;
        try {
            // Retrieve the locations of the robots
            pyObject = coppeliaSimApi.callAttr("getCuboidsLocations", sim);
            isSimStopped = false;
        } catch (PyException e) {
            if (e.getMessage() != null && e.getMessage().contains(" has already ended")) {
                Log.i("SIM: ", "Sim stopped.....");
                isSimStopped = true;
            } else {
                throw e;
            }
        }

        if (!isSimStopped) {
            locations = readLocationsFromJson(pyObject.toString());

            // Drawing the cuboids
            if (locations.size() != 0) {
                int count = 0;
                for (String key : locations.keySet()) {
                    float xCordinate = (((6 + locations.get(key).get(0)) / 12) * width) + leftPointBound.x;
                    float yCordinate = -(((6 + locations.get(key).get(1)) / 12) * width) + leftPointBound.y;

                    Log.i("Sim x", Double.toString(xCordinate));
                    Log.i("Sim y", Double.toString(yCordinate));
                    LatLng latLng = swarmMap.getProjection().fromScreenLocation(new
                            Point(Math.round(xCordinate),Math.round(yCordinate)));
                    swarmMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .title("Cuboid" + count));
                    count += 1;
                }
            }
        }
    }

    public Map<String, ArrayList<Float>> readLocationsFromJson(String jsonStringToBeRead) {
        Type mapOfStringObjectType = new TypeToken<Map<String, ArrayList<Float>>>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(jsonStringToBeRead, mapOfStringObjectType);
    }
}