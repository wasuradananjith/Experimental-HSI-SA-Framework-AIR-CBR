package com.example.mapswarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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
    private List<CircleOptions> circleOptionsList = new ArrayList();
    private Circle selectedCircle = null;
    private LatLng selectedCircleLatLng = null;
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

        swarmMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                CircleOptions circleOptions = new CircleOptions()
                        .center(latLng)
                        .radius(10)
                        .strokeWidth(10)
                        .strokeColor(Color.GREEN)
                        .fillColor(Color.argb(128, 255, 0, 0))
                        .clickable(true);
                selectedCircleLatLng = latLng;
                circleOptionsList.add(circleOptions);
            }
        });

        swarmMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                for (int i = 0; i < circleOptionsList.size(); i++) {
                    LatLng center = circleOptionsList.get(i).getCenter();
                    float[] distance = new float[2];
                    Location.distanceBetween(latLng.latitude, latLng.longitude, center.latitude,
                            center.longitude, distance);
                    if( distance[0] <= circleOptionsList.get(i).getRadius() ){
                        deleteRegionPopup(i);
                        selectedCircleLatLng = center;
                    }
                }
            }
        });
    }

    private void periodicWork() {
        swarmMap.clear();
        leftPointBound = swarmMap.getProjection().toScreenLocation(bottomLeftLatLng);
        rightPointBound = swarmMap.getProjection().toScreenLocation(bottomRightLatLng);

        int width = rightPointBound.x - leftPointBound.x;
//        Log.i("SIM: WIDTH",Double.toString(width)); // 1193
//        Log.i("SIM: Bottom Left x",Double.toString(leftPointBound.x));
//        Log.i("SIM: Bottom Left y",Double.toString(leftPointBound.y));
//        Log.i("SIM: Bottom Right x",Double.toString(rightPointBound.x));
//        Log.i("SIM: Bottom Right y",Double.toString(rightPointBound.y));

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

//                    Log.i("Sim x", Double.toString(xCordinate));
//                    Log.i("Sim y", Double.toString(yCordinate));
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

        for (CircleOptions circleOptions: circleOptionsList) {
            LatLng currentCircleLatLng = circleOptions.getCenter();
            if (isSelectedCircleLatLngEquals(currentCircleLatLng))
                circleOptions.strokeColor(circleOptions.getStrokeColor() ^ 0x00ffffff);
            else
                circleOptions.strokeColor(Color.GREEN);
            swarmMap.addCircle(circleOptions);
        }
        swarmMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                if (selectedCircleLatLng == null)
                    selectedCircleLatLng = circle.getCenter();
                else
                    selectedCircleLatLng = null;
            }
        });
    }

    private void deleteRegionPopup(int index){
        // Create the object of AlertDialog Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(SwarmActivity.this);

        // Set the message show for the Alert time
        builder.setMessage("Are you sure you want to delete the region " +index + "?");

        // Set Alert Title
        builder.setTitle("Delete Region Alert !");

        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
        builder.setCancelable(false);

        // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
            circleOptionsList.remove(index);
            selectedCircleLatLng = null;
        });

        // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
            // If user click no then dialog box is canceled.
            dialog.cancel();
        });

        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();
        // Show the Alert Dialog box
        alertDialog.show();
    }

    private boolean isSelectedCircleLatLngEquals(LatLng currentCircleLatLng) {
        return selectedCircleLatLng != null &&
                selectedCircleLatLng.latitude == currentCircleLatLng.latitude &&
                selectedCircleLatLng.longitude == currentCircleLatLng.longitude;
    }
    public Map<String, ArrayList<Float>> readLocationsFromJson(String jsonStringToBeRead) {
        Type mapOfStringObjectType = new TypeToken<Map<String, ArrayList<Float>>>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(jsonStringToBeRead, mapOfStringObjectType);
    }
}