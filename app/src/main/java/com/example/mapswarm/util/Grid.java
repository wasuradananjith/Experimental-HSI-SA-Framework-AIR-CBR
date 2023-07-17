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
    private int size;
    private ArrayList<LatLng> bottomLine = new ArrayList<>();
    private ArrayList<LatLng> topLine = new ArrayList<>();
    private ArrayList<LatLng> leftLine = new ArrayList<>();
    private ArrayList<LatLng> rightLine = new ArrayList<>();
    private ArrayList<Polyline> gridPolylines = new ArrayList<>();
    private ArrayList<Marker> gridMarkers = new ArrayList<>();
    private int[] letters = {R.drawable.a, R.drawable.b, R.drawable.c};
    private int[] numbers = {R.drawable.one, R.drawable.two, R.drawable.three};

    public Grid(LatLng bottomLeftLatLng, LatLng bottomRightLatLng, LatLng topLeftLatLng,
                GoogleMap googleMap, int size, Context context) {
        this.bottomLeftLatLng = bottomLeftLatLng;
        this.bottomRightLatLng = bottomRightLatLng;
        this.topLeftLatLng = topLeftLatLng;
        this.googleMap = googleMap;
        this.size = size;
        this.context = context;
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
            Polyline polyline = this.googleMap.addPolyline((new PolylineOptions()).add(bottomLine.get(i), topLine.get(i))
                    .width(5)
                    .color(Color.GRAY)
                    // below line is to make our poly line geodesic.
                    .geodesic(true));
            gridPolylines.add(polyline);
            polyline = this.googleMap.addPolyline((new PolylineOptions()).add(leftLine.get(i), rightLine.get(i))
                    .width(5)
                    .color(Color.GRAY)
                    // below line is to make our poly line geodesic.
                    .geodesic(true));
            gridPolylines.add(polyline);
        }
        for (int i = 0; i < size; i ++) {
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
                    .position(new LatLng(latLng.latitude, latLng.longitude - 0.00001)));
            gridMarkers.add(marker);

        }

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
        return BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmap, 50, 50, false));
    }
}
