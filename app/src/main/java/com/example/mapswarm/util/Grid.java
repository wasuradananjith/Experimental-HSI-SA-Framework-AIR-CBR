package com.example.mapswarm.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.mapswarm.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class Grid {

    private Context context;
    private final LatLng topLeftLatLng;
    private LatLng bottomLeftLatLng;
    private LatLng bottomRightLatLng;
    private GoogleMap googleMap;
    private int numberOfCells;
    private int numberOfCellsForRandomness = 2;
    private int simSize;
    private int offset;
    private ArrayList<LatLng> bottomLine = new ArrayList<>();
    private ArrayList<LatLng> topLine = new ArrayList<>();
    private ArrayList<LatLng> leftLine = new ArrayList<>();
    private ArrayList<LatLng> rightLine = new ArrayList<>();
    private ArrayList<Polyline> gridPolylines = new ArrayList<>();
    private ArrayList<Marker> gridMarkers = new ArrayList<>();
    private int[] letters = {R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d, R.drawable.e,
            R.drawable.f, R.drawable.g, R.drawable.h, R.drawable.i, R.drawable.j, R.drawable.k,
            R.drawable.l};
    private int[] numbers = {R.drawable.one, R.drawable.two, R.drawable.three, R.drawable.four,
            R.drawable.five, R.drawable.six, R.drawable.seven, R.drawable.eight, R.drawable.nine,
            R.drawable.ten, R.drawable.eleven, R.drawable.twelve};

    public Grid(LatLng bottomLeftLatLng, LatLng bottomRightLatLng, LatLng topLeftLatLng,
                GoogleMap googleMap, int numberOfCells, int simSize, int offset, Context context) {
        this.bottomLeftLatLng = bottomLeftLatLng;
        this.bottomRightLatLng = bottomRightLatLng;
        this.topLeftLatLng = topLeftLatLng;
        this.googleMap = googleMap;
        this.numberOfCells = numberOfCells;
        this.simSize = simSize;
        this.offset = offset;
        this.context = context;
    }

    public void initializeGrid() {
        double cellSize = Math.abs(bottomLeftLatLng.longitude - bottomRightLatLng.longitude)/ numberOfCells;
        // Log.i("SIM: cellSize ", String.valueOf(cellSize));
        // Log.i("SIM: size ", String.valueOf(this.size));

        bottomLine.add(bottomLeftLatLng);
        topLine.add(topLeftLatLng);
        for(int i = 1; i <= this.numberOfCells; i ++) {
            bottomLine.add(new LatLng(bottomLeftLatLng.latitude, bottomLeftLatLng.longitude + i*cellSize));
            topLine.add(new LatLng(topLeftLatLng.latitude, topLeftLatLng.longitude + i*cellSize));
        }

        cellSize = Math.abs(bottomLeftLatLng.latitude - topLeftLatLng.latitude)/ numberOfCells;
        leftLine.add(bottomLeftLatLng);
        rightLine.add(bottomRightLatLng);
        for(int i = 1; i <= this.numberOfCells; i ++) {
            leftLine.add(new LatLng(bottomLeftLatLng.latitude + i*cellSize, bottomLeftLatLng.longitude));
            rightLine.add(new LatLng(bottomRightLatLng.latitude + i*cellSize, bottomRightLatLng.longitude));
        }
    }

    public void drawGrid() {
        for(int i = 0; i <= this.numberOfCells; i ++) {
            Polyline polyline = this.googleMap.addPolyline((new PolylineOptions()).add(bottomLine.get(i), topLine.get(i))
                    .width(5)
                    .color(Color.GRAY)
                    // below line is to make our poly line geodesic.
                    .geodesic(true));
            polyline.setZIndex(1);
            gridPolylines.add(polyline);
            polyline = this.googleMap.addPolyline((new PolylineOptions()).add(leftLine.get(i), rightLine.get(i))
                    .width(5)
                    .color(Color.GRAY)
                    // below line is to make our poly line geodesic.
                    .geodesic(true));
            polyline.setZIndex(1);
            gridPolylines.add(polyline);
        }
        for (int i = 0; i < numberOfCells; i ++) {
            LatLng latLng = LatLngBounds.builder().include(bottomLine.get(i)).
                    include(bottomLine.get(i+1)).build().getCenter();
            Marker marker = this.googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapFromVector(
                            this.context,
                            letters[i]))
                    .position(new LatLng(latLng.latitude - 0.00005, latLng.longitude)));
            gridMarkers.add(marker);

            latLng = LatLngBounds.builder().include(leftLine.get(i)).
                    include(leftLine.get(i+1)).build().getCenter();
            marker = this.googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapFromVector(
                            this.context,
                            numbers[i]))
                    .position(new LatLng(latLng.latitude - 0.00002, latLng.longitude - 0.00004)));
            gridMarkers.add(marker);

        }

    }

    public String getCellName(float[] simCoordinate) {
        int x = (int) Math.ceil((simCoordinate[0] + this.offset)/(this.simSize/this.numberOfCells));
        int y = (int) Math.ceil((simCoordinate[1] + this.offset)/(this.simSize/this.numberOfCells));
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Log.i("Test x ", Integer.toString(x));
        Log.i("Test y ", Integer.toString(y));
        if (x <= 0 || x >= 12) {
            return "None";
        }
        if (y <= 0 || y >= 12) {
            return "None";
        }
        String letterX = alphabet.substring(x-1, x);
        return letterX+y;
//        try {
//            String letterX = alphabet.substring(x - 1, x);
//            return letterX+y;
//        } catch (StringIndexOutOfBoundsException e) {
//            Log.e("Swarm Activity ", e.toString());
//            return "";
//        }
    }

    public String getCellName(double[] simCoordinate) {
        int x = (int) Math.ceil((simCoordinate[0] + this.offset)/(this.simSize/this.numberOfCells));
        int y = (int) Math.ceil((simCoordinate[1] + this.offset)/(this.simSize/this.numberOfCells));
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String letterX = alphabet.substring(x-1, x);
        return letterX+y;
    }

    public float[][] getCellBoundary(float[] simCoordinate) {
        int x = (int) Math.ceil((simCoordinate[0] + this.offset)/(this.simSize/this.numberOfCells));
        int y = (int) Math.ceil((simCoordinate[1] + this.offset)/(this.simSize/this.numberOfCells));

        float xMax = x*(this.simSize/this.numberOfCells) - offset;
        float xMin = xMax - this.simSize/this.numberOfCells;
        float yMax = y*(this.simSize/this.numberOfCells) - offset;
        float yMin = yMax - this.simSize/this.numberOfCells;
        float centerX = (xMin + xMax)/2;
        float centerY = (yMin + yMax)/2;

        // topLeft, topRight, bottomLeft, bottomRight
        return new float[][] {{xMin, yMax}, {xMax, yMax}, {xMin, yMin}, {xMax, yMin},
                {centerX, centerY}};
    }

    public float[][] getNearestRandomCellBoundary(float[] simCoordinate){
        int x = (int) Math.ceil((simCoordinate[0] + this.offset)/(this.simSize/this.numberOfCellsForRandomness));
        int y = (int) Math.ceil((simCoordinate[1] + this.offset)/(this.simSize/this.numberOfCellsForRandomness));

        float xMax = x*(this.simSize/this.numberOfCellsForRandomness) - offset;
        float xMin = xMax - this.simSize/this.numberOfCellsForRandomness;
        float yMax = y*(this.simSize/this.numberOfCellsForRandomness) - offset;
        float yMin = yMax - this.simSize/this.numberOfCellsForRandomness;
        float centerX = (xMin + xMax)/2;
        float centerY = (yMin + yMax)/2;

        // topLeft, topRight, bottomLeft, bottomRight
        return new float[][] {{xMin, yMax}, {xMax, yMax}, {xMin, yMin}, {xMax, yMin},
                {centerX, centerY}};
    }

    public void clearGrid() {
        for (Polyline polyline: this.gridPolylines) {
            polyline.remove();
        }
        for (Marker marker: gridMarkers) {
            marker.remove();
        }
    }

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
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
        return BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmap, 30, 30, false));
    }
}
