package com.example.mapswarm;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.mapswarm.model.Question;

/**
 * A simple {@link Fragment} subclass.
 */
public class NonDrawingFragment extends Fragment {

    private Question question;
    private TextView questionText;
    private RadioButton answer1;
    private RadioButton answer2;
    private RadioButton answer3;
    private RadioButton answer4;

    public NonDrawingFragment(Question question) {
        this.question = question;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_non_drawing, container, false);
        questionText = root.findViewById(R.id.questionTxt);
        answer1 = root.findViewById(R.id.answer1);
        answer2 = root.findViewById(R.id.answer2);
        answer3 = root.findViewById(R.id.answer3);
        answer4 = root.findViewById(R.id.answer4);

        questionText.setText(question.getQuestionContent());
        answer1.setText(question.getMcqAnswer1());
        answer2.setText(question.getMcqAnswer2());
        answer3.setText(question.getMcqAnswer3());
        answer4.setText(question.getMcqAnswer4());
        return root;
    }
}