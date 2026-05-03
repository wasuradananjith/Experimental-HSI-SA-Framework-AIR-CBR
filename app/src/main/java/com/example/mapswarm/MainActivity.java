package com.example.mapswarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button swarmButton, drawViewButton, logoutButton;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeText = findViewById(R.id.welcomeText);
        swarmButton = findViewById(R.id.swarmButton);

        welcomeText.setText("Welcome to");
        swarmButton.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), SwarmActivity.class);
            startActivity(intent);
            finish();
        });

    }
}