package com.example.mapswarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.loadinganimation.LoadingAnimation;
import com.example.mapswarm.db.SQLiteManager;
import com.example.mapswarm.model.Question;
import com.example.mapswarm.util.MyTimer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class QuestionnaireActivity extends AppCompatActivity {

    private Button nextBtn;
    private Button backBtn;
    private ArrayList<Question> questions;
    private int questionCounter = 0;
    private int activityRound = 0;
    private int questionRound = 0;
    private Question currentQuestion = null;
    private MyTimer timer;
    private TextView timerTextView;
    private long timeLeftInMilliseconds = 120000;
    private long questionStartTime = 0;
    private boolean isTimeOutQuestionsUpdated = false;
    private LoadingAnimation loadingAnimation;
    private LoadingAnimation ranOutTimeAnimation;
    Handler handler = new Handler();
    Runnable trackTimer = new Runnable() {
        @Override
        public void run() {
            periodicWork();
            handler.postDelayed(this, 100);
        }
    };
    private NonDrawingFragment nonDrawingFragment;
    private DrawingFragment drawingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        loadingAnimation = findViewById(R.id.loadingAnim);
        ranOutTimeAnimation = findViewById(R.id.ranOutTimeAnim);
        timerTextView = findViewById(R.id.timerText);
        timer = new MyTimer(false, timeLeftInMilliseconds, timerTextView);
        timer.updateTimer();
        timer.startStop();

        // Retrieve the filter data count passed from the previous activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Integer filterDataCount = Integer.valueOf(extras.getInt("filterDataCount"));
            activityRound = Integer.valueOf(extras.getInt("activityRound"));
            questionRound = Integer.valueOf(extras.getInt("questionRound"));
            questions = retrieveQuestions(filterDataCount);
        }

        if (questions.size() != 0) {
            // Display the first question when the fragment is loaded
            currentQuestion = questions.get(0);
            questionStartTime = timer.getTimeLeftInMilliseconds();
            if (currentQuestion.isDrawing() == 1) {
                drawingFragment = new DrawingFragment(currentQuestion, questionCounter+1,
                        questions.size());
                getSupportFragmentManager().beginTransaction().add(R.id.container,
                        drawingFragment).commit();
            } else {
                nonDrawingFragment = new NonDrawingFragment(currentQuestion, questionCounter+1,
                        questions.size());
                getSupportFragmentManager().beginTransaction().add(R.id.container,
                        nonDrawingFragment).commit();
            }
        }

        nextBtn = findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(view -> {
            if (currentQuestion.isDrawing() == 1) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                View drawingView = findViewById(R.id.drawingView);
                drawingView.setDrawingCacheEnabled(true);
                Bitmap bitmap = drawingView.getDrawingCache();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] img = byteArrayOutputStream.toByteArray();
                currentQuestion.setDrawingAnswer(img);

//                ContextWrapper cw = new ContextWrapper(getApplicationContext());
//                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
//                Long tsLong = System.currentTimeMillis() / 1000;
//                String ts = tsLong.toString();
//                File file = new File(directory, ts + "UniqueFileName" + ".jpg");
//                if (!file.exists()) {
//                    Log.d("path", file.toString());
//                    FileOutputStream fos = null;
//                    try {
//                        fos = new FileOutputStream(file);
//                        View drawingView = findViewById(R.id.drawingView);
//                        drawingView.setDrawingCacheEnabled(true);
//                        Bitmap bitmap = drawingView.getDrawingCache();
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//                        fos.flush();
//                        fos.close();
//                    } catch (java.io.IOException e) {
//                        e.printStackTrace();
//                    }
//                }
                currentQuestion.setMcqAnswer("drawing");
            } else {
                String answer = nonDrawingFragment.getSelectedAnswer();
                currentQuestion.setMcqAnswer((answer == null)? "skipped": answer);
            }

            currentQuestion.setCount(currentQuestion.getCount()+1);
            currentQuestion.setElapsedTime((questionStartTime - timer.getTimeLeftInMilliseconds())/1000);
            updateTheQuestionDataOnNext(currentQuestion);
            questionCounter += 1;

            // If the question counter has not reached the end of the questions
            if (questionCounter != questions.size()) {
                currentQuestion = questions.get(questionCounter);
                //questionCounter += 1; // Increment the question counter to get the next question
                if (currentQuestion.isDrawing() == 1) {
                    drawingFragment = new DrawingFragment(currentQuestion, questionCounter+1,
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
                    nonDrawingFragment = new NonDrawingFragment(currentQuestion, questionCounter+1,
                            questions.size());
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_right,  // enter
                                    R.anim.slide_out_left  // exit
                            )
                            .replace(R.id.container, nonDrawingFragment)
                            .addToBackStack(null)
                            .commit();
                }
                questionStartTime = timer.getTimeLeftInMilliseconds();
            } else {
                Timer timer1 = new Timer();
                loadingAnimation.setVisibility(View.VISIBLE);
                loadingAnimation.setTextMsg("Thank you! \n Question round " + questionRound +
                        " completed... Back to the task!");
                timer1.schedule(new TimerTask() {
                    public void run() {
                        finish();
                        timer.getCountDownTimer().cancel();
                    }
                }, 3000);
            }
        });

        //backBtn = findViewById(R.id.backBtn);
        //backBtn.setOnClickListener(view -> finish());
        handler.post(trackTimer);
    }

    /**
     * Retrieve the questions from the database
     * @param filterDataCount  questions to be filtered from the
     *                         database based on the times they occurred
     * @return list of questions
     */
    public ArrayList<Question> retrieveQuestions(int filterDataCount) {
        SQLiteManager sqLiteManager = new SQLiteManager(this);
        try {
            sqLiteManager.open();
            ArrayList<Question> questions = sqLiteManager.fetchQuestionBankData(filterDataCount,
                    13);
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

    /**
     * Periodic work to set timer changes
     */
    public void periodicWork() {
        if (!isTimeOutQuestionsUpdated) {
            if (timer.getTimeLeftInMilliseconds() <= 10000) {
                timerTextView.setTextColor(Color.RED);
                if (timer.getTimeLeftInMilliseconds() <= 1000) {
                    isTimeOutQuestionsUpdated = true;
                    Timer timer1 = new Timer();
                    ranOutTimeAnimation.setVisibility(View.VISIBLE);
                    for (int i = questionCounter; i < questions.size(); i++) {
                        currentQuestion = questions.get(i);
                        currentQuestion.setCount(currentQuestion.getCount() + 1);
                        currentQuestion.setMcqAnswer("Timeout");
                        updateTheQuestionDataOnNext(currentQuestion);
                    }
                    timer1.schedule(new TimerTask() {
                        public void run() {
                            finish();
                            timer.getCountDownTimer().cancel();
                        }
                    }, 3000);
                }
            }
        }
    }
}