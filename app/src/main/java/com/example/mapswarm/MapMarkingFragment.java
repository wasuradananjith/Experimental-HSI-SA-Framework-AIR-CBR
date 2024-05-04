package com.example.mapswarm;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mapswarm.model.Question;
import com.example.mapswarm.util.Grid;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link MapMarkingFragment} subclass
 */
public class MapMarkingFragment extends Fragment implements OnMapReadyCallback {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private final Question question;
    private final int questionNo;
    private final int totalQuestions;
    private final int questionId;
    private TextView questionText;
    private TextView questionNoText;
    private RadioButton specialAnswer;
    private RadioButton answer5;
    private RadioGroup radioGroup;
    private GoogleMap swarmMap;
    private Grid grid;
    private Point leftPointBound = null;
    private Point rightPointBound = null;
    private int screenWidth = 0;
    private static final int SQUARE_COLOUR_SELECTED = Color.GREEN;
    private static final int SQUARE_COLOUR_UNSELECTED = Color.argb(0, 255, 0, 0);
    private HashMap<Integer, Polygon> squaresList = new HashMap<>();
    private int markingsCount = 0;
    private Integer selectedSquareId = null;
    private String selectedSquareName = null;
    private View root;
    private static String ONE_CELL_ONLY = "You cannot mark more than one cells for this question!";

    public MapMarkingFragment(Question question, int questionNo, int totalQuestions, int questionId) {
        this.question = question;
        this.questionNo = questionNo;
        this.totalQuestions = totalQuestions;
        this.questionId = questionId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_map_drawing, container, false);
        questionText = root.findViewById(R.id.questionTxt);
        questionText.setText(question.getQuestionContent());
        radioGroup = root.findViewById(R.id.radioGroup);
        answer5 = root.findViewById(R.id.answer5);
        answer5.setText(question.getMcqAnswer5());

        specialAnswer = root.findViewById(R.id.specialAnswer);
        if (questionId == 18 || questionId == 21 || questionId == 23 || questionId == 25 || questionId == 27) {
            specialAnswer.setText("Not Applicable");
        } else {
            specialAnswer.setVisibility(View.GONE);
        }

        questionNoText = root.findViewById(R.id.questionNo);
        questionNoText.setText("Question " + questionNo + " out of " + totalQuestions);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().
                findFragmentById(R.id.drawingMapView);
        supportMapFragment.getMapAsync(this);
        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        swarmMap = googleMap;

        swarmMap.addPolygon(new PolygonOptions()
                .add(SwarmActivity.bottomLeftLatLng,
                        SwarmActivity.bottomRightLatLng,
                        SwarmActivity.topRightLatLng,
                        SwarmActivity.topLeftLatLng,
                        SwarmActivity.bottomLeftLatLng)
                .strokeWidth(5)
                .strokeColor(Color.DKGRAY));

        swarmMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SwarmActivity.mapCentre, 19.05f));
        swarmMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        swarmMap.getUiSettings().setAllGesturesEnabled(false);

        calculateGraphicsDistances();

        grid = new Grid(SwarmActivity.bottomLeftLatLng, SwarmActivity.bottomRightLatLng,
                SwarmActivity.topLeftLatLng, swarmMap,
                12, SwarmActivity.simSize,SwarmActivity.simOffset, getContext());
        grid.initializeGrid();
        grid.drawGrid();

        swarmMap.setOnMapClickListener(latLng -> {

            calculateGraphicsDistances();
            markSquare(latLng);
        });

        swarmMap.setOnMapLongClickListener(latLng -> {
            float[] simCoordinates = latLngToSimCoordinates(latLng);
            String cellName = grid.getCellName(simCoordinates);
            for (Map.Entry<Integer, Polygon> entry : squaresList.entrySet()) {
                if (!cellName.equals("None") && !cellName.equals(entry.getValue().getTag())) {
                    entry.getValue().setStrokeColor(SQUARE_COLOUR_UNSELECTED);
                } else {
                    selectedSquareId = entry.getKey();
                    selectedSquareName = (String) entry.getValue().getTag();
                    entry.getValue().setStrokeColor(SQUARE_COLOUR_SELECTED);
                    deletePopup(selectedSquareId, selectedSquareName);
                }
            }
        });

        swarmMap.setOnCameraMoveListener(() -> calculateGraphicsDistances());

