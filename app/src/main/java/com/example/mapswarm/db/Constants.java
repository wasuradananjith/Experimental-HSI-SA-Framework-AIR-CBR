package com.example.mapswarm.db;

public class Constants {

    // User Round table related constants
    static final String USER_ROUND_TABLE = "USER_ROUND";
    static final String USER = "USER";
    static final String ROUND = "ROUND";

    // Question Bank table related constants
    static final String QUESTIONS_BANK_TABLE = "QUESTIONS_BANK";
    static final String QUESTION_ID = "QUESTION_ID";
    static final String QUESTION_CONTENT = "QUESTION_CONTENT";
    static final String IS_DRAWING = "IS_DRAWING";
    static final String ANSWER_1 = "ANSWER_1";
    static final String ANSWER_2 = "ANSWER_2";
    static final String ANSWER_3 = "ANSWER_3";
    static final String ANSWER_4 = "ANSWER_4";
    static final String SA_LEVEL = "SA_LEVEL";
    static final String QUESTION_COUNT = "QUESTION_COUNT";

    // User Answers table related constants (this uses most of the constants in Question Bank table)
    static final String USER_ANSWERS_TABLE = "USER_ANSWERS";
    static final String MCQ_ANSWER = "MCQ_ANSWER";
    static final String DRAWING_ANSWER = "DRAWING_ANSWER";
    static final String MARKED_CELLS = "MARKED_CELLS";
    static final String NO_OF_MARKED_CELLS = "NO_OF_MARKED_CELLS";
    static final String ELAPSED_TIME = "ELAPSED_TIME";

    static final String CREATE_USER_ROUND_TABLE =
            "CREATE TABLE " + USER_ROUND_TABLE + " ( "
                    + USER + " TEXT NOT NULL PRIMARY KEY, "
                    + ROUND + " INTEGER );";
    static final String CREATE_QUESTION_BANK_TABLE =
            "CREATE TABLE " + QUESTIONS_BANK_TABLE + " ( "
                    + QUESTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + QUESTION_CONTENT + " TEXT NOT NULL, "
                    + IS_DRAWING + " INTEGER, "
                    + ANSWER_1 + " TEXT NOT NULL, "
                    + ANSWER_2 + " TEXT NOT NULL, "
                    + ANSWER_3 + " TEXT NOT NULL, "
                    + ANSWER_4 + " TEXT NOT NULL, "
                    + SA_LEVEL + " INTEGER,"
                    + QUESTION_COUNT + " INTEGER DEFAULT 0 );";

    static final String CREATE_USER_ANSWERS_TABLE =
            "CREATE TABLE " + USER_ANSWERS_TABLE + " ( "
                    + QUESTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + QUESTION_CONTENT + " TEXT NOT NULL, "
                    + IS_DRAWING + " INTEGER, "
                    + ANSWER_1 + " TEXT NOT NULL, "
                    + ANSWER_2 + " TEXT NOT NULL, "
                    + ANSWER_3 + " TEXT NOT NULL, "
                    + ANSWER_4 + " TEXT NOT NULL, "
                    + SA_LEVEL + " INTEGER,"
                    + QUESTION_COUNT + " INTEGER,"
                    + DRAWING_ANSWER + " BLOB,"
                    + MARKED_CELLS + " TEXT,"
                    + NO_OF_MARKED_CELLS + " INTEGER,"
                    + ELAPSED_TIME + " TEXT,"
                    + MCQ_ANSWER + " TEXT DEFAULT '' );";
}
