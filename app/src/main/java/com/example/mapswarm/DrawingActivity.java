package com.example.mapswarm;

import static com.example.mapswarm.DrawingView.colourList;
import static com.example.mapswarm.DrawingView.currentBrush;
import static com.example.mapswarm.DrawingView.pathList;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class DrawingActivity extends AppCompatActivity {

    public static Path path = new Path();
    public static Paint paintBrush = new Paint();
    private Button pencil;
    private Button eraser;
    private Button nextBtn;
    private Button backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        pencil = findViewById(R.id.pencil);
        eraser = findViewById(R.id.eraser);
        nextBtn = findViewById(R.id.nextBtn);

        pencil.setOnClickListener(view -> {
            paintBrush.setColor(Color.GREEN);
            currentColour(paintBrush.getColor());
        });

        eraser.setOnClickListener(view -> {
            pathList.clear();
            colourList.clear();
            path.reset();
        });

        nextBtn.setOnClickListener(view -> {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            File file = new File(directory, ts +"UniqueFileName" + ".jpg");
            if (!file.exists()) {
                Log.d("path", file.toString());
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    View drawingView = findViewById(R.id.drawingView);
                    drawingView.setDrawingCacheEnabled(true);
                    Bitmap bitmap = drawingView.getDrawingCache();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }

            Toast.makeText(DrawingActivity.this, directory.getPath(),
                    Toast.LENGTH_SHORT).show();

        });

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view -> finish());
    }

    public void currentColour(int c) {
        currentBrush = c;
        path = new Path();
    }
}