package com.example.mapswarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button swarmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swarmButton = (Button)findViewById(R.id.swarmButton);
        swarmButton.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), SwarmActivity.class);
            startActivity(intent);
        });
    }
}