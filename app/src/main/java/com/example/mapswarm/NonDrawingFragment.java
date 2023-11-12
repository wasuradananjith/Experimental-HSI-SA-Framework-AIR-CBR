package com.example.mapswarm;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.mapswarm.model.Question;

/**
 * A simple {@link Fragment} subclass.
 */
public class NonDrawingFragment extends Fragment {

    private Question question;
    private int questionNo;
    private int totalQuestions;
    private TextView questionText;
    private TextView questionNoText;
    private RadioButton answer1;
    private RadioButton answer2;
    private RadioButton answer3;
    private RadioButton answer4;
    private RadioGroup radioGroup;
    private RadioButton selectedRadioButton;
    private View root;

    public NonDrawingFragment(Question question, int questionNo, int totalQuestions) {
        this.question = question;
        this.questionNo = questionNo;
        this.totalQuestions = totalQuestions;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_non_drawing, container, false);
        questionText = root.findViewById(R.id.questionTxt);
        questionNoText = root.findViewById(R.id.questionNo);
        answer1 = root.findViewById(R.id.answer1);
        answer2 = root.findViewById(R.id.answer2);
        answer3 = root.findViewById(R.id.answer3);
        answer4 = root.findViewById(R.id.answer4);
        radioGroup = root.findViewById(R.id.radioGroup);

        questionText.setText(question.getQuestionContent());
        questionNoText.setText("Question " + questionNo + " out of " + totalQuestions);
        answer1.setText(question.getMcqAnswer1());
        answer2.setText(question.getMcqAnswer2());
        answer3.setText(question.getMcqAnswer3());
        answer4.setText(question.getMcqAnswer4());
        return root;
    }

    /**
     * Retrieve selected answer from radio button group
     * @return selected answer
     */
    public String getSelectedAnswer() {
        // get selected radio button from radioGroup
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == -1)
         return null;

        // find the radiobutton by returned id
        RadioButton selectedRadioButton = root.findViewById(selectedId);
        return (String) selectedRadioButton.getText();
    }
}