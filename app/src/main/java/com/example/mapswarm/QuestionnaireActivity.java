package com.example.mapswarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class QuestionnaireActivity extends AppCompatActivity {

    private Button nextBtn;
    private Button backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);
        getSupportFragmentManager().beginTransaction().add(R.id.container,
                new DrawingFragment()).commit();

        nextBtn = findViewById(R.id.nextBtn);
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

            Toast.makeText(QuestionnaireActivity.this, directory.getPath(),
                    Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,  // enter
                        R.anim.slide_out_left  // exit
                    )
                    .replace(R.id.container, new NonDrawingFragment())
                    .addToBackStack(null)
                    .commit();
        });

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view -> finish());
    }
}