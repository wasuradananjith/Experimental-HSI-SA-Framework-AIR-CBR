package com.example.mapswarm;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.mapswarm.model.Question;

import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 */
public class DrawingFragment extends Fragment {
    private Button pencil;
    private Button eraser;
    private Question question;
    private int questionNo;
    private int totalQuestions;
    private TextView questionText;
    private TextView questionNoText;

    private DrawingView drawingView;

    public DrawingFragment(Question question, int questionNo, int totalQuestions) {
        this.question = question;
        this.questionNo = questionNo;
        this.totalQuestions = totalQuestions;
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        drawingView = new DrawingView(getActivity());
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_drawing, container, false);
        questionText = root.findViewById(R.id.questionTxt);
        questionText.setText(question.getQuestionContent());

        questionNoText = root.findViewById(R.id.questionNo);
        questionNoText.setText("Question " + questionNo + " out of " + totalQuestions);

        pencil = root.findViewById(R.id.pencil);
        eraser = root.findViewById(R.id.eraser);
        pencil.setOnClickListener(view -> {
            drawingView.getPaintBrush().setColor(Color.GREEN);
            currentColour(drawingView.getPaintBrush().getColor());
        });

        eraser.setOnClickListener(view -> {
            drawingView.getPathList().clear();
            drawingView.getColourList().clear();
            drawingView.getPath().reset();
        });
        return root;
    }

    public void currentColour(int c) {
        drawingView.setCurrentBrush(c);
        drawingView.setPath(new Path());
    }
}