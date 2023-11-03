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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class QuestionnaireActivity extends AppCompatActivity {

    private Button nextBtn;
    private Button backBtn;
    private ArrayList<Question> questions;
    private int questionCounter = 0;
    private int activityRound = 0;
    private Question currentQuestion = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        // Retrieve the filter data count passed from the previous activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Integer filterDataCount = Integer.valueOf(extras.getInt("filterDataCount"));
            activityRound = Integer.valueOf(extras.getInt("activityRound"));
            questions = retrieveQuestions(filterDataCount,
                    Integer.valueOf(extras.getInt("limit")));
        }

        if (questions.size() != 0) {
            // Display the first question when the fragment is loaded
            currentQuestion = questions.get(0);
            if (currentQuestion.isDrawing() == 1) {
                DrawingFragment drawingFragment = new DrawingFragment(currentQuestion, 1,
                        questions.size());
                getSupportFragmentManager().beginTransaction().add(R.id.container,
                        drawingFragment).commit();
            } else {
                NonDrawingFragment nonDrawingFragment = new NonDrawingFragment(currentQuestion, 1,
                        questions.size());
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
                    DrawingFragment drawingFragment = new DrawingFragment(currentQuestion, questionCounter+1,
                            questions.size());
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_right,  // enter
                                    R.anim.slide_out_left  // exit
                            )
                            .replace(R.id.container, drawingFragment)
                            .addToBackStack(null)
                            .commit();
                    currentQuestion.setDrawingAnswer(null);
                } else {
                    NonDrawingFragment nonDrawingFragment = new NonDrawingFragment(currentQuestion, questionCounter+1,
                            questions.size());
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_right,  // enter
                                    R.anim.slide_out_left  // exit
                            )
                            .replace(R.id.container, nonDrawingFragment)
                            .addToBackStack(null)
                            .commit();
                    currentQuestion.setMcqAnswer("test");
                }
            } else {
                finish();
            }
            currentQuestion.setCount(currentQuestion.getCount()+1);
            updateTheQuestionDataOnNext(currentQuestion);
        });

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view -> finish());
    }

    /**
     * Retrieve the questions from the database
     * @param filterDataCount  questions to be filtered from the
     *                         database based on the times they occurred
     * @param limit number of questions to retrieve
     * @return list of questions
     */
    public ArrayList<Question> retrieveQuestions(int filterDataCount, int limit) {
        SQLiteManager sqLiteManager = new SQLiteManager(this);
        try {
            sqLiteManager.open();
            ArrayList<Question> questions = sqLiteManager.fetchQuestionBankData(filterDataCount,
                    limit);
            sqLiteManager.close();
            return  questions;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update the question data
     * @param question question
     */
    public void updateTheQuestionDataOnNext(Question question) {
        SQLiteManager sqLiteManager = new SQLiteManager(this);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        sqLiteManager.open();
        if (sqLiteManager.updateQuestionBankData(question) > 0) {
            sqLiteManager.insertUserAnswers(currentUser.getEmail().split("@")[0],
                    question, question.getCount(), activityRound);
        }
        sqLiteManager.close();
    }
}