//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
//        {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId)
//            {
//                swarmMap.getUiSettings().setScrollGesturesEnabled(false);
//                swarmMap.getUiSettings().setAllGesturesEnabled(false);
//                swarmMap.getUiSettings().setMapToolbarEnabled(false);
//                grid.clearGrid();
//            }
//        });

        // To impose the actual image of the area from Google maps 2023 data
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.map))
                .position(SwarmActivity.mapCentre, SwarmActivity.widthInMeters,
                        SwarmActivity.widthInMeters);
        swarmMap.addGroundOverlay(newarkMap);
    }

    private void calculateGraphicsDistances() {
        LatLng currentLatLng  = swarmMap.getCameraPosition().target;
        if (!currentLatLng .equals(SwarmActivity.mapCentre)) {
            CameraPosition newPosition = new CameraPosition.Builder()
                    .target(SwarmActivity.mapCentre) // Set to initial LatLng
                    .zoom(swarmMap.getCameraPosition().zoom) // Keep current zoom
                    .tilt(swarmMap.getCameraPosition().tilt) // Keep current tilt
                    .bearing(swarmMap.getCameraPosition().bearing) // Keep current bearing
                    .build();
            swarmMap.moveCamera(CameraUpdateFactory.newCameraPosition(newPosition));
        }
        leftPointBound = swarmMap.getProjection().toScreenLocation(SwarmActivity.bottomLeftLatLng);
        rightPointBound = swarmMap.getProjection().toScreenLocation(SwarmActivity.bottomRightLatLng);
        screenWidth = rightPointBound.x - leftPointBound.x;
    }

    private void markSquare(LatLng latLng) {
        float simCoordinates[] = latLngToSimCoordinates(latLng);
        float[][] cellBoundary = grid.getCellBoundary(simCoordinates);
        if (question.getQuestionContent().contains("exactly") || question.getQuestionContent().contains("largest") || question.getQuestionContent().contains("closest")) {
            if (squaresList.size() >= 1) {
                SpannableStringBuilder biggerText = new SpannableStringBuilder(ONE_CELL_ONLY);
                biggerText.setSpan(new RelativeSizeSpan(1.5f), 0, ONE_CELL_ONLY.length(), 0);
                Toast.makeText(getActivity(), biggerText, Toast.LENGTH_LONG).show();
                return;
            }
        }

        String cellName = grid.getCellName(simCoordinates);
        if (!cellName.equals("None")) {
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
            selectedSquareId = markingsCount;
            Polygon newSquare = swarmMap.addPolygon(squareOptions);
            newSquare.setTag(cellName);
            newSquare.setZIndex(10);
            squaresList.put(selectedSquareId, newSquare);
            markingsCount += 1;
        }
    }

    private void deletePopup(int id, String cellName) {
        // Create the object of AlertDialog Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the message show for the Alert time
        builder.setMessage("Are you sure you want to delete " + cellName + "?");

        // Set Alert Title
        builder.setTitle("Delete drawing " + " Alert !");

        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
        builder.setCancelable(false);

        // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setPositiveButton("Yes", (dialog, which) -> {
            squaresList.get(id).remove();
            squaresList.remove(id);
            selectedSquareName = null;
            selectedSquareId = null;
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

    private float[] latLngToSimCoordinates(LatLng latLng) {
        Point screenPoint = swarmMap.getProjection().toScreenLocation(latLng);
        float simX = (((float) (screenPoint.x - leftPointBound.x) / screenWidth)
                * SwarmActivity.simSize) - SwarmActivity.simOffset;
        float simY = ((float) (leftPointBound.y - screenPoint.y) / screenWidth
                * SwarmActivity.simSize) - SwarmActivity.simOffset;
        return new float[]{simX, simY};
    }

    private LatLng simCoordinatesToLatLng(float[] simCordinates) {
        float xCordinate = (((SwarmActivity.simOffset + simCordinates[0]) / SwarmActivity.simSize)
                * screenWidth) + leftPointBound.x;
        float yCordinate = -(((SwarmActivity.simOffset + simCordinates[1]) / SwarmActivity.simSize)
                * screenWidth) + leftPointBound.y;
        LatLng latLng = swarmMap.getProjection().fromScreenLocation(new
                Point(Math.round(xCordinate), Math.round(yCordinate)));
        return latLng;
    }

    public String getMarkedCellNames() {
        String cellNames = "";
        for (Map.Entry<Integer, Polygon> square :
                squaresList.entrySet()) {
            cellNames += square.getValue().getTag() + "_";
        }
        return cellNames;
    }

    public int getCurrentMarkingsCount() {
        return squaresList.size();
    }

    public String getSelectedAnswer() {
        // get selected radio button from radioGroup
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == -1)
            return null;

        // find the radiobutton by returned id
        RadioButton selectedRadioButton = root.findViewById(selectedId);
        return (String) selectedRadioButton.getText();
    }

    /**
     * Clear the selected answers if any
     */
    public void clearFields() {
        radioGroup.clearCheck();
        for (Integer id : squaresList.keySet()) {
            Objects.requireNonNull(squaresList.get(id)).remove();
        }
        selectedSquareName = null;
        selectedSquareId = null;
    }
}