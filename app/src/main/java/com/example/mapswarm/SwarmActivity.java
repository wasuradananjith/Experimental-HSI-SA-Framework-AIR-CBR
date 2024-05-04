package com.example.mapswarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Spannable;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
import com.example.mapswarm.util.RobotTrappedInf;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
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

    private static final int SQUARE_COLOUR_UNSELECTED = Color.argb(0, 255, 0, 0);
    private static final int SQUARE_COLOUR_SELECTED = Color.GREEN;
    private static final String NOTIFICATION_MESSAGES = "messages";
    private static final String NOTIFICATION_MESSAGES_KEYS = "messagesKeys";
    private static final String DEACTIVATED_CUBOIDS_INFO = "deactivatedCuboids";
    private static final String TARGET_CELL_REACHED = "targetCellReached";
    private boolean targetCellReached = false;
    private float[] targetPosition;
    private String targetCell;
    private GoogleMap swarmMap;
    private Switch mapLockSwitch;
    private Button simControlButton;
    private TextView timerTextView;
    public static PyObject coppeliaSimApi;
    public static PyObject sim = null;
    public static int simOffset = 60;
    public static int simSize = 120;
    private int swipeRadius = 8;
    private int swipeForceStrength = 1000;
    private long timeLeftInMilliseconds = 300000;
    private int trappedDuration = 10000;
    private double trappedRange = 1;
    private boolean isSimStopped = false;
    private boolean isSimStoppedByTimeout = false;
    private boolean isTargetRegionRetrieved = false;
    private Map<String, ArrayList<Object>> locations;
    public static LatLng bottomLeftLatLng = new LatLng(-35.287459, 149.172585);
    public static LatLng bottomRightLatLng = new LatLng(-35.287459, 149.173901);
    public static LatLng topLeftLatLng = new LatLng(-35.2863799728, 149.172585);
    public static LatLng topRightLatLng = new LatLng(-35.2863799728, 149.173901);
    public static LatLng mapCentre = new LatLng(-35.286918, 149.173240);
    private Point leftPointBound = null;
    private Point rightPointBound = null;
    private Point bottomPointBound = null;
    private Point topPointBound = null;
    private String selectedSquareName = null;
    private Integer selectedSquareId = null;
    private int regionsCount = 1;
    private int screenWidth = 0;
    public static float widthInMeters = 0;
