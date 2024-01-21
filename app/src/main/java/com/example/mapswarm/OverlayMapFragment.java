package com.example.mapswarm;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.SupportMapFragment;

/**
 *  Class to overlay a view on top of a map fragment to intercept interactions
 */
public class OverlayMapFragment extends SupportMapFragment {
    private static final float MIN_SWIPE_DISTANCE = 10;
    public View mapView;
    public TouchableWrapper touchView;
    private OverlayMapFragment.OnTouchListener touchListener;
    private OverlayMapFragment.OnFlingListener flingListener;
    private float swipeX1;
    private float swipeY1;
    private float swipeX2;
    private float swipeY2;

    public static OverlayMapFragment newInstance() {
        return new OverlayMapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mapView = super.onCreateView(inflater, parent, savedInstanceState);
        // overlay a touch view on map view to intercept the event
        touchView = new TouchableWrapper(getActivity());
        touchView.addView(mapView);
        return touchView;
    }
    @Override
    public View getView() {
        return mapView;
    }
    public void setOnTouchListener(OverlayMapFragment.OnTouchListener listener) {
        this.touchListener = listener;
    }

    public interface OnTouchListener {
        void onTouch(MotionEvent event);
    }

    public void setOnFlingListener(OverlayMapFragment.OnFlingListener listener) {
        this.flingListener = listener;
    }

    public interface OnFlingListener {
        void onFling(float x1, float y1, float x2, float y2);
    }
    public class TouchableWrapper extends FrameLayout {
        public TouchableWrapper(Context context) {
            super(context);
        }
        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    swipeX1 = event.getX();
                    swipeY1 = event.getY();
                    return super.dispatchTouchEvent(event);
                case MotionEvent.ACTION_UP:
                    swipeX2 = event.getX();
                    swipeY2 = event.getY();
                    float deltaX = swipeX2 - swipeX1;
                    float deltaY = swipeY2 - swipeY1;
                    if (Math.abs(deltaX) > MIN_SWIPE_DISTANCE || Math.abs(deltaY) > MIN_SWIPE_DISTANCE) {
                        flingListener.onFling(swipeX1, swipeY1, swipeX2, swipeY2);
                    } else {
                        if (touchListener != null) {
                            touchListener.onTouch(event);
                        }
                    }
                    swipeX1 = 0f;
                    swipeX2 = 0f;
                    swipeY1 = 0f;
                    swipeY2 = 0f;
                    return super.dispatchTouchEvent(event);
            }
            return super.dispatchTouchEvent(event);
        }
    }
}
