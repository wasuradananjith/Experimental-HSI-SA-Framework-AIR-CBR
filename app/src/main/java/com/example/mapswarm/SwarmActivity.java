package com.example.mapswarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.example.loadinganimation.LoadingAnimation;
import com.example.mapswarm.db.SQLiteManager;
import com.example.mapswarm.util.Grid;
import com.example.mapswarm.util.MyTimer;
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
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SwarmActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final double DEFAULT_CIRCLE_RADIUS = 5;
    private static final int CIRCLE_COLOUR_UNSELECTED = Color.GRAY;
    private static final int CIRCLE_COLOUR_SELECTED = Color.GREEN;
    private static final String DYNAMIC_OBSTACLE_ADDED_NOTIFICATION = "newGridCellsToAvoid";
    private static final String DYNAMIC_OBSTACLE_REMOVED_NOTIFICATION = "safeCells";
    private static final String DEACTIVATED_CUBOIDS_INFO = "deactivatedCuboids";
    private static final String DANGEROUS_REGION_TAG = "dangerous";
    private static final String ATTRACTIVE_REGION_TAG = "attractive";
    private GoogleMap swarmMap;
    private SeekBar radiusSeekBar;
    private Switch showGridSwitch;
    private Switch attractorSwitch;
    private Button simControlButton;
    private Button popUpBtn;
    private TextView timerTextView;
    private TextView messagesTextView;
    private PyObject coppeliaSimApi;
    private PyObject sim = null;
    private int simOffset = 60;
    private int simSize = 120;
    private long timeLeftInMilliseconds = 600000;
    private boolean isSimStopped = false;
    private boolean isSimStoppedByTimeout = false;
    private boolean isStaticObstaclesRetrieved = false;
    private boolean isTargetRegionRetrieved = false;
    private Map<String, ArrayList<Float>> locations;
    private LatLng bottomLeftLatLng = new LatLng(-35.287459, 149.172585);
    private LatLng bottomRightLatLng = new LatLng(-35.287459, 149.173901);
    private LatLng topLeftLatLng = new LatLng(-35.2863799728, 149.172585);
    private LatLng topRightLatLng = new LatLng(-35.2863799728, 149.173901);
    private LatLng mapCentre = new LatLng(-35.286930, 149.173255);
    private HashMap<Float, String> regionMapping = new HashMap<>();
    private Point leftPointBound = null;
    private Point rightPointBound = null;
    private Double selectedCircleRadius = null;
    private LatLng selectedCircleLatLng = null;
    private Integer selectedCircleId = null;
    private int regionsCount = 0;
    private int screenWidth = 0;
    private float widthInMeters = 0;
//    private long[] questionTimes = { timeLeftInMilliseconds - 120000,
//            timeLeftInMilliseconds - 300000, timeLeftInMilliseconds - 420000,
//            timeLeftInMilliseconds - 540000 }; // 8min, 5min, 3min, 0min
    private long[] questionTimes = { timeLeftInMilliseconds - 15000
            , timeLeftInMilliseconds - 30000, timeLeftInMilliseconds - 45000,
            timeLeftInMilliseconds - 60000}; // test times (15 second gaps)
    private boolean[] questionsAsked = { false, false, false, false};
    private int activityRound = 0;  // Number of times the user performed the same task
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
    private boolean attractorsEnabled = false;
    private ArrayList<Marker> robotPositions = new ArrayList<>();
    private HashMap<Integer, Marker> breadcrumbsList = new HashMap<>();
    private HashMap<Integer, Circle> circlesList = new HashMap<>();
    private HashMap<Integer, String> messages = new HashMap<>();
    private int messagesCount = 0;
    private MyTimer timer;
    private int questionRound = 0;  // Number of times the questionnaire was displayed during
                                    // one user activity
    int filterDataCount = 0;    // argument to compare the counts of the already asked
                                // questions when retrieving from the database
    private boolean fromPause = false;
    private LoadingAnimation loadingAnimation;
    private LoadingAnimation endingAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swarm);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        messagesTextView = findViewById(R.id.messagesTextView);
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
                String selectedCircleTag = (String) circlesList.get(selectedCircleId).getTag();
                Boolean isUpdated = null;
                if (selectedCircleTag.equals(DANGEROUS_REGION_TAG)) {
                    isUpdated = coppeliaSimApi.callAttr("updateDangerousRegionRadius", sim,
                            selectedCircleId, mapDistanceToSimDistance(selectedCircleRadius)).toBoolean();
                } else {
                    isUpdated = coppeliaSimApi.callAttr("updateAttractiveRegionRadius", sim,
                            selectedCircleId, mapDistanceToSimDistance(selectedCircleRadius)).toBoolean();
                }
                if (isUpdated == null) {
                    Toast.makeText(SwarmActivity.this, "Failed to update the radius!",
                            Toast.LENGTH_SHORT).show();
                    seekBar.setProgress(previousRadius);
                    selectedCircleRadius = previousSelectedCircleRadius;
                }
            }
        });

        showGridSwitch = findViewById(R.id.showGridSwitch);
        showGridSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                grid.drawGrid();
            } else {
                grid.clearGrid();
            }
        });

