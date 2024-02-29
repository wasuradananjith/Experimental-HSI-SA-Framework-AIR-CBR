package com.example.mapswarm;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.loadinganimation.LoadingAnimation;
import com.example.mapswarm.db.SQLiteManager;
import com.example.mapswarm.model.Question;
import com.example.mapswarm.util.MyTimer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class QuestionnaireActivity extends AppCompatActivity {

    private Button nextBtn;
    private Button clearBtn;
    private ArrayList<Question> questions;
    private int questionCounter = 0;
    private int activityRound = 0;
    private int questionRound = 0;
    private Question currentQuestion = null;
    private MyTimer timer;
    private TextView timerTextView;
    private long timeLeftInMilliseconds = 150000;
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
    private NonMarkingFragment nonDrawingFragment;
    private MapMarkingFragment mapMarkingFragment;
    private long questionRoundTime;

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
            questionRoundTime = Long.valueOf(extras.getLong("questionRoundTime"));
            questions = retrieveQuestions(questionRound);
        }

        if (questions.size() != 0) {
            // Display the first question when the fragment is loaded
            currentQuestion = questions.get(0);
            currentQuestion.setQuestionRoundTime(questionRoundTime);
            questionStartTime = timer.getTimeLeftInMilliseconds();
            if (currentQuestion.isDrawing() == 1) {
                mapMarkingFragment = new MapMarkingFragment(currentQuestion, questionCounter+1,
                        questions.size(), currentQuestion.getQuestionId());
                getSupportFragmentManager().beginTransaction().add(R.id.container,
                        mapMarkingFragment).commit();
            } else {
                nonDrawingFragment = new NonMarkingFragment(currentQuestion, questionCounter+1,
                        questions.size());
                getSupportFragmentManager().beginTransaction().add(R.id.container,
                        nonDrawingFragment).commit();
            }
        }

        nextBtn = findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(view -> {
            currentQuestion.setQuestionRoundTime(questionRoundTime);
            if (currentQuestion.isDrawing() == 1) {
                currentQuestion.setDrawingAnswer(null); // This is from an old implementation (can be removed in future)

                String markedCells = mapMarkingFragment.getMarkedCellNames();
                String specialAnswer = mapMarkingFragment.getSelectedAnswer();
                if (specialAnswer == null) {
                    if ((markedCells.isEmpty())) {
                        currentQuestion.setMarkedCells("skipped");
                    } else {
                        currentQuestion.setMarkedCells(markedCells);
                    }
                } else {
                    currentQuestion.setMarkedCells(specialAnswer);
                }

                currentQuestion.setNumberOfMarkedCells(mapMarkingFragment.getCurrentMarkingsCount());
                currentQuestion.setMcqAnswer("-");
            } else {
                String answer = nonDrawingFragment.getSelectedAnswer();
                currentQuestion.setMcqAnswer((answer == null)? "skipped": answer);
                currentQuestion.setMarkedCells("-");
                currentQuestion.setNumberOfMarkedCells(0);
            }

            currentQuestion.setCount(currentQuestion.getCount()+1);
            currentQuestion.setElapsedTime((questionStartTime - timer.getTimeLeftInMilliseconds())/1000);

            // Update the csv file in the simulation side
            SwarmActivity.coppeliaSimApi.callAttr("recordQuestionAnswer", SwarmActivity.sim,
                    questionRound, currentQuestion.getQuestionId(), currentQuestion.getMcqAnswer(),
                    currentQuestion.getMarkedCells(), currentQuestion.getNumberOfMarkedCells(),
                    currentQuestion.getElapsedTime());

            // update the database
            updateTheQuestionDataOnNext(currentQuestion);
            questionCounter += 1;

            // If the question counter has not reached the end of the questions
            if (questionCounter != questions.size()) {
                currentQuestion = questions.get(questionCounter);
                if (currentQuestion.isDrawing() == 1) {
                    mapMarkingFragment = new MapMarkingFragment(currentQuestion, questionCounter+1,
                            questions.size(), currentQuestion.getQuestionId());
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.enter_from_right, R.anim.exit_to_left,
                                    R.anim.enter_from_left, R.anim.exit_to_right
                            )
                            .replace(R.id.container, mapMarkingFragment)
                            .addToBackStack(null)
                            .commit();
                    currentQuestion.setDrawingAnswer(null);
                } else {
                    nonDrawingFragment = new NonMarkingFragment(currentQuestion, questionCounter+1,
                            questions.size());
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right
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

        clearBtn = findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentQuestion.isDrawing() == 1) {
                    mapMarkingFragment.clearFields();
                } else {
                    nonDrawingFragment.clearFields();
                }
            }
        });

        handler.post(trackTimer);
    }

    /**
     * Retrieve the questions from the database
     * @param questionRound  questions to be filtered from the
     *                       database based on the question round
     * @return list of questions
     */
    public ArrayList<Question> retrieveQuestions(int questionRound) {
        SQLiteManager sqLiteManager = new SQLiteManager(this);
        try {
            sqLiteManager.open();
            ArrayList<Question> questions = sqLiteManager.fetchQuestionBankData(questionRound,
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
//                if (timer.getTimeLeftInMilliseconds() <= 1000) {
//                    isTimeOutQuestionsUpdated = true;
//                    Timer timer1 = new Timer();
//                    ranOutTimeAnimation.setVisibility(View.VISIBLE);
//                    for (int i = questionCounter; i < questions.size(); i++) {
//                        currentQuestion = questions.get(i);
//                        currentQuestion.setQuestionRoundTime(questionRoundTime);
//                        if (currentQuestion.isDrawing() == 1) {
//                            currentQuestion.setMcqAnswer("drawing");
//                            currentQuestion.setMarkedCells("timeout");
//                            currentQuestion.setNumberOfMarkedCells(0);
//                        } else {
//                            currentQuestion.setMcqAnswer("timeout");
//                            currentQuestion.setMarkedCells("mcq");
//                            currentQuestion.setNumberOfMarkedCells(0);
//                        }
//                        currentQuestion.setCount(currentQuestion.getCount() + 1);
//
//                        // Update the csv file in the simulation side
//                        SwarmActivity.coppeliaSimApi.callAttr("recordQuestionAnswer", SwarmActivity.sim,
//                                questionRound, currentQuestion.getQuestionId(),
//                                currentQuestion.getMcqAnswer(), currentQuestion.getMarkedCells(),
//                                currentQuestion.getNumberOfMarkedCells(),
//                                currentQuestion.getElapsedTime());
//
//                        // update the database
//
//                        updateTheQuestionDataOnNext(currentQuestion);
//                    }
//                    timer1.schedule(new TimerTask() {
//                        public void run() {
//                            finish();
//                            timer.getCountDownTimer().cancel();
//                        }
//                    }, 3000);
//                }
            }
        }
    }

    @Override
    public void onBackPressed() {

    }
}
