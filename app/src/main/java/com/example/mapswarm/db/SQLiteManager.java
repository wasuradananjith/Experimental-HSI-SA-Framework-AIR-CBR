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
import java.sql.SQLDataException;
import java.util.ArrayList;

public class SQLiteManager {

    private SQLiteHelper sqLiteHelper;
    private Context context;
    private SQLiteDatabase sqLiteDatabase;

    static final String DATABASE_TABLE = "QUESTIONS_BANK";
    static final String QUESTION_ID = "QUESTION_ID";
    static final String QUESTION_CONTENT = "QUESTION_CONTENT";
    static final String IS_DRAWING = "IS_DRAWING";
    static final String ANSWER_1 = "ANSWER_1";
    static final String ANSWER_2 = "ANSWER_2";
    static final String ANSWER_3 = "ANSWER_3";
    static final String ANSWER_4 = "ANSWER_4";
    static final String SA_LEVEL = "SA_LEVEL";
    static final String QUESTION_COUNT = "QUESTION_COUNT";
    private static final String CREATE_QUESTION_BANK_TABLE =
            "CREATE TABLE " + DATABASE_TABLE + " ( "
                    + QUESTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + QUESTION_CONTENT + " TEXT NOT NULL, "
                    + IS_DRAWING + " INTEGER, "
                    + ANSWER_1 + " TEXT NOT NULL, "
                    + ANSWER_2 + " TEXT NOT NULL, "
                    + ANSWER_3 + " TEXT NOT NULL, "
                    + ANSWER_4 + " TEXT NOT NULL, "
                    + SA_LEVEL + " INTEGER,"
                    + QUESTION_COUNT + " INTEGER DEFAULT 0 );";

    public SQLiteManager(Context ctx) {
        this.context = ctx;
    }

    public SQLiteManager open() {
        sqLiteHelper = new SQLiteHelper(context);
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        return this;
    }

    public void close () {
        sqLiteHelper.close();
    }

    public void createQuestionBank() {
        sqLiteDatabase.execSQL(CREATE_QUESTION_BANK_TABLE);
    }

    public void dropQuestionBankIfAlreadyExists() {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
    }

    public void insertQuestionBankData(InputStream inputStream) {
        String line = "";
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,
                Charset.forName("UTF-8")))){
            bufferedReader.readLine(); // step over the header
            while ((line = bufferedReader.readLine()) != null) {
                // Split by ','
                String[] tokens = line.split(",");

                ContentValues contentValues = new ContentValues();
                contentValues.put(QUESTION_CONTENT, tokens[0]);
                contentValues.put(IS_DRAWING, Integer.parseInt(tokens[1]));
                contentValues.put(ANSWER_1, tokens[2]);
                contentValues.put(ANSWER_2, tokens[3]);
                contentValues.put(ANSWER_3, tokens[4]);
                contentValues.put(ANSWER_4, tokens[5]);
                contentValues.put(SA_LEVEL, Integer.parseInt(tokens[6]));
                contentValues.put(QUESTION_COUNT, 0);
                sqLiteDatabase.insert(DATABASE_TABLE, null, contentValues);
            }
        } catch (IOException e) {
            Log.wtf("QuestionBank", "Error reading data file on line " + line, e);
            e.printStackTrace();
        }
    }

    @SuppressLint("Range")
    public ArrayList<Question> fetchQuestionBankData(int count, int limit) {

        ArrayList<Question> questions = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery( "SELECT * from "+DATABASE_TABLE+" WHERE "
                + QUESTION_COUNT + " = " + count + " ORDER BY RANDOM()" + " LIMIT "+
                limit, null );
        //Cursor cursor = sqLiteDatabase.query(DATABASE_TABLE, columns, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                // on below line we are adding the data from
                // cursor to our array list.
                questions.add(new Question(
                        cursor.getString(cursor.getColumnIndex(QUESTION_CONTENT)),
                        cursor.getInt(cursor.getColumnIndex(IS_DRAWING)),
                        cursor.getString(cursor.getColumnIndex(ANSWER_1)),
                        cursor.getString(cursor.getColumnIndex(ANSWER_2)),
                        cursor.getString(cursor.getColumnIndex(ANSWER_3)),
                        cursor.getString(cursor.getColumnIndex(ANSWER_4)),
                        cursor.getInt(cursor.getColumnIndex(SA_LEVEL)),
                        cursor.getInt(cursor.getColumnIndex(QUESTION_COUNT))));
            } while (cursor.moveToNext());
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning the array list.
        cursor.close();
        return questions;
    }
}
