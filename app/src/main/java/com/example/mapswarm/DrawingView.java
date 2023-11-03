package com.example.mapswarm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DrawingView extends View {
    public ArrayList<Path> pathList = new ArrayList<>();
    public ArrayList<Integer> colourList = new ArrayList<>();
    public ViewGroup.LayoutParams params;
    public int currentBrush = Color.GREEN;
    private Path path = new Path();
    private Paint paintBrush = new Paint();

    public DrawingView(Context context) {
        super(context);
        init(context);
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        paintBrush.setAntiAlias(true);
        paintBrush.setColor(Color.GREEN);
        paintBrush.setStyle(Paint.Style.STROKE);
        paintBrush.setStrokeCap(Paint.Cap.ROUND);
        paintBrush.setStrokeJoin(Paint.Join.ROUND);
        paintBrush.setStrokeWidth(50f);

        params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                pathList.add(path);
                colourList.add(currentBrush);
                invalidate();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int i = 0; i < pathList.size(); i++) {
            paintBrush.setColor(colourList.get(i));
            canvas.drawPath(pathList.get(i), paintBrush);
            invalidate();
        }
    }

    public ArrayList<Path> getPathList() {
        return pathList;
    }

    public void setPathList(ArrayList<Path> pathList) {
        this.pathList = pathList;
    }

    public ArrayList<Integer> getColourList() {
        return colourList;
    }

    public void setColourList(ArrayList<Integer> colourList) {
        this.colourList = colourList;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Paint getPaintBrush() {
        return paintBrush;
    }

    public void setPaintBrush(Paint paintBrush) {
        this.paintBrush = paintBrush;
    }

    public int getCurrentBrush() {
        return currentBrush;
    }

    public void setCurrentBrush(int currentBrush) {
        this.currentBrush = currentBrush;
    }
}
