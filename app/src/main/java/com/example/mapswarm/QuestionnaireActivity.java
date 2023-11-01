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

import com.example.mapswarm.db.SQLiteManager;
import com.example.mapswarm.model.Question;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class QuestionnaireActivity extends AppCompatActivity {

    private Button nextBtn;
    private Button backBtn;
    private ArrayList<Question> questions;
    private int questionCounter = 0;
    private Question currentQuestion = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        // Retrieve the filter data count passed from the previous activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Integer filterDataCount = Integer.valueOf(extras.getInt("filterDataCount"));
            questions = retrieveQuestions(filterDataCount);
        }

        if (questions.size() != 0) {
            // Display the first question when the fragment is loaded
            currentQuestion = questions.get(0);
            if (currentQuestion.isDrawing() == 1) {
                DrawingFragment drawingFragment = new DrawingFragment(currentQuestion);
                getSupportFragmentManager().beginTransaction().add(R.id.container,
                        drawingFragment).commit();
            } else {
                NonDrawingFragment nonDrawingFragment = new NonDrawingFragment(currentQuestion);
                getSupportFragmentManager().beginTransaction().add(R.id.container,
                        nonDrawingFragment).commit();
            }
        }

        nextBtn = findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(view -> {
            if (currentQuestion.isDrawing() == 1) {
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                Long tsLong = System.currentTimeMillis() / 1000;
                String ts = tsLong.toString();
                File file = new File(directory, ts + "UniqueFileName" + ".jpg");
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
            }

            // If the question counter has not reached the end of the questions
            if (questionCounter != questions.size()-1) {
                questionCounter += 1; // Increment the question counter to get the next question
                currentQuestion = questions.get(questionCounter);
                if (currentQuestion.isDrawing() == 1) {
                    DrawingFragment drawingFragment = new DrawingFragment(currentQuestion);
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_right,  // enter
                                    R.anim.slide_out_left  // exit
                            )
                            .replace(R.id.container, drawingFragment)
                            .addToBackStack(null)
                            .commit();
                } else {
                    NonDrawingFragment nonDrawingFragment = new NonDrawingFragment(currentQuestion);
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_right,  // enter
                                    R.anim.slide_out_left  // exit
                            )
                            .replace(R.id.container, nonDrawingFragment)
                            .addToBackStack(null)
                            .commit();
                }
            } else {
                finish();
            }
        });

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view -> finish());
    }

    /**
     * Retrieve the questions from the database
     * @param filterDataCount  number of questions to be filtered from the database
     * @return list of questions
     */
    public ArrayList<Question> retrieveQuestions(int filterDataCount) {
        SQLiteManager sqLiteManager = new SQLiteManager(this);
        try {
            sqLiteManager.open();
            ArrayList<Question> questions = sqLiteManager.fetchQuestionBankData(filterDataCount, 16);
            sqLiteManager.close();
            return  questions;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}