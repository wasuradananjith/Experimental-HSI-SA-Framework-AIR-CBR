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
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.example.mapswarm.util.Grid;
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
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SwarmActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final double DEFAULT_CIRCLE_RADIUS = 5;
    private static final int CIRCLE_COLOUR_UNSELECTED = Color.GRAY;
    private static final int CIRCLE_COLOUR_SELECTED = Color.GREEN;
    private static final String DYNAMIC_OBSTACLE_ADDED_NOTIFICATION = "newGridCellsToAvoid";
    private static final String DYNAMIC_OBSTACLE_REMOVED_NOTIFICATION = "safeCells";
    private static final String DEACTIVATED_CUBOIDS_INFO = "deactivatedCuboids";
    private GoogleMap swarmMap;
    private SeekBar radiusSeekBar;
    private Switch switchView;
    private TextView messagesTextView;
    private PyObject coppeliaSimApi;
    private PyObject sim = null;
    private int simOffset = 6;
    private int simSize = 12;
    private boolean isSimStopped = false;
    private Map<String, ArrayList<Float>> locations;
    private LatLng bottomLeftLatLng = new LatLng(-35.293925, 149.166375);
    private LatLng bottomRightLatLng = new LatLng(-35.293925, 149.167633);
    private LatLng topLeftLatLng = new LatLng(-35.292894, 149.166375);
    private LatLng mapCentre = new LatLng(-35.293379, 149.167026);
    private HashMap<Float, String> regionMapping = new HashMap<>();
    private Point leftPointBound = null;
    private Point rightPointBound = null;
    private Double selectedCircleRadius = null;
    private LatLng selectedCircleLatLng = null;
    private Integer selectedCircleId = null;
    private int regionsCount = 0;
    private int screenWidth = 0;
    private float widthInMeters = 0;
    Handler handler = new Handler();
    Runnable updateMarker = new Runnable() {
        @Override
        public void run() {
            periodicWork();
            handler.postDelayed(this, 100);
        }
    };
    private Grid grid;
    private boolean showStaticObstacles = true;
    private ArrayList<Marker> robotPositions = new ArrayList<>();
    private HashMap<Integer, Circle> circlesList = new HashMap<>();
    private HashMap<Integer, String> messages = new HashMap<>();
    private int messagesCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swarm);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        messagesTextView = (TextView) findViewById(R.id.messagesTextView);
        radiusSeekBar = findViewById(R.id.radiusSeekBar);

        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int previousRadius = 0;
            Double previousSelectedCircleRadius = 0.0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                previousRadius = seekBar.getProgress();
                previousSelectedCircleRadius = selectedCircleRadius;
                for (Map.Entry<Integer, Circle> entry : circlesList.entrySet()) {
                    Circle circle = entry.getValue();
                    LatLng currentCircleLatLng = circle.getCenter();
                    if (isSelectedCircleLatLngEquals(currentCircleLatLng)) {
                        selectedCircleRadius = (double) progress;
                        circle.setRadius(selectedCircleRadius);
                    }
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                //seekBar.setProgress((int) Math.round(selectedCircleRadius));
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                int currentRadius = seekBar.getProgress();
                Toast.makeText(SwarmActivity.this, "Current radius: " + currentRadius,
                        Toast.LENGTH_SHORT).show();
                Boolean isUpdated = coppeliaSimApi.callAttr("updateCylinderRadius", sim,
                        selectedCircleId, mapDistanceToSimDistance(selectedCircleRadius)).toBoolean();
                if (isUpdated == null) {
                    Toast.makeText(SwarmActivity.this, "Failed to update the radius!",
                            Toast.LENGTH_SHORT).show();
                    seekBar.setProgress(previousRadius);
                    selectedCircleRadius = previousSelectedCircleRadius;
                }
            }
        });

        switchView = findViewById(R.id.showGridSwitch);
        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                grid.drawGrid();
            } else {
                grid.clearGrid();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        swarmMap = googleMap;
        generateRegionMapping();

        // Add a marker in UNSW Canberra basketball court and move the camera
//        swarmMap.addMarker(new MarkerOptions()
//                .position(mapCentre)
//                .title("Marker in UNSW Canberra Main Parade Ground"));
        swarmMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCentre, 19.2f));
        swarmMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        Python py = Python.getInstance();
        coppeliaSimApi = py.getModule("coppeliaSimApi");
        sim = coppeliaSimApi.callAttr("connect");
        Log.i("SIM: ", sim.toString());

        if (sim != null) {
            handler.post(updateMarker);
        }

        // Calculate width of the arena in meters
        float[] distances = new float[2];
        Location.distanceBetween(bottomLeftLatLng.latitude, bottomLeftLatLng.longitude,
                bottomRightLatLng.latitude, bottomRightLatLng.longitude, distances);
        widthInMeters = distances[0]; // 114.42835
        Log.i("SIM: widthInMeters", String.valueOf(widthInMeters));

