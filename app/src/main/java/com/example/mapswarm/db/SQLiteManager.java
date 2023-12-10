package com.example.mapswarm.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mapswarm.model.Question;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class SQLiteManager {

    private SQLiteHelper sqLiteHelper;
    private Context context;
    private SQLiteDatabase sqLiteDatabase;

    public SQLiteManager(Context ctx) {
        this.context = ctx;
    }

    /**
     * Open a new SQLiteManager
     * @return SQLiteManager
     */
    public SQLiteManager open() {
        sqLiteHelper = new SQLiteHelper(context);
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        return this;
    }

    /**
     * Close SQLiteManager
     */
    public void close () {
        sqLiteHelper.close();
    }

    /**
     * Create Question Bank Table
     */
    public void createQuestionBank() {
        sqLiteDatabase.execSQL(Constants.CREATE_QUESTION_BANK_TABLE);
    }

    /**
     * Drop Question Bank Table if already exists
     */
    public void dropQuestionBankIfAlreadyExists() {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Constants.QUESTIONS_BANK_TABLE);
    }

    /**
     * Insert Question Bank to the table
     * @param inputStream file with the questions
     */
    public void insertQuestionBankData(InputStream inputStream) {
        String line = "";
        int count = 0;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,
                Charset.forName("UTF-8")))){
            bufferedReader.readLine(); // step over the header
            while ((line = bufferedReader.readLine()) != null) {
                count += 1;
                // Split by ','
                String[] tokens = line.split(",");

                ContentValues contentValues = new ContentValues();
                contentValues.put(Constants.QUESTION_CONTENT, tokens[0]);
                contentValues.put(Constants.IS_DRAWING, Integer.parseInt(tokens[1]));
                contentValues.put(Constants.ANSWER_1, tokens[2]);
                contentValues.put(Constants.ANSWER_2, tokens[3]);
                contentValues.put(Constants.ANSWER_3, tokens[4]);
                contentValues.put(Constants.ANSWER_4, tokens[5]);
                contentValues.put(Constants.SA_LEVEL, Integer.parseInt(tokens[6]));
                contentValues.put(Constants.QUESTION_ID, count);
                contentValues.put(Constants.QUESTION_COUNT, 0);
                sqLiteDatabase.insert(Constants.QUESTIONS_BANK_TABLE, null, contentValues);
            }
        } catch (IOException e) {
            Log.wtf("QuestionBank", "Error reading data file on line " + line, e);
            e.printStackTrace();
        }
    }

    /**
     * Retrieve Question Bank Data
     * @return
     */
    @SuppressLint("Range")
    public ArrayList<Question> fetchQuestionBankData(int count, int limit) {

        ArrayList<Question> questions = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery( "SELECT * from "+ Constants.QUESTIONS_BANK_TABLE +" WHERE "
                + Constants.QUESTION_COUNT + " = '" + count + "' ORDER BY RANDOM()" + " LIMIT "+
                limit, null );
        //Cursor cursor = sqLiteDatabase.query(DATABASE_TABLE, columns, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                // on below line we are adding the data from
                // cursor to our array list.
                questions.add(new Question(
                        cursor.getString(cursor.getColumnIndex(Constants.QUESTION_CONTENT)),
                        cursor.getInt(cursor.getColumnIndex(Constants.IS_DRAWING)),
                        cursor.getString(cursor.getColumnIndex(Constants.ANSWER_1)),
                        cursor.getString(cursor.getColumnIndex(Constants.ANSWER_2)),
                        cursor.getString(cursor.getColumnIndex(Constants.ANSWER_3)),
                        cursor.getString(cursor.getColumnIndex(Constants.ANSWER_4)),
                        cursor.getInt(cursor.getColumnIndex(Constants.SA_LEVEL)),
                        cursor.getInt(cursor.getColumnIndex(Constants.QUESTION_ID)),
                        cursor.getInt(cursor.getColumnIndex(Constants.QUESTION_COUNT))));
            } while (cursor.moveToNext());
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning the array list.
        cursor.close();
        return questions;
    }

    /**
     * Update the question bank data
     * @param question question
     * @return if update is success or not
     */
    public int updateQuestionBankData(Question question) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.QUESTION_COUNT, question.getCount());
        return sqLiteDatabase.update(Constants.QUESTIONS_BANK_TABLE, contentValues,
                Constants.QUESTION_ID +"= ?",
                new String[] {String.valueOf(question.getQuestionId())});
    }

    /**
     * Create User Round Table
     */
    public void createUserRound() {
        sqLiteDatabase.execSQL(Constants.CREATE_USER_ROUND_TABLE);
    }

    /**
     * Update the User Round Table
     * @param username username of the user
     * @return the round of the updated user
     */
    public int updateUserRound(String username) {

        if (!checkIfTableExists(Constants.USER_ROUND_TABLE)) {
            createUserRound();
        }

        try (Cursor cursor = sqLiteDatabase.rawQuery("SELECT " + Constants.ROUND + " from " +
                Constants.USER_ROUND_TABLE + " WHERE "
                + Constants.USER + " = '" + username + "'", null)) {
            if(cursor.getCount() <= 0){
                ContentValues contentValues = new ContentValues();
                contentValues.put(Constants.USER, username);
                contentValues.put(Constants.ROUND, 1);
                sqLiteDatabase.insert(Constants.USER_ROUND_TABLE, null, contentValues);
                cursor.close();
                return 1;
            } else {
                if (cursor.moveToFirst()) {
                    @SuppressLint("Range")
                    int round = cursor.getInt(cursor.getColumnIndex(Constants.ROUND));
                    round += 1;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(Constants.ROUND, round);
                    sqLiteDatabase.update(Constants.USER_ROUND_TABLE, contentValues,
                            Constants.USER +"= ?", new String[] {username});
                    cursor.close();
                    return round;
                }
            }
        }
        return 0;
    }

    /**
     * Create User Answers Table
     */
    public void createUserAnswer(String userAnswersTableName) {
        String query = Constants.CREATE_USER_ANSWERS_TABLE.replace(Constants.USER_ANSWERS_TABLE,
                userAnswersTableName);
        sqLiteDatabase.execSQL(query);
    }

    /**
     * Update the User Answers Table
     * @param username username of the user
     * @param question question
     * @param questionCount number of time question occured
     * @param activityRound number of turns the same user does the activity
     * @return whether the insert is success or not
     */
    public long insertUserAnswers(String username, Question question, int questionCount,
                                  int activityRound) {

        String userAnswersTableName = username + "_ANSWERS" + activityRound;
        if (!checkIfTableExists(userAnswersTableName)) {
            createUserAnswer(userAnswersTableName);
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.QUESTION_CONTENT, question.getQuestionContent());
        contentValues.put(Constants.IS_DRAWING, question.isDrawing());
        contentValues.put(Constants.ANSWER_1, question.getMcqAnswer1());
        contentValues.put(Constants.ANSWER_2, question.getMcqAnswer2());
        contentValues.put(Constants.ANSWER_3, question.getMcqAnswer3());
        contentValues.put(Constants.ANSWER_4, question.getMcqAnswer4());
        contentValues.put(Constants.SA_LEVEL, question.getSaLevel());
        contentValues.put(Constants.QUESTION_COUNT, questionCount);
        contentValues.put(Constants.MCQ_ANSWER, question.getMcqAnswer());
        contentValues.put(Constants.DRAWING_ANSWER, question.getDrawingAnswer());
        contentValues.put(Constants.MARKED_CELLS, question.getMarkedCells());
        contentValues.put(Constants.NO_OF_MARKED_CELLS, question.getNumberOfMarkedCells());
        contentValues.put(Constants.ELAPSED_TIME, question.getElapsedTime());
        return sqLiteDatabase.insert(userAnswersTableName, null, contentValues);
    }

    /**
     * Check whether a table already exists in the database
     * @param tableName name of the table
     * @return whether table exists or not
     */
    public boolean checkIfTableExists(String tableName) {
        try (Cursor cursor = sqLiteDatabase.rawQuery("SELECT DISTINCT tbl_name from " +
                "sqlite_master where tbl_name = '"+tableName+"';", null)) {
            if (cursor!=null) {
                if(cursor.getCount()>0) {
                    cursor.close();
                    return true;
                }
                cursor.close();
            }
            return false;
        }
    }
}
