package com.example.mapswarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button swarmButton;
    private Button drawViewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swarmButton = findViewById(R.id.swarmButton);
        swarmButton.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), SwarmActivity.class);
            startActivity(intent);
        });

        drawViewButton = findViewById(R.id.drawViewButton);
        drawViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DrawingActivity.class);
                startActivity(intent);
            }
        });
    }
}