//        Location.distanceBetween(bottomLeftLatLng.latitude, bottomLeftLatLng.longitude,
//                topLeftLatLng.latitude, topLeftLatLng.longitude, distances);
//        Log.i("SIM: heightInMeters", String.valueOf(distances[0]));
        calculateGraphicsDistances();

        swarmMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                // Add a circle to the space, upon a single click on the map
                CircleOptions circleOptions = new CircleOptions()
                        .center(latLng)
                        .radius(DEFAULT_CIRCLE_RADIUS)
                        .strokeWidth(10)
                        .strokeColor(CIRCLE_COLOUR_SELECTED)
                        .fillColor(Color.argb(128, 255, 0, 0))
                        .clickable(true);
                selectedCircleLatLng = latLng;
                selectedCircleRadius = circleOptions.getRadius();
                selectedCircleId = regionsCount;
                regionsCount += 1;
                float simCoordinates[] = latLngToSimCoordinates(latLng);

                boolean isCreated = coppeliaSimApi.callAttr("createCylinderRegion", sim,
                        selectedCircleId, simCoordinates[0], simCoordinates[1],
                        mapDistanceToSimDistance(DEFAULT_CIRCLE_RADIUS)).toBoolean();
                Circle newCircle = swarmMap.addCircle(circleOptions);
                if (isCreated) {
                    for (Map.Entry<Integer, Circle> entry : circlesList.entrySet()) {
                        entry.getValue().setStrokeColor(CIRCLE_COLOUR_UNSELECTED);
                    }
                    circlesList.put(selectedCircleId, newCircle);
                }

