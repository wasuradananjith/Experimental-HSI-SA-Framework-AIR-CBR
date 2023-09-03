package com.example.mapswarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button swarmButton, drawViewButton, logoutButton;
    private TextView welcomeText;
    private FirebaseUser user;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        welcomeText = findViewById(R.id.welcomeText);
        swarmButton = findViewById(R.id.swarmButton);
        drawViewButton = findViewById(R.id.drawViewButton);
        logoutButton = findViewById(R.id.logoutBtn);
        user = auth.getCurrentUser();

        if (user == null) {
             Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
             startActivity(intent);
             finish();
        } else {
            welcomeText.setText("Welcome "+ user.getEmail() + ", to SwarmTactiX!");
        }

        logoutButton.setOnClickListener(view -> {
            auth.signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });

        swarmButton.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), SwarmActivity.class);
            startActivity(intent);
            finish();
        });


        drawViewButton.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), QuestionnaireActivity.class);
            startActivity(intent);
            finish();
        });
    }
}