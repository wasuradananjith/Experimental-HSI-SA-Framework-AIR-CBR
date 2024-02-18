package com.example.mapswarm.util;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.mapswarm.model.Question;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class QuestionsBank {

    public QuestionsBank() {
    }

//    public void readAndSetQuestionsFromCsv(InputStream inputStream, DatabaseReference databaseReference) {
//        ArrayList<Question> questions = new ArrayList<>();
//        String line = "";
//        int count = 0;
//        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,
//                Charset.forName("UTF-8")))){
//            bufferedReader.readLine(); // step over the header
//            while ((line = bufferedReader.readLine()) != null) {
//                count += 1;
//                // Split by ','
//                String[] tokens = line.split(",");
//
//                // Read data
//                Question question = new Question(tokens[0], Integer.parseInt(tokens[1]),
//                        tokens[2], tokens[3], tokens[4], tokens[5], Integer.parseInt(tokens[6]),
//                        count,0);
//                questions.add(question);
//            }
//            databaseReference.setValue(questions);
//        } catch (IOException e) {
//            Log.wtf("QuestionBank", "Error reading data file on line " + line, e);
//            e.printStackTrace();
//        }
//    }
}
