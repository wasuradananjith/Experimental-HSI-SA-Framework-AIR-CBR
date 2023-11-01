package com.example.mapswarm;

import static com.example.mapswarm.DrawingView.colourList;
import static com.example.mapswarm.DrawingView.currentBrush;
import static com.example.mapswarm.DrawingView.pathList;

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

/**
 * A simple {@link Fragment} subclass.
 */
public class DrawingFragment extends Fragment {

    public static Path path = new Path();
    public static Paint paintBrush = new Paint();
    private Button pencil;
    private Button eraser;
    private Question question;
    private TextView questionText;

    public DrawingFragment(Question question) {
        this.question = question;
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_drawing, container, false);
        questionText = root.findViewById(R.id.questionTxt);
        questionText.setText(question.getQuestionContent());

        pencil = root.findViewById(R.id.pencil);
        eraser = root.findViewById(R.id.eraser);
        pencil.setOnClickListener(view -> {
            paintBrush.setColor(Color.GREEN);
            currentColour(paintBrush.getColor());
        });

        eraser.setOnClickListener(view -> {
            pathList.clear();
            colourList.clear();
            path.reset();
        });
        return root;
    }

    public void currentColour(int c) {
        currentBrush = c;
        path = new Path();
    }
}