//    private long[] questionTimes = { timeLeftInMilliseconds - 120000,
//            timeLeftInMilliseconds - 300000, timeLeftInMilliseconds - 420000,
//            timeLeftInMilliseconds - 540000 }; // 8min, 5min, 3min, 0min
//    private long[] questionTimes = { timeLeftInMilliseconds - 10000
//        , timeLeftInMilliseconds - 20000}; // test times (10 second gaps)

   //private long[] questionTimes = { timeLeftInMilliseconds - 120000
   //         , timeLeftInMilliseconds - 270000}; // test times (60 second gaps)

    private long[] questionTimes = {};
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
    private ArrayList<Marker> robotPositions = new ArrayList<>();
    HashMap<String, RobotTrappedInf> robotTrappedInfs = new HashMap<>();
    private HashMap<Integer, Polygon> squaresList = new HashMap<>();
    private HashMap<String, String> messages = new HashMap<>();
    private ArrayList<String> messageList = new ArrayList<>();
    private RecyclerView messageView;
    private MessageListAdapter messageListAdapter;
    private int messagesCount = 0;
    private MyTimer timer;
    private int questionRound = 0;  // Number of times the questionnaire was displayed during
                                    // one user activity
    int filterDataCount = 0;    // argument to compare the counts of the already asked
                                // questions when retrieving from the database
    private boolean fromPause = false;
    private boolean fromQuestionPause = false;
    private LoadingAnimation loadingAnimation;
    private LoadingAnimation endingAnimation;
    private boolean sameInterval = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swarm);

        OverlayMapFragment supportMapFragment = (OverlayMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        messageView = findViewById(R.id.messagesTextView);
        messageListAdapter = new MessageListAdapter(messageList, this);
        messageView.setAdapter(messageListAdapter);

//        mapLockSwitch = findViewById(R.id.mapLockSwitch);
//        mapLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked) {
//                swarmMap.getUiSettings().setScrollGesturesEnabled(false);
//            } else {
//                swarmMap.getUiSettings().setScrollGesturesEnabled(true);
//            }
//        });

        timerTextView = findViewById(R.id.timerText);
        timer = new MyTimer(false, timeLeftInMilliseconds, timerTextView);

        loadingAnimation = findViewById(R.id.loadingAnim);
        endingAnimation = findViewById(R.id.endingAnim);

        timer.updateTimer();

        supportMapFragment.setOnFlingListener(new OverlayMapFragment.OnFlingListener() {
            @Override
            public void onFling(float x1, float y1, float x2, float y2) {
                if (isWithinBounds(new Point((int) x1, (int) y1))) {
                    float[] startPos = screenPointToSimCoordinates(x1, y1);
                    float[] endPos = screenPointToSimCoordinates(x2, y2);
                    coppeliaSimApi.callAttr("createSwipeForce", sim, startPos[0], startPos[1],
                            endPos[0], endPos[1]);
                    drawCircle(simCoordinatesToLatLng(startPos));
                }
            }
        });

        supportMapFragment.setOnTouchListener(new OverlayMapFragment.OnTouchListener() {
            @Override
            public void onTouch(MotionEvent event) {
                float[] touchPoint = screenPointToSimCoordinates(event.getX(), event.getY());
                markSquare(simCoordinatesToLatLng(touchPoint));
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        swarmMap = googleMap;

        swarmMap.addPolygon(new PolygonOptions()
                .add(bottomLeftLatLng,
                       bottomRightLatLng,
                        topRightLatLng,
                        topLeftLatLng,
                        bottomLeftLatLng)
                        .strokeWidth(5)
                .strokeColor(Color.DKGRAY));

        swarmMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCentre, 19.35f));
        swarmMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        swarmMap.getUiSettings().setZoomControlsEnabled(true);
        swarmMap.getUiSettings().setRotateGesturesEnabled(false);
        swarmMap.getUiSettings().setScrollGesturesEnabled(false);
        swarmMap.getUiSettings().setTiltGesturesEnabled(false);

        Python py = Python.getInstance();
        coppeliaSimApi = py.getModule("coppeliaSimApi");
        sim = coppeliaSimApi.callAttr("connect");

        simControlButton = findViewById(R.id.simControlButton);
        simControlButton.setOnClickListener(view -> {
            if (simControlButton.getText().equals("Start")) {
                initializeQuestionBankAndStartSimulation();
            } else if (simControlButton.getText().equals("Pause")) {
                simControlButton.setText("Resume");
                pauseSimulation();
            } else if (simControlButton.getText().equals("Resume")) {
                simControlButton.setText("Pause");
                resumeSimulation();
            }
        });

        simControlButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return simControlOnLongPress();
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

        swarmMap.setOnMapLongClickListener(latLng -> {
            float[] simCoordinates = latLngToSimCoordinates(latLng);
            String cellName = grid.getCellName(simCoordinates);
            if (!cellName.equals("None")) {
                for (Map.Entry<Integer, Polygon> entry : squaresList.entrySet()) {
                    if (!cellName.equals(entry.getValue().getTag())) {
                        entry.getValue().setStrokeColor(SQUARE_COLOUR_UNSELECTED);
                    } else {
                        selectedSquareId = entry.getKey();
                        selectedSquareName = (String) entry.getValue().getTag();
                        entry.getValue().setStrokeColor(SQUARE_COLOUR_SELECTED);
                        deletePopup(selectedSquareId, selectedSquareName);
                    }
                }
            }
        });

        swarmMap.setOnPolygonClickListener(square -> {
            // If the clicked square is already selected, deselect it
            if (selectedSquareName == square.getTag()) {
                selectedSquareName = null;
                selectedSquareId = null;
                square.setStrokeColor(SQUARE_COLOUR_UNSELECTED);
            } else {
                // If the clicked square is not already selected, select it
                selectedSquareName = (String) square.getTag();
                for (Map.Entry<Integer, Polygon> entry : squaresList.entrySet()) {
                    if (selectedSquareName == entry.getValue().getTag()) {
                        selectedSquareId = entry.getKey();
                        entry.getValue().setStrokeColor(SQUARE_COLOUR_SELECTED);
                    } else {
                        entry.getValue().setStrokeColor(SQUARE_COLOUR_UNSELECTED);
                    }
                }
                square.setStrokeColor(SQUARE_COLOUR_SELECTED);
            }
        });

        swarmMap.setOnCameraMoveListener(this::calculateGraphicsDistances);
        swarmMap.setOnMarkerClickListener(marker -> true);

        grid = new Grid(bottomLeftLatLng, bottomRightLatLng, topLeftLatLng, swarmMap,
                12, simSize,simOffset, this);
        grid.initializeGrid();

        // draw the grid when the map is loaded for the first time
        grid.drawGrid();
        swarmMap.getUiSettings().setScrollGesturesEnabled(false);

        // To impose the actual image of the area from Google maps 2023 data
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.map))
                .position(mapCentre, widthInMeters, widthInMeters);
        swarmMap.addGroundOverlay(newarkMap);
    }

    private void markSquare(LatLng latLng) {
        float simCoordinates[] = latLngToSimCoordinates(latLng);
        float[][] cellBoundary = grid.getCellBoundary(simCoordinates);
        String cellName = grid.getCellName(simCoordinates);
        if (!cellName.equals("None")) {
            boolean alreadyMarked = false;
            for (Map.Entry<Integer, Polygon> entry : squaresList.entrySet()) {
                if (entry.getValue().getTag().equals(cellName)) {
                    alreadyMarked = true;
                    break;
                }
            }
            if (!alreadyMarked) {
                boolean isCreated;
                isCreated = coppeliaSimApi.callAttr("createDangerousRegion", sim,
                        regionsCount, cellBoundary[4][0], cellBoundary[4][1]).toBoolean();
                if (isCreated) {
                    PolygonOptions squareOptions = new PolygonOptions()
                            .add(simCoordinatesToLatLng(cellBoundary[0]),
                                    simCoordinatesToLatLng(cellBoundary[1]),
                                    simCoordinatesToLatLng(cellBoundary[3]),
                                    simCoordinatesToLatLng(cellBoundary[2]),
                                    simCoordinatesToLatLng(cellBoundary[0]))
                            .strokeWidth(10)
                            .clickable(true)
                            .fillColor(Color.argb(128, 255, 0, 0))
                            .strokeColor(SQUARE_COLOUR_SELECTED);
                    for (Map.Entry<Integer, Polygon> entry : squaresList.entrySet()) {
                        entry.getValue().setStrokeColor(SQUARE_COLOUR_UNSELECTED);
                    }
                    selectedSquareName = cellName;
                    selectedSquareId = regionsCount;
                    Polygon newSquare = swarmMap.addPolygon(squareOptions);
                    newSquare.setTag(cellName);
                    newSquare.setZIndex(10);
                    squaresList.put(selectedSquareId, newSquare);
                    regionsCount += 1;
                }
            }
        }
    }


    private void calculateGraphicsDistances() {
        leftPointBound = swarmMap.getProjection().toScreenLocation(bottomLeftLatLng);
        rightPointBound = swarmMap.getProjection().toScreenLocation(bottomRightLatLng);
        topPointBound = swarmMap.getProjection().toScreenLocation(topLeftLatLng);
        bottomPointBound = swarmMap.getProjection().toScreenLocation(bottomLeftLatLng);
        screenWidth = rightPointBound.x - leftPointBound.x;
    }

    private void periodicWork() {

        if (!isSimStopped && timer.getTimeLeftInMilliseconds() <= 10000) {
            timerTextView.setTextColor(Color.RED);
            if (timerTextView.getText().equals("0:00") || timer.getTimeLeftInMilliseconds() <= 1000) {
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
        if (questionTimes != null && questionTimes.length > 0 && questionRound != 2 && timer.getTimeLeftInMilliseconds() <= questionTimes[questionRound]
                && !questionsAsked[questionRound]) {
            if (questionRound == 2) {
                filterDataCount += 1;
            }
            pauseSimulationForQuestions(filterDataCount, timeLeftInMilliseconds - timer.getTimeLeftInMilliseconds());
            questionsAsked[questionRound] = true;
            questionRound += 1;
        }
        if (!isTargetRegionRetrieved) {
            drawTargetRegion();
        }

        if (questionTimes == null || questionTimes.length == 0) {
            // = getQuestionnaireTimes();
            questionTimes = new long[]{ timeLeftInMilliseconds - 10000, timeLeftInMilliseconds - 20000};
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

            if (locations.size() != 0) {
                // Mark the target cell if the target cell is reached by at least one robot
                if (locations.containsKey(TARGET_CELL_REACHED)) {
                    if (!targetCellReached && (Boolean) locations.get(TARGET_CELL_REACHED).get(0)) {
                        targetCellReached = true;
                        if (targetPosition != null) {
                            float[][] cellBoundary = grid.getCellBoundary(targetPosition);
                            PolygonOptions squareOptions = new PolygonOptions()
                                    .add(simCoordinatesToLatLng(cellBoundary[0]),
                                            simCoordinatesToLatLng(cellBoundary[1]),
                                            simCoordinatesToLatLng(cellBoundary[3]),
                                            simCoordinatesToLatLng(cellBoundary[2]),
                                            simCoordinatesToLatLng(cellBoundary[0]))
                                    .clickable(false)
                                    .strokeWidth(1)
                                    .strokeColor(Color.argb( 128, 0, 167, 0))
                                    .fillColor(Color.argb( 128, 0, 167, 0));
                            Polygon newSquare = swarmMap.addPolygon(squareOptions);
                            newSquare.setZIndex(9);
                        }
                    }
                    locations.remove(TARGET_CELL_REACHED);
                }

                // Retrieve the deactivated robots related information
                ArrayList<Object> deactivatedCuboids = new ArrayList<>();
                if (locations.containsKey(DEACTIVATED_CUBOIDS_INFO)) {
                    deactivatedCuboids = locations.get(DEACTIVATED_CUBOIDS_INFO);
                    locations.remove(DEACTIVATED_CUBOIDS_INFO);
                }

                // Mark the robots positions
                int count = 0;
                for (String key : locations.keySet()) {
                    if (key.equals(NOTIFICATION_MESSAGES) ||
                            key.equals(NOTIFICATION_MESSAGES_KEYS)) {
                        updateMessages(locations);
                    } else {
                        Float iconColour = isDeactivated(deactivatedCuboids, key) ?
                                BitmapDescriptorFactory.HUE_CYAN : BitmapDescriptorFactory.HUE_BLUE;
                        double[] robotPosition = new double[]{(double) locations.get(key).get(0),
                                (double) locations.get(key).get(1)};
                        if (iconColour.equals(BitmapDescriptorFactory.HUE_BLUE) && isRobotTrapped(key, robotPosition)) {
                            iconColour = BitmapDescriptorFactory.HUE_YELLOW;
                        }
                        Marker marker = swarmMap.addMarker(new MarkerOptions()
                                .position(simCoordinatesToLatLng(robotPosition))
                                .icon(BitmapDescriptorFactory.defaultMarker(iconColour))
                                // .icon(BitmapFromVector(getApplicationContext(), R.drawable.blue_marker))
                                .title("Cuboid" + count));
                        marker.setTag(key);
                        count += 1;
                        robotPositions.add(marker);
                    }
                }
            }
        }
    }
    private boolean isRobotTrapped(String key, double[] robotPosition) {
        RobotTrappedInf robotTrappedInf;
        boolean trapped = false;
        if (!robotTrappedInfs.containsKey(key)) {
            robotTrappedInf = new RobotTrappedInf(robotPosition, timer.getTimeLeftInMilliseconds());
            robotTrappedInfs.put(key, robotTrappedInf);
        } else {
            robotTrappedInf = robotTrappedInfs.get(key);
            long currentTime = timer.getTimeLeftInMilliseconds();
            if (robotTrappedInf.getLastRecordedTime() - currentTime >= trappedDuration || robotTrappedInf.isTrapped()) {
                double dist = euclideanDistance(robotPosition, robotTrappedInf.getPreviousPosition());
                if (robotTrappedInf.isTrapped() && dist <= trappedRange) {
                    trapped = true;
                } else if (!robotTrappedInf.isTrapped() && dist <= trappedRange) {
                    trapped = true;
                } else {
                    trapped = false;
                }

                if (trapped && targetCell.equals(grid.getCellName(robotPosition))) {
                    trapped = false;
                }

                if (!robotTrappedInf.isTrapped()) {
                    robotTrappedInf.setLastRecordedTime(currentTime);
                    robotTrappedInf.setPreviousPosition(robotPosition);
                    robotTrappedInf.setTrapped(trapped);
                    robotTrappedInfs.put(key, robotTrappedInf);
                }
            }
            return trapped;
        }
        robotTrappedInf.setTrapped(false);
        return false;
    }

    private void deletePopup(int id, String cellName) {
        // Create the object of AlertDialog Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(SwarmActivity.this);

        // Set the message show for the Alert time
        builder.setMessage("Are you sure you want to delete " + cellName + "?");

        // Set Alert Title
        builder.setTitle("Delete region " + " Alert !");

        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
        builder.setCancelable(false);

        // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setPositiveButton("Yes", (dialog, which) -> {
            boolean isDeleted = coppeliaSimApi.callAttr("deleteDangerousRegion", sim,
                    id).toBoolean();
            if (isDeleted) {
                squaresList.get(id).remove();
                squaresList.remove(id);
                selectedSquareName = null;
                selectedSquareId = null;
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

    private void drawTargetRegion() {
        PyObject targetRegionData = null;
        try {
            // Retrieve the target region
            targetRegionData = coppeliaSimApi.callAttr("getTargetPosition", sim);
            Log.i("Sim targetRegionData", String.valueOf(targetRegionData));
            isSimStopped = false;
            isTargetRegionRetrieved = true;
            targetPosition = targetRegionData.toJava(float[].class);
            targetCell = grid.getCellName(targetPosition);
            float[][] cellBoundary = grid.getNearestRandomCellBoundary(targetPosition);
            swarmMap.addPolygon(new PolygonOptions()
                    .add(simCoordinatesToLatLng(cellBoundary[0]),
                            simCoordinatesToLatLng(cellBoundary[1]),
                            simCoordinatesToLatLng(cellBoundary[3]),
                            simCoordinatesToLatLng(cellBoundary[2]),
                            simCoordinatesToLatLng(cellBoundary[0]))
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

    private long[] getQuestionnaireTimes() {
        long[] questionnaireTimes = null;
        try {
            // Retrieve the target region
            questionnaireTimes = coppeliaSimApi.callAttr("getQuestionnaireTimeSeeds", sim)
                    .toJava(long[].class);
        } catch (PyException e) {
            if (e.getMessage() != null && e.getMessage().contains(" has already ended")) {
                Log.i("SIM: ", "Sim stopped.....");
            } else {
                throw e;
            }
        }
        return questionnaireTimes;
    }

    /**
     * Check whether a point within the map bounds
     * @param point point to check
     * @return whether within the bounds or not
     */
    private boolean isWithinBounds(Point point) {
        return ((point.x <= rightPointBound.x && point.x >= leftPointBound.x) ||
                (point.y >= topPointBound.y && point.y <= bottomPointBound.y));
    }

    private float[] latLngToSimCoordinates(LatLng latLng) {
        Point screenPoint = swarmMap.getProjection().toScreenLocation(latLng);
        float simX = (((float) (screenPoint.x - leftPointBound.x) / screenWidth) * simSize)
                - simOffset;
        float simY = ((float) (leftPointBound.y - screenPoint.y) / screenWidth * simSize)
                - simOffset;
       return new float[]{simX, simY};
    }

    private float[] screenPointToSimCoordinates(float x, float y) {
        LatLng coord = swarmMap.getProjection().fromScreenLocation(new Point((int) x, (int) y));
        return latLngToSimCoordinates(coord);
    }

    private LatLng simCoordinatesToLatLng(float[] simCordinates) {
        float xCordinate = (((simOffset + simCordinates[0]) / simSize) * screenWidth) + leftPointBound.x;
        float yCordinate = -(((simOffset + simCordinates[1]) / simSize) * screenWidth) + leftPointBound.y;
        LatLng latLng = swarmMap.getProjection().fromScreenLocation(new
                Point(Math.round(xCordinate), Math.round(yCordinate)));
        return latLng;
    }

    private LatLng simCoordinatesToLatLng(double[] simCordinates) {
        double xCordinate = (((simOffset + simCordinates[0]) / simSize) * screenWidth) + leftPointBound.x;
        double yCordinate = -(((simOffset + simCordinates[1]) / simSize) * screenWidth) + leftPointBound.y;
        LatLng latLng = swarmMap.getProjection().fromScreenLocation(new
                Point((int) Math.round(xCordinate), (int) Math.round(yCordinate)));
        return latLng;
    }

    private Integer getSquareIdFromName(String squareName) {
        for (Map.Entry<Integer, Polygon> entry : squaresList.entrySet()) {
            if (entry.getValue().getTag() == squareName) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Map<String, ArrayList<Object>> readLocationsFromJson(String jsonStringToBeRead) {
        Type mapOfStringObjectType = new TypeToken<Map<String, ArrayList<Object>>>() {
        }.getType();
        Gson gson = new Gson();
        return gson.fromJson(jsonStringToBeRead, mapOfStringObjectType);
    }

    /**
     * Check whether a particular robot is deactivated
     * @param deactivatedCuboids the set of deactivated robots
     * @param robot the particular robot
     * @return
     */
    private boolean isDeactivated(ArrayList<Object> deactivatedCuboids, String robot) {
        if (deactivatedCuboids != null && deactivatedCuboids.size() != 0) {
            for (Object deactivatedCuboid: deactivatedCuboids) {
                if ((double)deactivatedCuboid == (Double.parseDouble(robot))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fromPause) {
            resumeSimulation();
        }
    }

    public void initializeQuestionBankAndStartSimulation() {
        initializeQuestionBank();
        startSimulation();
        drawTargetRegion();
    }

    public void startSimulation() {
        Integer state = coppeliaSimApi.callAttr("startSim", sim).toInt();
        if (state > 0) {
            simControlButton.setText("Pause");
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

    public void resumeSimulation() {
        Integer state = coppeliaSimApi.callAttr("resumeSim", sim).toInt();
        if (state > 0) {
            simControlButton.setText("Pause");
            simControlButton.setBackgroundColor(Color.RED);
            loadingAnimation.setVisibility(View.GONE);
            timer.startStop();
            fromPause = false;
            Toast.makeText(getApplicationContext(), "Task resumed!",
                    Toast.LENGTH_SHORT).show();
        } else if (state == -1) {
            warningDialog("Error!", "Error when resuming the simulation. " +
                    "Please contact the administrator...");
        } else if (state == 0) {
            warningDialog("Error!", "Operation could not be performed when resuming " +
                    "the simulation. Please contact the administrator...");
        }
    }

    public void pauseSimulationForQuestions(int filterDataCount, long questionRoundTime) {
        fromQuestionPause = true;
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
                    intent.putExtra("questionRoundTime", questionRoundTime/1000);
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

    public void pauseSimulation() {
        Integer state = coppeliaSimApi.callAttr("pauseSim", sim).toInt();
        Log.i("Log: pauseState ", state.toString());
        if (state > 0) {
            timer.startStop();
            fromPause = true;
            Toast.makeText(getApplicationContext(), "Task paused!",
                    Toast.LENGTH_SHORT).show();
        } else if (state == -1) {
            warningDialog("Error!", "Error when pausing the simulation. " +
                    "Please contact the administrator...");
        } else if (state == 0) {
            warningDialog("Error!", "Operation could not be performed when pausing " +
                    "the simulation. Please contact the administrator...");
        }
    }
    private boolean simControlOnLongPress() {
        if (simControlButton.getText().equals("Start")) {
            initializeQuestionBankAndStartSimulation();
            return true;
        }

        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Stop")
                .setMessage("Are you sure you want to stop the task?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (simControlButton.getText().equals("Resume")) {
                            resumeSimulation();
                        }
                        stopSimulation();
                        //finish();
                        //System.exit(0);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (simControlButton.getText().equals("Resume")) {
                            resumeSimulation();
                        }
                    }
                }).show();
        return true;
    }
    public void stopSimulation() {
        fromQuestionPause = true;
        coppeliaSimApi.callAttr("terminateSim", sim);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        timer.getCountDownTimer().cancel();
        finish();
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

    /**
     * Append text with colour
     * @param tv text view object
     * @param text  text content
     */
    private void appendColoredText(TextView tv, String text) {
        int start = tv.getText().length();
        tv.append(text);
        int end = tv.getText().length();

        Spannable spannableText = (Spannable) tv.getText();
        if (!sameInterval) {
            spannableText.setSpan(new AbsoluteSizeSpan(50), 0, start, 0); // set size
            spannableText.setSpan(new ForegroundColorSpan(Color.parseColor("#808080")),
                    0, start, 0);
            spannableText.setSpan(new AbsoluteSizeSpan(60), start, end, 0); // set size
            spannableText.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
            sameInterval = true;
        } else {
            spannableText.setSpan(new AbsoluteSizeSpan(60), start, end, 0); // set size
            spannableText.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
        }
    }

    /**
     * Update the message text in UI
     * @param regionsDataFromSim data from the simulation
     */
    private void updateMessages(Map<String, ArrayList<Object>> regionsDataFromSim) {
        String newMsg;
        ArrayList<Object> regionsDataValuesFromSimAsList = regionsDataFromSim.get(NOTIFICATION_MESSAGES);
        ArrayList<Object> regionsDataKeysFromSimAsList = regionsDataFromSim.get(NOTIFICATION_MESSAGES_KEYS);

        newMsg = "";
        for(int i = 0; i < regionsDataValuesFromSimAsList.size(); i++) {
            String readMsg = (String) regionsDataValuesFromSimAsList.get(i);
            if (!messages.containsKey(regionsDataKeysFromSimAsList.get(i))) {
                messages.put((String) regionsDataKeysFromSimAsList.get(i), readMsg);
                //appendColoredText(messagesTextView, "\n" + newMsg);
                if (newMsg.length() != 0)
                    newMsg = newMsg + "\n" + readMsg;
                else
                    newMsg = readMsg;
            }
        }
        if (newMsg.length() != 0) {
            messageList.add(newMsg);
            messageListAdapter.notifyDataSetChanged();
            messageView.smoothScrollToPosition(messageListAdapter.getItemCount() - 1);
        }
    }

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId)
    {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(
                context, vectorResId);

        // below line is use to set bounds to our vector
        // drawable.
        vectorDrawable.setBounds(
                0, 0, vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our
        // bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onBackPressed() {
        //
    }

    @Override
    protected void onPause() {
        if (!fromQuestionPause) {
            pauseSimulation();
        } else {
            fromQuestionPause = false;
        }
        super.onPause();
    }

    private void drawCircle(LatLng point){

        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();

        // Specifying the center of the circle
        circleOptions.center(point);

        // Radius of the circle
        circleOptions.radius(swipeRadius);

        // Border color of the circle
        circleOptions.strokeColor(Color.YELLOW);

        // Fill color of the circle
        circleOptions.fillColor(0x40FEFFBA);

        // Border width of the circle
        circleOptions.strokeWidth(2);

        // Adding the circle to the GoogleMap
        Circle circle = swarmMap.addCircle(circleOptions);
        circle.setZIndex(11);
        //swipeCircles.add(circle);
        CountDownTimer swipeCountDownTimer = new CountDownTimer(swipeForceStrength, 1000 /*Tick duration*/) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                circle.remove();
            }
        };
        swipeCountDownTimer.start();
    }

    private double euclideanDistance(double[] pos1, double[] pos2) {
        return Math.sqrt(Math.pow((pos1[0] - pos2[0]), 2) + Math.pow((pos1[1] - pos2[1]), 2));
    }
}