//                for (Map.Entry<Integer, CircleOptions> entry : circleOptionsList.entrySet()) {
//                    CircleOptions circleOptions = entry.getValue();
//                    LatLng currentCircleLatLng = circleOptions.getCenter();
//                    // Change the colour of the selected circle's stroke
//                    if (isSelectedCircleLatLngEquals(currentCircleLatLng)) {
//                        circleOptions.strokeColor(circleOptions.getStrokeColor() ^ 0x00ffffff);
//                        selectedCircleRadius = circleOptions.getRadius();
//                        selectedCircleId = entry.getKey();
//                    } else {
//                        circleOptions.strokeColor(Color.GREEN);
//                    }
//                    swarmMap.addCircle(circleOptions);
//                }
            }
        });

        swarmMap.setOnMapLongClickListener(latLng -> {
            Integer currentRegionId = isRegionInsideCircle(latLng);
            if (currentRegionId != null) {
                for (Map.Entry<Integer, Circle> entry : circlesList.entrySet()) {
                    if (!currentRegionId.equals(entry.getKey()))
                        entry.getValue().setStrokeColor(CIRCLE_COLOUR_UNSELECTED);
                }
                circlesList.get(currentRegionId).setStrokeColor(CIRCLE_COLOUR_SELECTED);
                deleteRegionPopup(currentRegionId);
            }

        });

        swarmMap.setOnCircleClickListener(circle -> {
            // If the clicked circle is already selected, deselect it
            if (isSelectedCircleLatLngEquals(circle.getCenter())) {
                selectedCircleLatLng = null;
                selectedCircleRadius = null;
                selectedCircleId = null;
                circle.setStrokeColor(CIRCLE_COLOUR_UNSELECTED);
            } else {
                // If the clicked circle is not already selected, select it
                selectedCircleLatLng = circle.getCenter();
                selectedCircleRadius = circle.getRadius();
                selectedCircleId = isRegionInsideCircle(selectedCircleLatLng);
                for (Map.Entry<Integer, Circle> entry : circlesList.entrySet()) {
                    if (!selectedCircleId.equals(entry.getKey()))
                        entry.getValue().setStrokeColor(CIRCLE_COLOUR_UNSELECTED);
                }
                circle.setStrokeColor(CIRCLE_COLOUR_SELECTED);
            }
        });

        swarmMap.setOnCameraMoveListener(this::calculateGraphicsDistances);

        grid = new Grid(bottomLeftLatLng, bottomRightLatLng, topLeftLatLng, swarmMap, 3, this);
        grid.initializeGrid();

        if (showStaticObstacles)
            drawRectangularObstacles();
    }

    private void calculateGraphicsDistances() {
        leftPointBound = swarmMap.getProjection().toScreenLocation(bottomLeftLatLng);
        rightPointBound = swarmMap.getProjection().toScreenLocation(bottomRightLatLng);
        screenWidth = rightPointBound.x - leftPointBound.x;
    }

    private void periodicWork() {
        for (Marker marker: robotPositions) {
            marker.remove();
        }

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
                ArrayList<Float> deactivatedCuboids = new ArrayList<>();
                if (locations.containsKey(DEACTIVATED_CUBOIDS_INFO)) {
                    deactivatedCuboids = locations.get(DEACTIVATED_CUBOIDS_INFO);
                    locations.remove(DEACTIVATED_CUBOIDS_INFO);
                }

                int count = 0;
                for (String key : locations.keySet()) {
                    if (key.equals(DYNAMIC_OBSTACLE_ADDED_NOTIFICATION)) {
                        messagesCount += 1;
                        messages.put(messagesCount, "Avoid region " + regionMapping.get(locations.get(key).get(0)) + " !");
                    } else if(key.equals(DYNAMIC_OBSTACLE_REMOVED_NOTIFICATION)) {
                        messagesCount += 1;
                        messages.put(messagesCount, "Region " + regionMapping.get(locations.get(key).get(0)) + " is safe now!");
                    } else {
                        Float iconColour = deactivatedCuboids.contains(Float.parseFloat(key))? BitmapDescriptorFactory.HUE_CYAN: BitmapDescriptorFactory.HUE_BLUE;
                        Marker marker = swarmMap.addMarker(new MarkerOptions()
                                .position(simCoordinatesToLatLng(new float[]{locations.get(key).get(0),
                                        locations.get(key).get(1)}))
                                .icon(BitmapDescriptorFactory.defaultMarker(iconColour))
                                .title("Cuboid" + count));
                        count += 1;
                        robotPositions.add(marker);
                        Log.i("SIM: Key", key);
                    }
                }
            }
        }

        // When a circle is not selected, disable the radius seek bar
        if (selectedCircleRadius == null) {
            radiusSeekBar.setEnabled(false);
        } else {
            // When a circle is selected, enable the radius seek bar and
            // set the progress to the selected circle's radius
            radiusSeekBar.setEnabled(true);
            radiusSeekBar.setProgress((int) Math.round(selectedCircleRadius));
        }
        updateMessagesOnScreen();
    }

    private void updateMessagesOnScreen() {
        String message = "";
        for (Map.Entry<Integer, String> entry : messages.entrySet()) {
            message += entry.getValue() + "\n";
        }
        messagesTextView.setText(message);
    }

    private void deleteRegionPopup(int regionId) {
        // Create the object of AlertDialog Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(SwarmActivity.this);

        // Set the message show for the Alert time
        builder.setMessage("Are you sure you want to delete the region " + regionId + "?");

        // Set Alert Title
        builder.setTitle("Delete Region Alert !");

        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
        builder.setCancelable(false);

        // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
            boolean isDeleted = coppeliaSimApi.callAttr("deleteCylinderRegion", sim,
                    regionId).toBoolean();
            if (isDeleted) {
                circlesList.get(regionId).remove();
                circlesList.remove(regionId);
                selectedCircleLatLng = null;
                selectedCircleRadius = null;
                selectedCircleId = null;
            } else {
                Toast.makeText(getApplicationContext(), "Region deletion unsuccessful!",
                        Toast.LENGTH_SHORT).show();
            }
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

    private void drawRectangularObstacles() {
        PyObject staticObstacleData = coppeliaSimApi.callAttr("getRectangularStaticObstacles", sim);
        float[][] rectangularStaticObstacles = staticObstacleData.toJava(float[][].class);
        for(float[] rectangle: rectangularStaticObstacles) {
            Log.i("SIM: rectangle", Arrays.toString(rectangle));
            float xWidth = rectangle[3];
            float yHeight = rectangle[4];
            float[] topLeftPoint = { rectangle[0] - xWidth/2,  rectangle[1] + yHeight/2 };
            float[] topRightPoint = { rectangle[0] + xWidth/2,  rectangle[1] + yHeight/2 };
            float[] bottomRightPoint = { rectangle[0] + xWidth/2,  rectangle[1] - yHeight/2 };
            float[] bottomLeftPoint = { rectangle[0] - xWidth/2,  rectangle[1] - yHeight/2 };
            swarmMap.addPolygon(new PolygonOptions()
                    .add(simCoordinatesToLatLng(topLeftPoint),
                            simCoordinatesToLatLng(topRightPoint),
                            simCoordinatesToLatLng(bottomRightPoint),
                            simCoordinatesToLatLng(bottomLeftPoint),
                            simCoordinatesToLatLng(topLeftPoint))
                    .strokeColor(Color.GRAY)
                    .fillColor(Color.GRAY));
        }
    }

    private void generateRegionMapping() {
        regionMapping.put(1.0F, "C1");
        regionMapping.put(2.0F, "B1");
        regionMapping.put(3.0F, "A1");
        regionMapping.put(4.0F, "A2");
        regionMapping.put(5.0F, "A3");
        regionMapping.put(6.0F, "B3");
        regionMapping.put(7.0F, "C3");
        regionMapping.put(8.0F, "C2");
        regionMapping.put(0.0F, "B2");
    }

    private boolean isSelectedCircleLatLngEquals(LatLng currentCircleLatLng) {
        return selectedCircleLatLng != null &&
                selectedCircleLatLng.latitude == currentCircleLatLng.latitude &&
                selectedCircleLatLng.longitude == currentCircleLatLng.longitude;
    }

    private float[] latLngToSimCoordinates(LatLng latLng) {
        Point screenPoint = swarmMap.getProjection().toScreenLocation(latLng);
        float simX = (((float) (screenPoint.x - leftPointBound.x) / screenWidth) * simSize)
                - simOffset;
        float simY = ((float) (leftPointBound.y - screenPoint.y) / screenWidth * simSize)
                - simOffset;
       return new float[]{simX, simY};
    }

    private LatLng simCoordinatesToLatLng(float[] simCordinates) {
        float xCordinate = (((simOffset + simCordinates[0]) / simSize) * screenWidth) + leftPointBound.x;
        float yCordinate = -(((simOffset + simCordinates[1]) / simSize) * screenWidth) + leftPointBound.y;
        LatLng latLng = swarmMap.getProjection().fromScreenLocation(new
                Point(Math.round(xCordinate), Math.round(yCordinate)));
        return latLng;
    }

    private Integer isRegionInsideCircle(LatLng latLng) {
        for (Map.Entry<Integer, Circle> entry : circlesList.entrySet()) {
            int regionId = entry.getKey();
            Circle circle = entry.getValue();
            LatLng center = circle.getCenter();
            float[] distance = new float[2];
            Location.distanceBetween(latLng.latitude, latLng.longitude, center.latitude,
                    center.longitude, distance);
            // If the long clicked point is inside a circle,
            // prompt the region deletion dialog
            if (distance[0] <= circle.getRadius()) {
                return regionId;
            }
        }
        return null;
    }

    private float mapDistanceToSimDistance(double mapDistance) {
        return (float) mapDistance * simSize / widthInMeters;
    }

    private Map<String, ArrayList<Float>> readLocationsFromJson(String jsonStringToBeRead) {
        Type mapOfStringObjectType = new TypeToken<Map<String, ArrayList<Float>>>() {
        }.getType();
        Gson gson = new Gson();
        return gson.fromJson(jsonStringToBeRead, mapOfStringObjectType);
    }
}