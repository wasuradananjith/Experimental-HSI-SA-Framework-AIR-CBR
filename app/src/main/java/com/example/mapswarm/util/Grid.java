package com.example.mapswarm.util;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class Grid {

    private final LatLng topLeftLatLng;
    private LatLng bottomLeftLatLng;
    private LatLng bottomRightLatLng;
    private GoogleMap googleMap;
    private int size;
    private ArrayList<LatLng> bottomLine = new ArrayList<>();
    private ArrayList<LatLng> topLine = new ArrayList<>();
    private ArrayList<LatLng> leftLine = new ArrayList<>();
    private ArrayList<LatLng> rightLine = new ArrayList<>();

    public Grid(LatLng bottomLeftLatLng, LatLng bottomRightLatLng, LatLng topLeftLatLng, GoogleMap googleMap, int size) {
        this.bottomLeftLatLng = bottomLeftLatLng;
        this.bottomRightLatLng = bottomRightLatLng;
        this.topLeftLatLng = topLeftLatLng;
        this.googleMap = googleMap;
        this.size = size;
    }

    public void initializeGrid() {
        double cellSize = Math.abs(bottomLeftLatLng.longitude - bottomRightLatLng.longitude)/size;
        Log.i("SIM: cellSize ", String.valueOf(cellSize));
        Log.i("SIM: size ", String.valueOf(this.size));

        bottomLine.add(bottomLeftLatLng);
        topLine.add(topLeftLatLng);
        for(int i = 1; i <= this.size; i ++) {
            bottomLine.add(new LatLng(bottomLeftLatLng.latitude, bottomLeftLatLng.longitude + i*cellSize));
            topLine.add(new LatLng(topLeftLatLng.latitude, topLeftLatLng.longitude + i*cellSize));
        }

        cellSize = Math.abs(bottomLeftLatLng.latitude - topLeftLatLng.latitude)/size;
        leftLine.add(bottomLeftLatLng);
        rightLine.add(bottomRightLatLng);
        for(int i = 1; i <= this.size; i ++) {
            leftLine.add(new LatLng(bottomLeftLatLng.latitude + i*cellSize, bottomLeftLatLng.longitude));
            rightLine.add(new LatLng(bottomRightLatLng.latitude + i*cellSize, bottomRightLatLng.longitude));
        }

//        for (LatLng latLng: bottomLine) {
//            Log.i("SIM: lat ", String.valueOf(latLng.latitude));
//            Log.i("SIM: long ", String.valueOf(latLng.longitude));
//            googleMap.addMarker(new MarkerOptions()
//                    .position(latLng)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
//        }
//
//        for (LatLng latLng: topLine) {
//            Log.i("SIM: lat ", String.valueOf(latLng.latitude));
//            Log.i("SIM: long ", String.valueOf(latLng.longitude));
//            googleMap.addMarker(new MarkerOptions()
//                    .position(latLng)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
//        }
//
//        for (LatLng latLng: leftLine) {
//            Log.i("SIM: lat ", String.valueOf(latLng.latitude));
//            Log.i("SIM: long ", String.valueOf(latLng.longitude));
//            googleMap.addMarker(new MarkerOptions()
//                    .position(latLng)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
//        }
//
//        for (LatLng latLng: rightLine) {
//            Log.i("SIM: lat ", String.valueOf(latLng.latitude));
//            Log.i("SIM: long ", String.valueOf(latLng.longitude));
//            googleMap.addMarker(new MarkerOptions()
//                    .position(latLng)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
//        }
    }

    public void drawGrid() {
        for(int i = 0; i <= this.size; i ++) {
            this.googleMap.addPolyline((new PolylineOptions()).add(bottomLine.get(i), topLine.get(i))
                    .width(5)
                    .color(Color.GRAY)
                    // below line is to make our poly line geodesic.
                    .geodesic(true));
            this.googleMap.addPolyline((new PolylineOptions()).add(leftLine.get(i), rightLine.get(i))
                    .width(5)
                    .color(Color.GRAY)
                    // below line is to make our poly line geodesic.
                    .geodesic(true));
        }

    }
}