//        attractorSwitch = findViewById(R.id.attractorSwitch);
//        attractorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            attractorsEnabled = isChecked;
//        });

        timerTextView = findViewById(R.id.timerText);
        timer = new MyTimer(false, timeLeftInMilliseconds, timerTextView);

//        popUpBtn = findViewById(R.id.popUpBtn);
//        popUpBtn.setOnClickListener(view -> {
//            pauseSimulationForQuestions(0);
//        });

        loadingAnimation = findViewById(R.id.loadingAnim);
        endingAnimation = findViewById(R.id.endingAnim);

        timer.updateTimer();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        swarmMap = googleMap;
        generateRegionMapping();

        swarmMap.addPolygon(new PolygonOptions()
                .add(bottomLeftLatLng,
                       bottomRightLatLng,
                        topRightLatLng,
                        topLeftLatLng,
                        bottomLeftLatLng)
                        .strokeWidth(5)
                .strokeColor(Color.DKGRAY));

        swarmMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCentre, 19.2f));
        swarmMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        swarmMap.getUiSettings().setZoomControlsEnabled(true);

        Python py = Python.getInstance();
        coppeliaSimApi = py.getModule("coppeliaSimApi");
        sim = coppeliaSimApi.callAttr("connect");

        simControlButton = findViewById(R.id.simControlButton);
        simControlButton.setOnClickListener(view -> {
            if (simControlButton.getText().equals("Start")) {
                initializeQuestionBankAndStartSimulation();
            } else {
                stopSimulation();
            }
        });

        if (sim != null) {
            handler.post(updateMarker);
        }

        // Calculate width of the arena in meters
        float[] distances = new float[2];
        Location.distanceBetween(bottomLeftLatLng.latitude, bottomLeftLatLng.longitude,
                bottomRightLatLng.latitude, bottomRightLatLng.longitude, distances);
        widthInMeters = distances[0]; // 114.42835
