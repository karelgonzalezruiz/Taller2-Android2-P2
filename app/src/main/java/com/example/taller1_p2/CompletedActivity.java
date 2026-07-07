package com.example.taller1_p2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class CompletedActivity extends AppCompatActivity {

    private Button btnCompletadaInicio;
    private Button btnNuevaEncuesta;
    private Button btnCompletadaHistorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed);

        btnCompletadaInicio = findViewById(R.id.btnCompletadaInicio);
        btnNuevaEncuesta = findViewById(R.id.btnNuevaEncuesta);
        btnCompletadaHistorial = findViewById(R.id.btnCompletadaHistorial);

        btnCompletadaInicio.setOnClickListener(v -> {
            Intent intent = new Intent(CompletedActivity.this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnNuevaEncuesta.setOnClickListener(v -> {
            Intent intent = new Intent(CompletedActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnCompletadaHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(CompletedActivity.this, HistorialActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
