package com.example.taller1_p2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnIniciarEncuesta;
    private Button btnVerHistorialInicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        new SurveyDbHelper(this).getReadableDatabase().close();

        btnIniciarEncuesta = findViewById(R.id.btnIniciarEncuesta);
        btnVerHistorialInicio = findViewById(R.id.btnVerHistorialInicio);

        btnIniciarEncuesta.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnVerHistorialInicio.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, HistorialActivity.class);
            startActivity(intent);
        });
    }
}
