package com.example.mapswarm;

import static com.example.mapswarm.DrawingView.colourList;
import static com.example.mapswarm.DrawingView.currentBrush;
import static com.example.mapswarm.DrawingView.pathList;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class DrawingActivity extends AppCompatActivity {

    public static Path path = new Path();
    public static Paint paintBrush = new Paint();
    private Button pencil;
    private Button eraser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        pencil = findViewById(R.id.pencil);
        eraser = findViewById(R.id.eraser);

        pencil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paintBrush.setColor(Color.BLACK);
                currentColour(paintBrush.getColor());
            }
        });

        eraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pathList.clear();
                colourList.clear();
                path.reset();
            }
        });
    }

    public void currentColour(int c) {
        currentBrush = c;
        path = new Path();
    }
}