//        Log.i("SIM: widthInMeters", String.valueOf(widthInMeters));
//
//        Location.distanceBetween(bottomLeftLatLng.latitude, bottomLeftLatLng.longitude,
//                topLeftLatLng.latitude, topLeftLatLng.longitude, distances);
//        Log.i("SIM: heightInMeters", String.valueOf(distances[0]));
        calculateGraphicsDistances();

        swarmMap.setOnMapClickListener(latLng -> {
            if (attractorsEnabled) {
                drawCircle(latLng, true);
            } else {
                drawCircle(latLng, false);
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
                deletePopup(currentRegionId, "Region");
            } else {
                Integer breadcrumbId = isCloserToBreadcrumb(latLng);
                if (breadcrumbId != null) {
                    deletePopup(breadcrumbId, "Breadcrumb");
                }
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

        swarmMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() != null && (Boolean) marker.getTag()) {
                Toast.makeText(getApplicationContext(), "Marker clicked!",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        swarmMap.setOnCameraMoveListener(this::calculateGraphicsDistances);

        grid = new Grid(bottomLeftLatLng, bottomRightLatLng, topLeftLatLng, swarmMap, 3, this);
        grid.initializeGrid();

        // draw the grid when the map is loaded for the first time
        grid.drawGrid();

        drawTargetRegion();

        //if (showStaticObstacles)
            //drawRectangularObstacles();
    }

    private void drawCircle(LatLng latLng, Boolean isAttractor) {
        float simCoordinates[] = latLngToSimCoordinates(latLng);
        boolean isCreated;
        int fillCircleColour;
        String tag;
        if (!isAttractor) {
            isCreated = coppeliaSimApi.callAttr("createDangerousRegion", sim,
                    regionsCount, simCoordinates[0], simCoordinates[1],
                    mapDistanceToSimDistance(DEFAULT_CIRCLE_RADIUS)).toBoolean();
            fillCircleColour = Color.argb(128, 255, 0, 0);
            tag = DANGEROUS_REGION_TAG;
        } else {
            isCreated = coppeliaSimApi.callAttr("createAttractiveRegion", sim,
                    regionsCount, simCoordinates[0], simCoordinates[1],
                    mapDistanceToSimDistance(DEFAULT_CIRCLE_RADIUS)).toBoolean();
            fillCircleColour = Color.argb(128, 0, 255, 255);
            tag = ATTRACTIVE_REGION_TAG;
        }
        if (isCreated) {
            CircleOptions circleOptions = new CircleOptions()
                    .center(latLng)
                    .radius(DEFAULT_CIRCLE_RADIUS)
                    .strokeWidth(10)
                    .strokeColor(CIRCLE_COLOUR_SELECTED)
                    .fillColor(fillCircleColour)
                    .clickable(true);
            selectedCircleLatLng = latLng;
            selectedCircleRadius = circleOptions.getRadius();
            selectedCircleId = regionsCount;
            regionsCount += 1;
            Circle newCircle = swarmMap.addCircle(circleOptions);
            newCircle.setTag(tag);
            for (Map.Entry<Integer, Circle> entry : circlesList.entrySet()) {
                entry.getValue().setStrokeColor(CIRCLE_COLOUR_UNSELECTED);
            }
            circlesList.put(selectedCircleId, newCircle);
        }
    }

//    private void drawBreadcrumb(LatLng latLng) {
//        float simCoordinates[] = latLngToSimCoordinates(latLng);
//
//        boolean isCreated = coppeliaSimApi.callAttr("createBreadcrumb", sim,
//                breadCrumbsCount, simCoordinates[0], simCoordinates[1]).toBoolean();
//        if (isCreated) {
//            Marker marker = swarmMap.addMarker(new MarkerOptions()
//                    .position(latLng)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
//                    .title("Breadcrumb " + breadcrumbsList.size() + 1));
//            marker.setTag(true);
//            breadcrumbsList.put(breadCrumbsCount, marker);
//            breadCrumbsCount += 1;
//        }
//    }


    private void calculateGraphicsDistances() {
        leftPointBound = swarmMap.getProjection().toScreenLocation(bottomLeftLatLng);
        rightPointBound = swarmMap.getProjection().toScreenLocation(bottomRightLatLng);
        screenWidth = rightPointBound.x - leftPointBound.x;
    }

    private void periodicWork() {
        if (!isSimStopped && timer.getTimeLeftInMilliseconds() <= 10000) {
            timerTextView.setTextColor(Color.RED);
            if (timer.getTimeLeftInMilliseconds() <= 1000) {
                isSimStopped = true;
                isSimStoppedByTimeout = true;
                Timer timer = new Timer();
                endingAnimation.setVisibility(View.VISIBLE);
                timer.schedule(new TimerTask() {
                    public void run() {
                        stopSimulation();
                    }
                }, 3000);
            }
        }

        // Popup the questionnaire in predefined times
        if (questionRound != 4 && timer.getTimeLeftInMilliseconds() <= questionTimes[questionRound]
                && !questionsAsked[questionRound]) {
            if (questionRound == 2) {
                filterDataCount += 1;
            }
            pauseSimulationForQuestions(filterDataCount);
            questionsAsked[questionRound] = true;
            questionRound += 1;
        }

        if (!isTargetRegionRetrieved) {
            drawTargetRegion();
        }
        if (!isStaticObstaclesRetrieved) {
            //drawRectangularObstacles();
        }
        for (Marker marker: robotPositions) {
            marker.remove();
        }

//        Log.i("SIM: Bottom Left x",Double.toString(leftPointBound.x));
//        Log.i("SIM: Bottom Left y",Double.toString(leftPointBound.y));
//        Log.i("SIM: Bottom Right x",Double.toString(rightPointBound.x));
//        Log.i("SIM: Bottom Right y",Double.toString(rightPointBound.y));

        PyObject pyObject = null;
        if (!isSimStoppedByTimeout) {
            // Check the status of the simulation
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
        } else {
            isSimStopped = true;
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
                        marker.setTag(false);
                        count += 1;
                        robotPositions.add(marker);
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

    private void deletePopup(int id, String type) {
        // Create the object of AlertDialog Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(SwarmActivity.this);

        // Set the message show for the Alert time
        builder.setMessage("Are you sure you want to delete the " + type.toLowerCase() + " " + id + "?");

        // Set Alert Title
        builder.setTitle("Delete " + type + " Alert !");

        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
        builder.setCancelable(false);

        // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setPositiveButton("Yes", (dialog, which) -> {
            if (type.equalsIgnoreCase("region")) {
                String tag = (String) circlesList.get(id).getTag();
                boolean isDeleted = false;
                if (tag.equals(DANGEROUS_REGION_TAG)) {
                    isDeleted = coppeliaSimApi.callAttr("deleteDangerousRegion", sim,
                            id).toBoolean();
                } else {
                    isDeleted = coppeliaSimApi.callAttr("deleteAttractiveRegion", sim,
                            id).toBoolean();
                }
                if (isDeleted) {
                    circlesList.get(id).remove();
                    circlesList.remove(id);
                    selectedCircleLatLng = null;
                    selectedCircleRadius = null;
                    selectedCircleId = null;
                } else {
                    Toast.makeText(getApplicationContext(), "Region deletion unsuccessful!",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                boolean isDeleted = coppeliaSimApi.callAttr("deleteBreadcrumb", sim,
                        id).toBoolean();
                if (isDeleted) {
                    breadcrumbsList.get(id).remove();
                    breadcrumbsList.remove(id);
                } else {
                    Toast.makeText(getApplicationContext(), "Breadcrumb deletion unsuccessful!",
                            Toast.LENGTH_SHORT).show();
                }
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

    private void drawTargetRegion() {
        PyObject targetRegionData = null;
        try {
            // Retrieve the target region
            targetRegionData = coppeliaSimApi.callAttr("getTargetRegion", sim);
            isSimStopped = false;
            isTargetRegionRetrieved = true;
            float[] targetRegion = targetRegionData.toJava(float[].class);
            float[] topLeftPoint = { targetRegion[1], targetRegion[2] };
            float[] topRightPoint = { targetRegion[0], targetRegion[2] };
            float[] bottomRightPoint = { targetRegion[0], targetRegion[3] };
            float[] bottomLeftPoint = { targetRegion[1], targetRegion[3] };
            swarmMap.addPolygon(new PolygonOptions()
                    .add(simCoordinatesToLatLng(topLeftPoint),
                            simCoordinatesToLatLng(topRightPoint),
                            simCoordinatesToLatLng(bottomRightPoint),
                            simCoordinatesToLatLng(bottomLeftPoint),
                            simCoordinatesToLatLng(topLeftPoint))
                    .strokeColor(Color.GREEN));
        } catch (PyException e) {
            if (e.getMessage() != null && e.getMessage().contains(" has already ended")) {
                Log.i("SIM: ", "Sim stopped.....");
                isSimStopped = true;
                isTargetRegionRetrieved = false;
            } else {
                throw e;
            }
        }
    }

    private void drawRectangularObstacles() {
        PyObject staticObstacleData = null;
        try {
            // Retrieve the rectangular obstacles
            staticObstacleData = coppeliaSimApi.callAttr("getRectangularStaticObstacles", sim);
            isSimStopped = false;
            isStaticObstaclesRetrieved = true;
            float[][] rectangularStaticObstacles = staticObstacleData.toJava(float[][].class);
            for(float[] rectangle: rectangularStaticObstacles) {
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
        } catch (PyException e) {
            if (e.getMessage() != null && e.getMessage().contains(" has already ended")) {
                Log.i("SIM: ", "Sim stopped.....");
                isSimStopped = true;
                isStaticObstaclesRetrieved = false;
            } else {
                throw e;
            }
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

    private Integer isCloserToBreadcrumb(LatLng latLng) {
        for (Map.Entry<Integer, Marker> entry : breadcrumbsList.entrySet()) {
            int breadcrumbId = entry.getKey();
            Marker breadcrumb = entry.getValue();
            float[] distance = new float[2];
            LatLng position = breadcrumb.getPosition();
            Location.distanceBetween(latLng.latitude, latLng.longitude, position.latitude,
                    position.longitude, distance);
            if (distance[0] <= 4) {
                return breadcrumbId;
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

    @Override
    public void onResume() {
        super.onResume();
        if (fromPause) {
            resumeAfterPause();
        }
    }

    public void initializeQuestionBankAndStartSimulation() {
        initializeQuestionBank();
        startSimulation();
    }

    public void startSimulation() {

        Integer state = coppeliaSimApi.callAttr("startSim", sim).toInt();
        if (state > 0) {
            simControlButton.setText("Stop");
            simControlButton.setBackgroundColor(Color.RED);
            loadingAnimation.setVisibility(View.GONE);
            timer.startStop();
        } else if (state == -1) {
            warningDialog("Error!", "Error when starting the simulation. " +
                    "Please contact the administrator...");
        } else if (state == 0) {
            warningDialog("Error!", "Operation could not be performed when starting " +
                    "the simulation. Please contact the administrator...");
        }
    }

    public void pauseSimulationForQuestions(int filterDataCount) {
        Integer state = coppeliaSimApi.callAttr("pauseSim", sim).toInt();
        Log.i("Log: pauseState ", state.toString());
        if (state > 0) {
            timer.startStop();
            fromPause = true;
            Timer timer = new Timer();
            loadingAnimation.setVisibility(View.VISIBLE);
            loadingAnimation.setTextMsg("Get ready for questions round " + (questionRound+1) + "...");
            timer.schedule(new TimerTask() {
                public void run() {
                    Intent intent = new Intent(SwarmActivity.this, QuestionnaireActivity.class);
                    intent.putExtra("filterDataCount", filterDataCount);
                    intent.putExtra("activityRound", activityRound);
                    intent.putExtra("questionRound", questionRound);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }, 3000);
        } else if (state == -1) {
            warningDialog("Error!", "Error when pausing the simulation. " +
                    "Please contact the administrator...");
        } else if (state == 0) {
            warningDialog("Error!", "Operation could not be performed when pausing " +
                    "the simulation. Please contact the administrator...");
        }
    }

    public void stopSimulation() {
        Integer state = coppeliaSimApi.callAttr("stopSim", sim).toInt();
        if (state > 0) {
            coppeliaSimApi.callAttr("stopSim", sim);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            timer.getCountDownTimer().cancel();
            finish();
        } else if (state == -1) {
            warningDialog("Error!", "Error when stopping the simulation. " +
                    "Please contact the administrator...");
        } else {
            warningDialog("Error!", "Operation could not be performed when stopping " +
                    "the simulation. Please contact the administrator...");
        }
    }

    private void resumeAfterPause() {
        startSimulation();
    }

    private void warningDialog(String title, String message) {
        // Create the object of AlertDialog Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(SwarmActivity.this);

        // Set the message show for the Alert time
        builder.setMessage(message);

        // Set Alert Title
        builder.setTitle(title);

        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
        builder.setCancelable(true);

        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();
        // Show the Alert Dialog box
        alertDialog.show();
    }

    /**
     * Initialise the question bank for the new round
     */
    public void initializeQuestionBank() {
        SQLiteManager sqLiteManager = new SQLiteManager(this);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        try {
            sqLiteManager.open();
            sqLiteManager.dropQuestionBankIfAlreadyExists();
            sqLiteManager.createQuestionBank();
            InputStream inputStream = getResources().openRawResource(R.raw.questions);
            sqLiteManager.insertQuestionBankData(inputStream);

            // Update the round number for the user
            if (currentUser != null) {
                activityRound = sqLiteManager.updateUserRound(currentUser.getEmail().split("@")[0]);
            }
            sqLiteManager.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}